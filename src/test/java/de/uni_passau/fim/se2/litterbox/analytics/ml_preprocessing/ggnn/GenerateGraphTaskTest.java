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
package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.ggnn;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.google.common.truth.Truth.assertThat;

class GenerateGraphTaskTest implements JsonTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphEmptyProgram(boolean wholeProgram) throws Exception {
        List<GgnnProgramGraph> graphs = getGraphs(Path.of("src", "test", "fixtures", "emptyProject.json"), false, wholeProgram);
        assertThat(graphs).hasSize(1);

        int expectedNodeCount;
        if (wholeProgram) {
            expectedNodeCount = 20;
        } else {
            expectedNodeCount = 8;
        }
        assertThat(graphs.get(0).getContextGraph().getNodeLabels()).hasSize(expectedNodeCount);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void testGraphWholeProgram(boolean includeStage) throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(inputPath.toString());
        GenerateGraphTask graphTask = new GenerateGraphTask(program, inputPath, includeStage, true, null);

        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(1);

        String graphJsonl = graphTask.generateJsonGraphData(graphs);
        assertThat(graphJsonl.lines().collect(Collectors.toList())).hasSize(1);
    }

    @Test
    void testVariableGuardedBy() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "guarded_by.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);
        Map<Integer, String> nodeLabels = spriteGraph.getContextGraph().getNodeLabels();
        Set<Pair<Integer>> guardEdges = spriteGraph.getContextGraph().getEdges(GgnnProgramGraph.EdgeType.GUARDED_BY);
        assertThat(guardEdges).hasSize(1);

        assertThat(labelledEdges(guardEdges, nodeLabels)).contains(Pair.of("Variable", "BiggerThan"));
    }

    @Test
    void testVariablesGuardedByIfElse() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "guarded_by_if_else_multiple.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);
        Map<Integer, String> nodeLabels = spriteGraph.getContextGraph().getNodeLabels();
        Set<Pair<Integer>> guardEdges = spriteGraph.getContextGraph().getEdges(GgnnProgramGraph.EdgeType.GUARDED_BY);
        // three different variables are connected to the expression
        assertThat(guardEdges).hasSize(3);

        List<Pair<String>> labelledEdges = labelledEdges(guardEdges, nodeLabels);
        Pair<String> expectedEdge = Pair.of("Variable", "BiggerThan");
        assertThat(labelledEdges).containsExactly(expectedEdge, expectedEdge, expectedEdge);
    }

    @Test
    void testVariablesComputedFrom() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "computed_from.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);
        Map<Integer, String> nodeLabels = spriteGraph.getContextGraph().getNodeLabels();
        Set<Pair<Integer>> guardEdges = spriteGraph.getContextGraph().getEdges(GgnnProgramGraph.EdgeType.COMPUTED_FROM);
        // two different variables on the right side
        assertThat(guardEdges).hasSize(2);

        List<Pair<String>> labelledEdges = labelledEdges(guardEdges, nodeLabels);
        Pair<String> expectedEdge = Pair.of("Variable", "Variable");
        assertThat(labelledEdges).containsExactly(expectedEdge, expectedEdge);
    }

    @Test
    void testParameterPassing() throws Exception {
        Path inputPath = Path.of("src", "test", "fixtures", "ml_preprocessing", "ggnn", "parameter_passing.json");
        List<GgnnProgramGraph> graphs = getGraphs(inputPath, false, false);
        assertThat(graphs).hasSize(1);

        GgnnProgramGraph spriteGraph = graphs.get(0);
        Map<Integer, String> nodeLabels = spriteGraph.getContextGraph().getNodeLabels();
        Set<Pair<Integer>> paramEdges = spriteGraph.getContextGraph().getEdges(GgnnProgramGraph.EdgeType.PARAMETER_PASSING);
        assertThat(paramEdges).hasSize(2);

        List<Pair<String>> labelledEdges = labelledEdges(paramEdges, nodeLabels);
        Pair<String> expectedEdge1 = Pair.of("some|text|input", "ParameterDefinition");
        Pair<String> expectedEdge2 = Pair.of("BiggerThan", "ParameterDefinition");
        assertThat(labelledEdges).containsExactly(expectedEdge1, expectedEdge2);

        // two different parameter definitions as targets
        Set<Integer> edgeTargets = paramEdges.stream().map(Pair::getSnd).collect(Collectors.toSet());
        assertThat(edgeTargets).hasSize(2);
    }

    private List<GgnnProgramGraph> getGraphs(Path fixturePath, boolean includeStage, boolean wholeProgram) throws Exception {
        Program program = getAST(fixturePath.toString());
        GenerateGraphTask graphTask = new GenerateGraphTask(program, fixturePath, includeStage, wholeProgram, null);
        return graphTask.getProgramGraphs();
    }

    private List<Pair<String>> labelledEdges(final Set<Pair<Integer>> edges, final Map<Integer, String> labels) {
        return edges.stream().map(e -> labelledEdge(labels, e)).collect(Collectors.toList());
    }

    private Pair<String> labelledEdge(final Map<Integer, String> labels, final Pair<Integer> edge) {
        return Pair.of(labels.get(edge.getFst()), labels.get(edge.getSnd()));
    }
}
