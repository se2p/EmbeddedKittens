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
package de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.embedded_kittens.JsonTest;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelation;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

class PathGeneratorTest implements JsonTest {

    private final ProgramRelationFactory programRelationFactory = ProgramRelationFactory.withHashCodeFactory();

    final String[] expectedLeaves = { "LOUDNESS", "10", "hello_!", "left_right", "PITCH", "100", "draggable",
        "COLOR", "0", "1", "FORWARD", "FRONT", "NUMBER", "Size", "1", "2", "LOG", "YEAR" };

    @Test
    void testGeneratePaths() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator generator = PathGeneratorFactory.createPathGenerator(
            PathType.SPRITE, 8, false, program, true,
            programRelationFactory, ActorNameNormalizer.getDefault()
        );
        List<ProgramFeatures> pathContextsPerSprite = generator.generatePaths();
        assertEquals(2, pathContextsPerSprite.size());
        int positionCat = 0;
        int positionAbby = 0;
        if (
            pathContextsPerSprite.get(0).getName().equals("cat")
                && pathContextsPerSprite.get(1).getName().equals("abby")
        ) {
            positionAbby = 1;
        }
        else if (
            pathContextsPerSprite.get(1).getName().equals("cat")
                && pathContextsPerSprite.get(0).getName().equals("abby")
        ) {
            positionCat = 1;
        }
        else {
            fail();
        }

        // Sprite cat
        ProgramFeatures cat = pathContextsPerSprite.get(positionCat);
        assertEquals("cat", cat.getName());
        assertEquals(3, cat.getFeatures().size());
        assertEquals("39,625791294,hi_!", cat.getFeatures().get(0).toString());
        assertEquals("39,1493538624,Show", cat.getFeatures().get(1).toString());
        assertEquals("hi_!,-547448667,Show", cat.getFeatures().get(2).toString());

        // Sprite abby
        ProgramFeatures abby = pathContextsPerSprite.get(positionAbby);
        assertEquals("abby", abby.getName());
        assertEquals(1, abby.getFeatures().size());
        assertEquals("GreenFlag,-2069003229,hello_!", abby.getFeatures().get(0).toString());
    }

    @Test
    void testGeneratePathsWithDifferentTokens() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/allChangeableTokens.json");
        PathGenerator generator = PathGeneratorFactory.createPathGenerator(
            PathType.SPRITE, 8, false, program, true,
            programRelationFactory, ActorNameNormalizer.getDefault()
        );
        List<String> tokens = generator.getAllLeaves();
        assertArrayEquals(expectedLeaves, tokens.toArray());
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = { true, false })
    void testGeneratePathsWholeProgram(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator generator = PathGeneratorFactory.createPathGenerator(
            PathType.PROGRAM, 8, includeStage, program,
            true, programRelationFactory, ActorNameNormalizer.getDefault()
        );

        List<ProgramFeatures> pathContexts = generator.generatePaths();
        assertEquals(1, pathContexts.size());
        assertEquals("program", pathContexts.get(0).getName());

        int expectedPathCount;
        List<String> expectedPaths = new ArrayList<>(
            List.of(
                "39,625791294,hi_!",
                "39,1493538624,Show",
                "hi_!,-547448667,Show",
                "GreenFlag,-2069003229,hello_!"
            )
        );
        if (includeStage) {
            expectedPathCount = 6;
            expectedPaths.add("GreenFlag,272321927,GreenFlag");
            expectedPaths.add("GreenFlag,1809747443,10");
        }
        else {
            expectedPathCount = 4;
        }

        List<String> actualPaths = pathContexts.get(0).getFeatures()
            .stream().map(ProgramRelation::toString).toList();
        assertEquals(expectedPathCount, actualPaths.size());

        assertThat(actualPaths).containsExactlyElementsIn(expectedPaths);
    }

    @ParameterizedTest(name = "{displayName} [{index}] pathType={0}, includeStage={1}")
    @MethodSource("code2vecOptions")

    void testGeneratePathsEmptyProgram(PathType pathType, boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");

        PathGenerator generator = PathGeneratorFactory.createPathGenerator(
            pathType, 8, includeStage, program, true,
            programRelationFactory, ActorNameNormalizer.getDefault()
        );
        List<ProgramFeatures> features = generator.generatePaths();
        assertTrue(features.isEmpty());
    }

    private static Stream<Arguments> code2vecOptions() {
        return Stream.of(
            Arguments.arguments(PathType.SPRITE, true),
            Arguments.arguments(PathType.SPRITE, false),
            Arguments.arguments(PathType.SCRIPT, true),
            Arguments.arguments(PathType.SCRIPT, false),
            Arguments.arguments(PathType.PROGRAM, false),
            Arguments.arguments(PathType.PROGRAM, false)
        );
    }
}
