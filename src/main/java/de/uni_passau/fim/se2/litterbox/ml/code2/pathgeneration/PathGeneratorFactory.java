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
package de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.litterbox.ml.shared.ActorNameNormalizer;

public class PathGeneratorFactory {
    private PathGeneratorFactory() {
        throw new IllegalCallerException("utility class");
    }

    public static PathGenerator createPathGenerator(
            PathType type, int maxPathLength, boolean includeStage, Program program, boolean includeDefaultSprites,
            ProgramRelationFactory programRelationFactory, ActorNameNormalizer actorNameNormalizer
    ) {
        return createPathGenerator(
                type, maxPathLength, includeStage, program, includeDefaultSprites, new PathFormatOptions(),
                programRelationFactory, actorNameNormalizer
        );
    }

    public static PathGenerator createPathGenerator(
            PathType type, int maxPathLength, boolean includeStage, Program program, boolean includeDefaultSprites,
            PathFormatOptions pathFormatOptions, ProgramRelationFactory programRelationFactory,
            ActorNameNormalizer actorNameNormalizer
    ) {
        return switch (type) {
            case SCRIPT -> new ScriptEntityPathGenerator(
                    program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions,
                    programRelationFactory
            );
            case PROGRAM -> new ProgramPathGenerator(
                    program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions,
                    programRelationFactory
            );
            default -> new SpritePathGenerator(
                    program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions,
                    programRelationFactory, actorNameNormalizer
            );
        };
    }
}
