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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.metric.ProcedureCount;
import de.uni_passau.fim.se2.litterbox.analytics.metric.ScriptCount;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

class GeneratePathTaskTest implements JsonTest {

    static final String CAT_PATHS = "cat 39,625791294,hi_! 39,1493538624,Show hi_!,-547448667,Show";
    static final String ABBY_PATHS = "abby GreenFlag,-2069003229,hello_!";
    static final String STAGE_PATHS = "stage GreenFlag,1809747443,10";

    /**
     * the only sprite that has a script contains more than one leaf: Cat
     */
    static final List<String> CAT_SCRIPT_PATHS = List.of(
            "39,625791294,hi_!",
            "39,1493538624,Show",
            "hi_!,-547448667,Show",
            "GreenFlag,-2069003229,hello_!"
    );

    @Test
    void testCreateContextEmptyProgram() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/emptyProject.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(PathType.PROGRAM, 8, true, program, false);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextForCode2Vec = generatePathTask.createContextForCode2Vec();
        assertThat(pathContextForCode2Vec).isEmpty();
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testCreateContextForCode2Vec(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(PathType.SPRITE, 8, includeStage, program, false);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContextForCode2Vec();

        if (includeStage) {
            assertThat(pathContextsForCode2Vec).hasSize(3);
        } else {
            assertThat(pathContextsForCode2Vec).hasSize(2);
        }

        List<String> pathContexts = pathContextsForCode2Vec
                .stream()
                .map(ProgramFeatures::toString)
                .collect(Collectors.toList());

        assertThat(pathContexts).contains(CAT_PATHS);
        assertThat(pathContexts).contains(ABBY_PATHS);

        if (includeStage) {
            assertThat(pathContexts).contains(STAGE_PATHS);
        } else {
            assertThat(pathContexts).doesNotContain(STAGE_PATHS);
        }
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testCreateContextCustomProcedures(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/ml_preprocessing/shared/custom_blocks_simple.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(PathType.SPRITE, 8, includeStage, program, false);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);

        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContextForCode2Vec();
        assertThat(pathContextsForCode2Vec).hasSize(1);
        List<ProgramRelation> programRelations = pathContextsForCode2Vec.get(0).getFeatures();

        assertThat(programRelations).hasSize(1);
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testCreateContextForCode2VecPerScripts(boolean includeStage) throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/multipleSprites.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(PathType.SCRIPT, 8, includeStage, program, false);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContextForCode2Vec();

        List<String> pathContexts = pathContextsForCode2Vec
                .stream()
                .flatMap(features -> features.getFeatures().stream())
                .map(ProgramRelation::toString)
                .collect(Collectors.toList());

        if (includeStage) {
            assertThat(pathContexts).hasSize(5);
            assertThat(pathContexts).contains("GreenFlag,1809747443,10");
        } else {
            assertThat(pathContexts).hasSize(4);
        }

        assertThat(pathContexts).containsAtLeastElementsIn(CAT_SCRIPT_PATHS);
    }

    @ParameterizedTest(name = "{displayName} [{index}] includeStage={0}")
    @ValueSource(booleans = {true, false})
    void testCreateContextForCode2VecPerScriptsCountForProgramWithOnlyValidScripts(boolean includeStage) throws IOException, ParsingException {
        Program program = getAST("src/test/fixtures/bugsPerScripts/random_project.json");
        PathGenerator pathGenerator = PathGeneratorFactory.createPathGenerator(PathType.SCRIPT, 8, includeStage, program, false);
        GeneratePathTask generatePathTask = new GeneratePathTask(pathGenerator);
        List<ProgramFeatures> pathContextsForCode2Vec = generatePathTask.createContextForCode2Vec();
        List<String> pathContexts = pathContextsForCode2Vec
                .stream()
                .map(ProgramFeatures::toStringWithoutNodeName)
                .collect(Collectors.toList());

        ScriptCount<ASTNode> scriptCount = new ScriptCount<>();
        int scriptCountPerProgram = (int) scriptCount.calculateMetric(program);

        ProcedureCount<ASTNode> procedureCount = new ProcedureCount<>();
        int procedureCountPerProgram = (int) procedureCount.calculateMetric(program);

        assertThat(pathContexts).hasSize(scriptCountPerProgram + procedureCountPerProgram);
    }
}
