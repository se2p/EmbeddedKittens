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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration;

import de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public class PathGeneratorFactory {
    private PathGeneratorFactory() {
        throw new IllegalCallerException("utility class");
    }

    public static PathGenerator createPathGenerator(
            PathType type, int maxPathLength, boolean includeStage, Program program, boolean includeDefaultSprites,
            ProgramRelationFactory programRelationFactory
    ) {
        return createPathGenerator(
                type, maxPathLength, includeStage, program, includeDefaultSprites, new PathFormatOptions(),
                programRelationFactory
        );
    }

    public static PathGenerator createPathGenerator(
            PathType type, int maxPathLength, boolean includeStage, Program program, boolean includeDefaultSprites,
            PathFormatOptions pathFormatOptions, ProgramRelationFactory programRelationFactory
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
                    programRelationFactory
            );
        };
    }
}
