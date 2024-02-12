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
package de.uni_passau.fim.se2.litterbox.ml.ggnn;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import de.uni_passau.fim.se2.litterbox.ml.MLFilePreprocessor;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;

public class GgnnGraphPreprocessor extends MLFilePreprocessor<GgnnAnalyzerOutput> {

    private final GgnnOutputFormat outputFormat;

    public GgnnGraphPreprocessor(
        final MLPreprocessorCommonOptions commonOptions,
        GgnnOutputFormat outputFormat,
        String labelName
    ) {
        super(new GgnnProgramPreprocessor(commonOptions, outputFormat, labelName), commonOptions.outputPath());

        this.outputFormat = outputFormat;
    }

    @Override
    protected Path outputFileName(final Path inputFile) {
        final String format;
        if (outputFormat.isDotGraph()) {
            format = ".dot";
        }
        else {
            format = ".jsonl";
        }

        return Path.of("GraphData_" + FilenameUtils.removeExtension(inputFile.getFileName().toString()) + format);
    }
}
