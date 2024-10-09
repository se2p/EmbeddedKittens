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

import static com.google.common.truth.Truth.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.embedded_kittens.JsonTest;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

class GgnnProgramGraphDotGraphBuilderTest implements JsonTest {

    @Test
    void testAllElementsPresent() throws Exception {
        Path filePath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(filePath);
        GenerateGgnnGraphTask graphTask = new GenerateGgnnGraphTask(
            program, true, true, false, null, ActorNameNormalizer.getDefault()
        );
        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(3);

        String dotGraph = graphTask.generateDotGraphData("multipleSprites");
        // one subgraph per sprite
        assertThat(substringCount(dotGraph, "subgraph")).isEqualTo(3);

        long totalEdges = graphs.stream()
            .flatMapToLong(g -> g.contextGraph().edges().values().stream().mapToLong(Set::size))
            .sum();
        assertThat(substringCount(dotGraph, "->")).isEqualTo(totalEdges);
    }

    @Test
    void testSingleGraph() throws Exception {
        Path filePath = Path.of("src", "test", "fixtures", "multipleSprites.json");
        Program program = getAST(filePath);
        GenerateGgnnGraphTask graphTask = new GenerateGgnnGraphTask(
            program, true, true, true, null, ActorNameNormalizer.getDefault()
        );
        List<GgnnProgramGraph> graphs = graphTask.getProgramGraphs();
        assertThat(graphs).hasSize(1);

        String dotGraph = GgnnProgramGraphDotGraphBuilder.asDotGraph(graphs.get(0));
        assertThat(dotGraph).startsWith("digraph \"multipleSprites\" {");
    }

    private int substringCount(String searchIn, String substring) {
        return (searchIn.length() - searchIn.replace(substring, "").length()) / substring.length();
    }
}
