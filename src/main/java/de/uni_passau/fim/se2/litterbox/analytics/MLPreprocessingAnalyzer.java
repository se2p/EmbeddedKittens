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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.MLPreprocessorCommonOptions;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MLPreprocessingAnalyzer extends Analyzer {
    private static final Logger log = Logger.getLogger(MLPreprocessingAnalyzer.class.getName());

    protected final MLOutputPath outputPath;
    protected final boolean includeStage;
    protected final boolean wholeProgram;

    /**
     * Sets up an analyzer that extracts the necessary information for a machine learning model from a program.
     *
     * @param commonOptions Some common options used for all machine learning preprocessors.
     */
    protected MLPreprocessingAnalyzer(final MLPreprocessorCommonOptions commonOptions) {
        super(commonOptions.getInputPath(), commonOptions.getOutputPath().toString(), commonOptions.deleteAfterwards());

        this.outputPath = commonOptions.getOutputPath();
        this.includeStage = commonOptions.includeStage();
        this.wholeProgram = commonOptions.wholeProgram();
    }

    protected abstract Stream<String> process(File inputFile) throws IOException;

    protected abstract Path outputFileName(File inputFile);

    private void runProcessingSteps(File inputFile) throws IOException {
        final Stream<String> output = process(inputFile);
        final String joined = output.collect(Collectors.joining(System.lineSeparator()));
        if (!joined.isBlank()) {
            writeResultToOutput(inputFile, joined);
        }
    }

    private void writeResultToOutput(File inputFile, String result) throws IOException {
        if (result.isBlank()) {
            log.warning("The processing step returned no output!");
            return;
        }

        if (outputPath.isConsoleOutput()) {
            System.out.println(result);
        } else {
            writeResultToFile(inputFile, result);
        }
    }

    private void writeResultToFile(File inputFile, String result) throws IOException {
        Files.createDirectories(outputPath.getPath());

        Path outName = outputFileName(inputFile);
        Path outputFile = outputPath.getPath().resolve(outName);

        try (BufferedWriter bw = Files.newBufferedWriter(outputFile)) {
            bw.write(result);
            bw.flush();
        }
        log.info("Wrote processing result of " + inputFile + " to file " + outputFile);
    }

    @Override
    void check(File fileEntry, String csv) throws IOException {
        runProcessingSteps(fileEntry);
    }
}
