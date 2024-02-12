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

import java.nio.file.Path;

import org.apache.commons.io.FilenameUtils;

import de.uni_passau.fim.se2.litterbox.ml.MLFilePreprocessor;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.ProgramFeatures;

abstract class Code2Preprocessor extends MLFilePreprocessor<ProgramFeatures> {

    protected Code2Preprocessor(
        final Code2ProgramPreprocessor programAnalyzer, final MLPreprocessorCommonOptions commonOptions
    ) {
        super(programAnalyzer, commonOptions.outputPath());
    }

    @Override
    protected Path outputFileName(Path inputFile) {
        return Path.of(FilenameUtils.removeExtension(inputFile.getFileName().toString()));
    }
}
