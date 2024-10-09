/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens.ggnn;

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import de.uni_passau.fim.se2.embedded_kittens.MLFilePreprocessor;
import de.uni_passau.fim.se2.embedded_kittens.MLPreprocessorCommonOptions;

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
