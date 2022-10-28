/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox;

import com.google.common.io.Files;
import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.Code2VecAnalyzer;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec.ProgramRelation;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn.GgnnGraphAnalyzer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;
import org.apache.commons.cli.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static de.uni_passau.fim.se2.litterbox.utils.GroupConstants.*;

public final class Main {

    private static final String REFACTOR = "refactor";
    private static final String REFACTOR_SHORT = "r";
    private static final String CHECK = "check";
    private static final String CHECK_SHORT = "c";
    private static final String LEILA = "leila";
    private static final String LEILA_SHORT = "l";
    private static final String NONDET = "nondet";
    private static final String NONDET_SHORT = "n";
    private static final String STATS = "stats";
    private static final String STATS_SHORT = "s";
    private static final String HELP = "help";
    private static final String HELP_SHORT = "h";
    private static final String DETECTORS_LIST = "detectorslist";
    private static final String DETECTORS_LIST_SHORT = "dl";

    private static final String PROJECTPATH = "path";
    private static final String PROJECTPATH_SHORT = "p";
    private static final String PROJECTID = "projectid";
    private static final String PROJECTID_SHORT = "i";
    private static final String PROJECTLIST = "projectlist";
    private static final String PROJECTLIST_SHORT = "t";
    private static final String FEATURE = "feature";
    private static final String FEATURE_SHORT = "f";
    private static final String DELETE_PROJECT_AFTERWARDS = "delete";
    private static final String DELETE_PROJECT_AFTERWARDS_SHORT = "del";

    private static final String OUTPUT_LANG = "lang";
    private static final String OUTPUT_LANG_SHORT = "k";
    private static final String OUTPUT = "output";
    private static final String OUTPUT_SHORT = "o";
    private static final String ANNOTATE = "annotate";
    private static final String ANNOTATE_SHORT = "a";
    private static final String REFACTORED_PROJECTS = "refactored-projects";
    private static final String REFACTORED_PROJECTS_SHORT = "e"; // rEfactored
    private static final String DETECTORS = "detectors";
    private static final String DETECTORS_SHORT = "d";
    private static final String IGNORE_LOOSE_BLOCKS = "ignoreloose";
    private static final String IGNORE_LOOSE_BLOCKS_SHORT = "g";

    // code2vec options
    private static final String CODE2VEC = "code2vec";
    private static final String CODE2VEC_SHORT = "c2v";
    private static final String NOHASH = "nohash";
    private static final String NOHASH_SHORT = "nh";
    private static final String MAXPATHLENGTH = "maxpathlength";
    private static final String MAXPATHLENGTH_SHORT = "plength";

    // GGNN options
    private static final String GRAPH = "gatedgraphnn";
    private static final String GRAPH_SHORT = "ggnn";
    private static final String LABEL_NAME = "labelname";
    private static final String AS_DOT_GRAPH = "dotgraph";

    // shared machine learning preprocessor options
    private static final String INCLUDE_STAGE = "includestage";
    private static final String INCLUDE_STAGE_SHORT = "incstage";
    private static final String WHOLE_PROGRAM = "wholeprogram";
    private static final String WHOLE_PROGRAM_SHORT = "whpro";

    private Main() {
    }

    static Options getCommandLineOptions() {

        // Operation mode
        OptionGroup mainMode = new OptionGroup();
        mainMode.addOption(new Option(REFACTOR_SHORT, REFACTOR, false, "Refactor specified Scratch projects"));
        mainMode.addOption(new Option(CHECK_SHORT, CHECK, false, "Check specified Scratch projects for issues"));
        mainMode.addOption(new Option(LEILA_SHORT, LEILA, false, "Translate specified Scratch projects to Leila"));
        mainMode.addOption(new Option(STATS_SHORT, STATS, false, "Extract project metrics (see also -" + FEATURE_SHORT + ")"));
        mainMode.addOption(new Option(FEATURE_SHORT, FEATURE, false, "Extract script features from Scratch projects (see also -" + STATS_SHORT + ")"));
        mainMode.addOption(new Option(DETECTORS_LIST_SHORT, DETECTORS_LIST, false, "Print a list of all detectors implemented in LitterBox"));
        mainMode.addOption(new Option(CODE2VEC_SHORT, CODE2VEC, false, "Generate text output for specified Scratch projects as input for code2vec"));
        mainMode.addOption(new Option(GRAPH_SHORT, GRAPH, false, "Generate text output for specified Scratch projects as input for the Gated Graph Neural Network"));
        mainMode.addOption(new Option(HELP_SHORT, HELP, false, "Print this message"));

        Options options = new Options();
        options.addOptionGroup(mainMode);

        // Target project(s)
        OptionGroup targetProject = new OptionGroup();
        targetProject.addOption(new Option(PROJECTID_SHORT, PROJECTID, true,
                "id of the project that should be downloaded and analysed."));
        targetProject.addOption(new Option(PROJECTLIST_SHORT, PROJECTLIST, true,
                "path to a file with a list of project ids of projects"
                        + " which should be downloaded and analysed."));
        options.addOptionGroup(targetProject);

        // Storage options
        options.addOption(new Option(PROJECTPATH_SHORT, PROJECTPATH, true,
                "path to folder or file that should be analyzed, or path in which to store downloaded projects"));
        options.addOption(new Option(DELETE_PROJECT_AFTERWARDS_SHORT, DELETE_PROJECT_AFTERWARDS, false, "indicates if project files should be deleted after analysing them"));

        // Output options
        options.addOption(OUTPUT_SHORT, OUTPUT, true,
                "path with name of the csv file you want to save (required if "
                        + "path argument"
                        + " is a folder path)\nusage with --leila: "
                        + "Path to file or folder for the resulting .sc file(s); "
                        + "has to be a folder if multiple projects are analysed "
                        + "(file will be created if not existing yet, path has to exist)"
                        + "\nusage with machine learning preprocessors (e.g., --code2vec): "
                        + "Path to a folder.");
        options.addOption(ANNOTATE_SHORT, ANNOTATE, true, "path where scratch files with hints to bug patterns should"
                + " be created");
        options.addOption(REFACTORED_PROJECTS_SHORT, REFACTORED_PROJECTS, true, "path where the refactored scratch projects should be created");

        // Parameters
        options.addOption(DETECTORS_SHORT, DETECTORS, true, "name all detectors you want to run separated by ',' "
                + " (all detectors defined in the README)");
        options.addOption(NONDET_SHORT, NONDET, false, "flag whether attributes in intermediate "
                + "language should be non deterministic (i.e. not initialized)");
        options.addOption(OUTPUT_LANG_SHORT, OUTPUT_LANG, true, "language of hints in the output");

        options.addOption(IGNORE_LOOSE_BLOCKS_SHORT, IGNORE_LOOSE_BLOCKS, false, "ignore loose blocks when checking bug patterns");

        // Parameters for code2vec and GGNN
        options.addOption(MAXPATHLENGTH_SHORT, MAXPATHLENGTH, true, "maximum of path length for connecting two AST leafs. 0 means there is no max path length. Default is 8");
        options.addOption(NOHASH_SHORT, NOHASH, false, "paths will not be converted to hashes");
        options.addOption(INCLUDE_STAGE_SHORT, INCLUDE_STAGE, false, "generate paths for the stage sprite");
        options.addOption(WHOLE_PROGRAM_SHORT, WHOLE_PROGRAM, false, "generate paths between terminals across the whole program instead of per sprite");
        options.addOption(AS_DOT_GRAPH, false, "generate a dotgraph representation of the GGNN graph");
        options.addOption(LABEL_NAME, false, "use a specific label name for the GGNN graph instead of the file name");

        return options;
    }

    static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("LitterBox", getCommandLineOptions(), true);
        System.out.println("Example: " + "java -jar Litterbox.jar --check --path "
                + "C:\\scratchprojects\\files\\ --output C:\\scratchprojects\\files\\test.csv --detectors bugs\n");
        System.out.println("Example for metric output: "
                + "java -jar Litterbox-1.0.jar --stats -o ~/path/to/folder/or/file/for/the/output --path "
                + "~/path/to/json/project/or/folder/with/projects \n");
        System.out.println("Example for Leila intermediate language output: "
                + "java -jar Litterbox-1.0.jar --leila -o ~/path/to/folder/or/file/for/the/output --path "
                + "~/path/to/json/project/or/folder/with/projects \n");
        System.out.println("Example for refactoring: java -jar Litterbox-1.0.jar -r -p ~/path/to/project -e /path/to/output/folder \n");
        System.out.println("Example to produce a text file as input for code2vec: "
                + "java -jar target/Litterbox-1.7-SNAPSHOT.jar -c2v -output ~/path/to/folder/for/the/output "
                + "-path ~/path/to/json/project/or/folder/with/projects -maxpathlength 8");
    }

    static void printDetectorList() {
        System.out.println("Detectors:");
        IssueTranslator messages = IssueTranslator.getInstance();
        System.out.printf("\t%-20s %-30s%n", ALL, messages.getInfo(ALL));
        System.out.printf("\t%-20s %-30s%n", BUGS, messages.getInfo(BUGS));
        System.out.printf("\t%-20s %-30s%n", SMELLS, messages.getInfo(SMELLS));
        System.out.printf("\t%-20s %-30s%n", PERFUMES, messages.getInfo(PERFUMES));

        System.out.println(System.lineSeparator());
        System.out.printf("\t%-20s %-30s%n", "Bugpatterns:", "");
        IssueTool.getBugFinderNames().forEach(finder -> System.out.printf(
                "\t%-20s %-30s%n",
                finder,
                messages.getName(finder)
        ));

        System.out.println(System.lineSeparator());
        System.out.printf("\t%-20s %-30s%n", "Smells:", "");
        IssueTool.getSmellFinderNames().forEach(finder -> System.out.printf(
                "\t%-20s %-30s%n",
                finder,
                messages.getName(finder)
        ));

        System.out.println(System.lineSeparator());
        System.out.printf("\t%-20s %-30s%n", "Perfumes:", "");
        IssueTool.getPerfumeFinderNames().forEach(finder -> System.out.printf(
                "\t%-20s %-30s%n",
                finder,
                messages.getName(finder)
        ));
    }

    static void refactorPrograms(CommandLine cmd) throws ParseException, IOException {
        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String outputPath = cmd.getOptionValue(OUTPUT);
        String refactoredPath = cmd.getOptionValue(REFACTORED_PROJECTS);
        String input = cmd.getOptionValue(PROJECTPATH);
        boolean delete = cmd.hasOption(DELETE_PROJECT_AFTERWARDS);

        RefactoringAnalyzer refactorer = new RefactoringAnalyzer(input, outputPath, refactoredPath, delete);

        runAnalysis(cmd, refactorer);
    }

    static void checkPrograms(CommandLine cmd) throws ParseException, IOException {
        String outputPath = cmd.getOptionValue(OUTPUT);
        String detectors = cmd.getOptionValue(DETECTORS, DEFAULT);
        String path;
        if (cmd.hasOption(PROJECTPATH)) {
            path = cmd.getOptionValue(PROJECTPATH);
        } else {
            path = Files.createTempDir().getPath();
        }
        boolean ignoreLooseBlocks = cmd.hasOption(IGNORE_LOOSE_BLOCKS);
        BugAnalyzer analyzer = new BugAnalyzer(path, outputPath, detectors, ignoreLooseBlocks, cmd.hasOption(DELETE_PROJECT_AFTERWARDS));

        if (cmd.hasOption(ANNOTATE)) {
            String annotationPath = cmd.getOptionValue(ANNOTATE);
            analyzer.setAnnotationOutput(annotationPath);
        }

        runAnalysis(cmd, analyzer);
    }

    static void translatePrograms(CommandLine cmd) throws ParseException, IOException {
        if (!cmd.hasOption(OUTPUT)) {
            throw new ParseException("Output path option '" + OUTPUT + "' required");
        }

        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String outputPath = cmd.getOptionValue(OUTPUT);
        String input = cmd.getOptionValue(PROJECTPATH);
        boolean nonDet = cmd.hasOption(NONDET);

        LeilaAnalyzer analyzer = new LeilaAnalyzer(input, outputPath, nonDet, false, cmd.hasOption(DELETE_PROJECT_AFTERWARDS));
        runAnalysis(cmd, analyzer);
    }

    static void statsPrograms(CommandLine cmd) throws ParseException, IOException, ParsingException {
        if (!cmd.hasOption(OUTPUT)) {
            throw new ParseException("Output path option '" + OUTPUT + "' required");
        }

        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String outputPath = cmd.getOptionValue(OUTPUT);
        String input = cmd.getOptionValue(PROJECTPATH);
        MetricAnalyzer analyzer = new MetricAnalyzer(input, outputPath, cmd.hasOption(DELETE_PROJECT_AFTERWARDS));
        runAnalysis(cmd, analyzer);
    }

    static void convertToCode2Vec(CommandLine cmd) throws ParseException, IOException {
        MLPreprocessorCommonOptions commonOptions = getMLPreprocessorCommonOptions(cmd);
        int maxPathLength = 8;

        if (cmd.hasOption(NOHASH)) {
            ProgramRelation.setNoHash();
        }

        if (cmd.hasOption(MAXPATHLENGTH)) {
            maxPathLength = Integer.parseInt(cmd.getOptionValue(MAXPATHLENGTH));
            if (maxPathLength < 0) {
                throw new ParseException("Max Path Length can't be negative");
            }
        }

        Code2VecAnalyzer analyzer = new Code2VecAnalyzer(commonOptions, maxPathLength);
        runAnalysis(cmd, analyzer);
    }

    static void convertToGraph(CommandLine cmd) throws ParseException, IOException {
        MLPreprocessorCommonOptions commonOptions = getMLPreprocessorCommonOptions(cmd);
        boolean outputDotStringGraph = cmd.hasOption(AS_DOT_GRAPH);
        String labelName = cmd.getOptionValue(LABEL_NAME);

        GgnnGraphAnalyzer analyzer = new GgnnGraphAnalyzer(commonOptions, outputDotStringGraph, labelName);
        runAnalysis(cmd, analyzer);
    }

    private static MLPreprocessorCommonOptions getMLPreprocessorCommonOptions(CommandLine cmd) throws ParseException {
        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String inputPath = cmd.getOptionValue(PROJECTPATH);
        MLOutputPath outputPath = getMLPreprocessorOutputPath(cmd);
        boolean deleteAfterwards = cmd.hasOption(DELETE_PROJECT_AFTERWARDS)
                || cmd.hasOption(DELETE_PROJECT_AFTERWARDS_SHORT);
        boolean includeStage = cmd.hasOption(INCLUDE_STAGE) || cmd.hasOption(INCLUDE_STAGE_SHORT);
        boolean wholeProgram = cmd.hasOption(WHOLE_PROGRAM) || cmd.hasOption(WHOLE_PROGRAM_SHORT);

        return new MLPreprocessorCommonOptions(inputPath, outputPath, deleteAfterwards, includeStage, wholeProgram);
    }

    private static MLOutputPath getMLPreprocessorOutputPath(CommandLine cmd) throws ParseException {
        if (cmd.hasOption(OUTPUT)) {
            Path outputPath = Path.of(cmd.getOptionValue(OUTPUT));
            File output = outputPath.toFile();
            if (output.exists() && !output.isDirectory()) {
                throw new ParseException("The output path for a machine learning preprocessor must be a directory.");
            }
            return MLOutputPath.directory(outputPath);
        } else {
            return MLOutputPath.console();
        }
    }

    private static void featurePrograms(CommandLine cmd) throws ParseException, IOException {
        if (!cmd.hasOption(OUTPUT)) {
            throw new ParseException("Output path option '" + OUTPUT + "' required");
        }

        if (!cmd.hasOption(PROJECTPATH)) {
            throw new ParseException("Input path option '" + PROJECTPATH + "' required");
        }

        String outputPath = cmd.getOptionValue(OUTPUT);
        String input = cmd.getOptionValue(PROJECTPATH);
        FeatureAnalyzer analyzer = new FeatureAnalyzer(input, outputPath, cmd.hasOption(DELETE_PROJECT_AFTERWARDS));
        runAnalysis(cmd, analyzer);
    }

    static void runAnalysis(CommandLine cmd, Analyzer analyzer) throws IOException {
        if (cmd.hasOption(PROJECTID)) {
            String projectId = cmd.getOptionValue(PROJECTID);
            analyzer.analyzeSingle(projectId);
        } else if (cmd.hasOption(PROJECTLIST)) {
            String projectList = cmd.getOptionValue(PROJECTLIST);
            analyzer.analyzeMultiple(projectList);
        } else {
            analyzer.analyzeFile();
        }
    }

    static void parseCommandLine(String[] args) {
        Options options = getCommandLineOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);

            String lang = cmd.getOptionValue(OUTPUT_LANG, "en");
            IssueTranslator.getInstance().setLanguage(lang);

            if (cmd.hasOption(REFACTOR)) {
                refactorPrograms(cmd);
            } else if (cmd.hasOption(CHECK)) {
                checkPrograms(cmd);
            } else if (cmd.hasOption(STATS)) {
                statsPrograms(cmd);
            } else if (cmd.hasOption(LEILA)) {
                translatePrograms(cmd);
            } else if (cmd.hasOption(CODE2VEC)) {
                convertToCode2Vec(cmd);
            } else if (cmd.hasOption(GRAPH)) {
                convertToGraph(cmd);
            } else if (cmd.hasOption(FEATURE)) {
                featurePrograms(cmd);
            } else if (cmd.hasOption(DETECTORS_LIST)) {
                printDetectorList();
            } else {
                printHelp();
            }
        } catch (ParseException parseException) {
            System.err.println("Invalid option: " + parseException.getMessage());
            printHelp();
        } catch (IOException ioException) {
            System.err.println("Error while trying to read/write project: " + ioException.getMessage());
        } catch (ParsingException parseException) {
            System.err.println("Error while trying to parse project: " + parseException.getMessage());
        }
    }

    /**
     * Entry point to LitterBox where the arguments are parsed and the selected functionality is called.
     *
     * @param args Arguments that are parsed as options.
     */
    public static void main(String[] args) {
        PropertyLoader.setDefaultSystemProperties("litterbox.properties");
        PropertyLoader.setGlobalLoggingLevelFromEnvironment();
        parseCommandLine(args);
    }
}
