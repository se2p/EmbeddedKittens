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
package de.uni_passau.fim.se2.litterbox.ml.code2;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessingAnalyzer;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.PathType;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.ProgramFeatures;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelation;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

abstract class Code2Analyzer extends MLPreprocessingAnalyzer<ProgramFeatures> {

    private static final Logger log = Logger.getLogger(Code2Analyzer.class.getName());

    protected final PathType pathType;
    protected final int maxPathLength;

    protected Code2Analyzer(
            final MLPreprocessorCommonOptions commonOptions, final int maxPathLength, final boolean isPerScript
    ) {
        super(commonOptions);

        this.maxPathLength = maxPathLength;

        if (isPerScript) {
            this.pathType = PathType.SCRIPT;
        } else if (wholeProgram) {
            this.pathType = PathType.PROGRAM;
        } else {
            this.pathType = PathType.SPRITE;
        }
    }

    @Override
    protected String resultToString(ProgramFeatures result) {
        return result.toString();
    }

    @Override
    protected Path outputFileName(File inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getName()));
    }

    @Override
    protected void writeResultToFile(
            final Path projectFile, final Program program, final Stream<ProgramFeatures> checkResult
    ) throws IOException {
        if (pathType == PathType.SCRIPT) {
            writeResultPerScriptToOutput(projectFile.toFile(), checkResult.toList());
        } else {
            super.writeResultToFile(projectFile, program, checkResult);
        }
    }

    protected void writeResultPerScriptToOutput(File inputFile, List<ProgramFeatures> result) {
        if (result.isEmpty()) {
            return;
        }

        if (outputPath.isConsoleOutput()) {
            System.out.println(result);
        } else {
            writeResultPerScriptToFile(inputFile, result);
        }
    }

    protected void writeResultPerScriptToFile(File inputFile, List<ProgramFeatures> result) {
        for (ProgramFeatures token : result) {
            Path outName = outputFileName(inputFile);
            Path outputFile = outputPath.getPath().resolve(outName + "_" + token.getName());
            if (Files.exists(outputFile)) {
                log.warning("A duplicated script has been skipped " + outputFile);
                continue;
            }
            writeProgramFeaturesToFile(outputFile, token);
        }
    }

    protected static void writeProgramFeaturesToFile(Path outputFile, ProgramFeatures token) {
        try (BufferedWriter bw = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            bw.write(token.getFeatures().stream().map(ProgramRelation::toString).collect(Collectors.joining(" ")));
            bw.flush();
        } catch (IOException e) {
            log.severe("Exception in writing the file " + outputFile + "Error message " + e.getMessage());
        }
    }
}
