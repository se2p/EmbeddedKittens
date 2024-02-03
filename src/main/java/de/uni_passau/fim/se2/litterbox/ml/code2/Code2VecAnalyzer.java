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

import java.util.stream.Stream;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.GeneratePathTask;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.PathGenerator;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.PathGeneratorFactory;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.ProgramFeatures;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelationFactory;

public class Code2VecAnalyzer extends Code2Analyzer {

    public Code2VecAnalyzer(final MLPreprocessorCommonOptions commonOptions, int maxPathLength, boolean isPerScript) {
        super(commonOptions, maxPathLength, isPerScript);
    }

    @Override
    public Stream<ProgramFeatures> check(final Program program) {
        final ProgramRelationFactory programRelationFactory = ProgramRelationFactory.withHashCodeFactory();
        final PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(
            pathType, maxPathLength, includeStage, program, includeDefaultSprites, programRelationFactory,
            actorNameNormalizer
        );
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        return generatePathTask.createContext().stream();
    }
}
