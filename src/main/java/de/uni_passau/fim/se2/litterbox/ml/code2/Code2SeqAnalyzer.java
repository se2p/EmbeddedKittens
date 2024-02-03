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

import java.util.logging.Logger;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.*;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelationFactory;

public class Code2SeqAnalyzer extends Code2Analyzer {

    private static final Logger log = Logger.getLogger(Code2SeqAnalyzer.class.getName());

    public Code2SeqAnalyzer(
        final MLPreprocessorCommonOptions commonOptions, final int maxPathLength, final boolean isPerScript
    ) {
        super(commonOptions, maxPathLength, isPerScript);
    }

    @Override
    public Stream<ProgramFeatures> check(final Program program) {
        final ProgramRelationFactory programRelationFactory = new ProgramRelationFactory();
        final PathFormatOptions pathFormatOptions = new PathFormatOptions("|", "|", "|", "", "", true, true);
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(
            pathType, maxPathLength, includeStage, program, includeDefaultSprites, pathFormatOptions,
            programRelationFactory, actorNameNormalizer
        );
        final GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);

        return generatePathTask.createContext().stream();
    }
}
