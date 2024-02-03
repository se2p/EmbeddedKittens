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
package de.uni_passau.fim.se2.litterbox.ml.ggnn;

import static com.google.common.truth.Truth.assertThat;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.JsonTest;

class GgnnGraphBuilderTest implements JsonTest {

    @Test
    void testNodeIndicesSequential() throws Exception {
        Program p = getAST("src/test/fixtures/multipleSprites.json");
        GgnnGraphBuilder builder = new GgnnGraphBuilder(p);
        GgnnProgramGraph.ContextGraph graph = builder.build();
        Map<Integer, String> nodes = graph.nodeLabels();

        List<Integer> nodeIndices = nodes.keySet().stream().sorted().toList();
        for (int i = 0; i < nodeIndices.size(); ++i) {
            assertThat(nodeIndices.get(i)).isEqualTo(i);
        }
    }

    @Test
    void testLiteralNodeLabels() throws Exception {
        Program p = getAST("src/test/fixtures/ml_preprocessing/ggnn/literal_nodes.json");
        GgnnGraphBuilder builder = new GgnnGraphBuilder(p);
        GgnnProgramGraph.ContextGraph graph = builder.build();
        Map<Integer, String> nodes = graph.nodeLabels();

        Set<String> labels = new HashSet<>(nodes.values());
        // positive numbers remain as is; special symbols should not be removed from negative numbers;
        // spaces are removed from strings; colours as hexadecimal RGB
        assertThat(labels).containsAtLeast("10", "-10", "what_s_your_name_?", "#84135d");
    }
}
