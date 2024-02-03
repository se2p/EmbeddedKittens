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

import java.nio.file.Path;

import de.uni_passau.fim.se2.litterbox.ml.shared.ActorNameNormalizer;

/**
 * Combines some options used for all machine learning preprocessing analyzers.
 *
 * @param inputPath             The path to the file that should be analysed.
 * @param outputPath            The path which the results should be written to.
 * @param deleteAfterwards      If the input path should be deleted after processing has finished.
 * @param includeStage          If the stage should be included like a regular sprite in the processing steps.
 * @param wholeProgram          If the whole program should be treated as a single entity instead of performing the
 *                              analysis per sprite.
 * @param includeDefaultSprites If output should be generated for sprites that have the default name, e.g. ‘Sprite1’.
 * @param abstractTokens        If literals and variable names should be replaced by generic placeholders, e.g.
 *                              {@code var}.
 */
public record MLPreprocessorCommonOptions(
    Path inputPath,
    MLOutputPath outputPath,
    boolean deleteAfterwards,
    boolean includeStage,
    boolean wholeProgram,
    boolean includeDefaultSprites,
    boolean abstractTokens,
    ActorNameNormalizer actorNameNormalizer
) {
}
