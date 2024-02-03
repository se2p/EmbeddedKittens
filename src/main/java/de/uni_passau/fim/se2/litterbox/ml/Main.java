/*
 * Copyright (C) 2021-2024 LitterBox-ML contributors
 *
 * This file is part of LitterBox-ML.
 *
 * LitterBox-ML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox-ML is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox-ML. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ml;

import de.uni_passau.fim.se2.litterbox.analytics.Analyzer;
import de.uni_passau.fim.se2.litterbox.ml.astnn.AstnnAnalyzer;
import de.uni_passau.fim.se2.litterbox.ml.code2.Code2SeqAnalyzer;
import de.uni_passau.fim.se2.litterbox.ml.code2.Code2VecAnalyzer;
import de.uni_passau.fim.se2.litterbox.ml.ggnn.GgnnGraphAnalyzer;
import de.uni_passau.fim.se2.litterbox.ml.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.ml.tokenizer.TokenizingAnalyzer;
import de.uni_passau.fim.se2.litterbox.ml.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ml.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import picocli.CommandLine;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;

@CommandLine.Command(
        name = "LitterBox-ML",
        mixinStandardHelpOptions = true,
        version = "LitterBox-ML 1.0-SNAPSHOT",
        subcommands = {
                Main.AstnnSubcommand.class,
                Main.Code2SeqSubcommand.class,
                Main.Code2vecSubcommand.class,
                Main.GgnnSubcommand.class,
                Main.TokenizerSubcommand.class
        },
        footerHeading = "%nExamples:%n",
        footer = {
                "%nExample for code2vec preprocessing:%n"
                        + "java -jar Litterbox.jar code2vec%n"
                        + "    --path ~/path/to/json/project/or/folder/with/projects%n"
                        + "    -o ~/path/to/folder/or/file/for/the/output%n"
                        + "    --max-path-length 8",
        }
)
public class Main implements Callable<Integer> {
    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    @Override
    public Integer call() throws Exception {
        spec.commandLine().usage(System.out);
        return 0;
    }

    public static void main(String... args) {
        PropertyLoader.setDefaultSystemProperties("litterbox.properties");
        PropertyLoader.setGlobalLoggingLevelFromEnvironment();

        int exitCode = new CommandLine(new Main()).execute(args);
        System.exit(exitCode);
    }

    @CommandLine.Command(mixinStandardHelpOptions = true)
    abstract static class LitterBoxSubcommand implements Callable<Integer> {
        @CommandLine.Spec
        CommandLine.Model.CommandSpec spec;

        @CommandLine.Option(
                names = {"-l", "--lang"},
                description = "Language of the hints in the output."
        )
        String language = "en";

        @CommandLine.Option(
                names = {"-p", "--path"},
                description = "Path to the folder or file that should be analysed, "
                        + "or path in which to store downloaded projects."
        )
        Path projectPath;

        @CommandLine.Option(
                names = {"--project-id"},
                description = "ID of the project that should be downloaded and analysed."
        )
        String projectId;

        @CommandLine.Option(
                names = {"--project-list"},
                description = "Path to a file with a list of project ids which should be downloaded and analysed."
        )
        Path projectList;

        @CommandLine.Option(
                names = {"-o", "--output"},
                description = "Path to the file or folder for the analyser results. "
                        + "Has to be a folder if multiple projects are analysed."
        )
        Path outputPath;

        @CommandLine.Option(
                names = {"--delete"},
                description = "Delete the project files after analysing them."
        )
        boolean deleteProject;

        protected abstract Analyzer<?> getAnalyzer() throws Exception;

        /**
         * Override to implement custom parameter validation before the analyzer is run.
         *
         * @throws Exception Thrown when an invalid parameter configuration was passed.
         */
        protected void validateParams() throws Exception {
            // intentionally empty here, to be implemented by subclasses when needed
        }

        @Override
        public final Integer call() throws Exception {
            IssueTranslator.getInstance().setLanguage(language);

            validateParams();

            final Analyzer<?> analyzer = getAnalyzer();
            return runAnalysis(analyzer);
        }

        private int runAnalysis(final Analyzer<?> analyzer) throws IOException {
            if (projectId != null) {
                analyzer.analyzeSingle(projectId);
            } else if (projectList != null) {
                analyzer.analyzeMultiple(projectList);
            } else {
                analyzer.analyzeFile();
            }

            return 0;
        }

        protected void requireProjectPath() throws CommandLine.ParameterException {
            if (projectPath == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Input path option '--path' required.");
            }
        }

        protected void requireOutputPath() throws CommandLine.ParameterException {
            if (outputPath == null) {
                throw new CommandLine.ParameterException(spec.commandLine(), "Output path option '--output' required.");
            }
        }
    }

    abstract static class MLPreprocessorSubcommand extends LitterBoxSubcommand {
        @CommandLine.Option(
                names = {"-s", "--include-stage"},
                description = "Include the stage like a regular sprite into the analysis."
        )
        boolean includeStage;

        @CommandLine.Option(
                names = {"-w", "--whole-program"},
                description = "Treat the program as a single big sprite."
        )
        boolean wholeProgram;

        @CommandLine.Option(
                names = {"--include-default-sprites"},
                description = "Include sprites that have the default name in any language, e.g. ‘Sprite1’, ‘Actor3’."
        )
        boolean includeDefaultSprites;

        @CommandLine.Option(
                names = {"--abstract-tokens"},
                description = "Replaces literal values and variable names with abstract tokens. "
                        + "E.g., '1.0' -> 'numberliteral'."
        )
        boolean abstractTokens = false;

        @CommandLine.Option(
                names = {"--latin-only-sprite-names"},
                description = "Normalise sprite names to include characters in the latin base alphabet (a-z) only."
        )
        boolean latinOnlyActorNames;

        protected final MLOutputPath getOutputPath() throws CommandLine.ParameterException {
            if (outputPath != null) {
                final File outputDirectory = outputPath.toFile();
                if (outputDirectory.exists() && !outputDirectory.isDirectory()) {
                    throw new CommandLine.ParameterException(
                            spec.commandLine(),
                            "The output path for a machine learning preprocessor must be a directory."
                    );
                }
                return MLOutputPath.directory(outputDirectory.toPath());
            } else {
                return MLOutputPath.console();
            }
        }

        protected final MLPreprocessorCommonOptions getCommonOptions() {
            requireProjectPath();

            final MLOutputPath outputPath = getOutputPath();
            return new MLPreprocessorCommonOptions(projectPath, outputPath, deleteProject, includeStage, wholeProgram,
                    includeDefaultSprites, abstractTokens, buildActorNameNormalizer());
        }

        private ActorNameNormalizer buildActorNameNormalizer() {
            if (latinOnlyActorNames) {
                return NodeNameUtil::normalizeSpriteNameLatinOnly;
            } else {
                return ActorNameNormalizer.getDefault();
            }
        }
    }

    @CommandLine.Command(
            name = "astnn",
            description = "Transform Scratch projects into the ASTNN input format."
    )
    static class AstnnSubcommand extends MLPreprocessorSubcommand {

        @Override
        protected AstnnAnalyzer getAnalyzer() {
            return new AstnnAnalyzer(getCommonOptions());
        }
    }

    private abstract static class Code2Subcommand extends MLPreprocessorSubcommand {

        @CommandLine.Option(
                names = {"--max-path-length"},
                description = "The maximum length for connecting two AST leaves. "
                        + "Zero means there is no max path length. "
                        + "Default: 8."
        )
        int maxPathLength = 8;

        @CommandLine.Option(
                names = {"--scripts"},
                description = "Generate token per script."
        )
        boolean isPerScript = false;

        @Override
        protected void validateParams() throws CommandLine.ParameterException {
            if (maxPathLength < 0) {
                throw new CommandLine.ParameterException(spec.commandLine(), "The path length can’t be negative.");
            }

            if (wholeProgram && isPerScript) {
                throw new CommandLine.ParameterException(
                        spec.commandLine(),
                        "The analysis must be done either per script or for whole program"
                );
            }
        }
    }

    @CommandLine.Command(
            name = "code2vec",
            description = "Transform Scratch projects into the code2vec input format."
    )
    static class Code2vecSubcommand extends Code2Subcommand {

        @Override
        protected Code2VecAnalyzer getAnalyzer() {
            return new Code2VecAnalyzer(getCommonOptions(), maxPathLength, isPerScript);
        }
    }

    @CommandLine.Command(
            name = "code2seq",
            description = "Transform Scratch projects into the code2seq input format."
    )
    static class Code2SeqSubcommand extends Code2Subcommand {

        @Override
        protected Code2SeqAnalyzer getAnalyzer() {
            return new Code2SeqAnalyzer(getCommonOptions(), maxPathLength, isPerScript);
        }
    }

    @CommandLine.Command(
            name = "ggnn",
            description = "Transform Scratch projects into the Gated Graph Neural Network input format."
    )
    static class GgnnSubcommand extends MLPreprocessorSubcommand {

        @CommandLine.Option(
                names = {"--label"},
                description = "Use a specific label for the graph instead of the file name."
        )
        String label;

        @CommandLine.Option(
                names = {"--dotgraph"},
                description = "Generate a dot-graph representation of the GGNN graph."
        )
        boolean dotGraph;

        @Override
        protected GgnnGraphAnalyzer getAnalyzer() {
            return new GgnnGraphAnalyzer(getCommonOptions(), dotGraph, label);
        }
    }

    @CommandLine.Command(
            name = "tokenizer",
            description = "Transforms each Scratch project into a token sequence."
    )
    static class TokenizerSubcommand extends MLPreprocessorSubcommand {

        @CommandLine.Option(
                names = {"--sequence-per-script"},
                description = "Generate one token sequence per script instead of per sprite/program."
                        + "Custom procedure definitions count as scripts."
        )
        boolean sequencePerScript = false;

        @CommandLine.Option(
                names = {"--abstract-fixed-node-options"},
                description = "Replace fixed node options with abstract tokens."
        )
        boolean abstractFixedNodeOption = false;

        @CommandLine.Option(
                names = {"--statement-level"},
                description = "Generate a sequence consisting of only statement tokens."
        )
        boolean statementLevel = false;

        @CommandLine.Option(
                names = {"--masked-statement-id"},
                description = "Block-Id of the statement to mask. Default: no masking."
        )
        String maskedStatementId = null;

        @Override
        protected TokenizingAnalyzer getAnalyzer() {
            if (wholeProgram && sequencePerScript) {
                throw new CommandLine.ParameterException(
                        spec.commandLine(),
                        "Cannot generate one sequence for the whole program and sequences per script at the same time."
                );
            }

            final var maskingStrategy = maskedStatementId == null
                    ? MaskingStrategy.none()
                    : MaskingStrategy.statement(maskedStatementId);
            return new TokenizingAnalyzer(getCommonOptions(), sequencePerScript, abstractFixedNodeOption,
                    statementLevel, maskingStrategy);
        }
    }
}
