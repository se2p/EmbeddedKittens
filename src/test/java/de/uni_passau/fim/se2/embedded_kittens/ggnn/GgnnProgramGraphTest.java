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
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.embedded_kittens.JsonTest;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.utils.Pair;

class GgnnProgramGraphTest implements JsonTest {

    @Test
    void testConstructionMissingEdgeTypes() {
        final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges = new HashMap<>();
        for (GgnnProgramGraph.EdgeType t : GgnnProgramGraph.EdgeType.values()) {
            edges.put(t, new HashSet<>());
        }
        edges.remove(GgnnProgramGraph.EdgeType.CHILD);

        assertThrows(
            IllegalArgumentException.class, () -> new GgnnProgramGraph.ContextGraph(edges, Map.of(), Map.of())
        );
    }

    @Test
    void testContainsAllEdgeTypes() throws Exception {
        Program p = getAST("src/test/fixtures/emptyProject.json");
        GgnnGraphBuilder builder = new GgnnGraphBuilder(p);
        GgnnProgramGraph.ContextGraph graph = builder.build();

        Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges = graph.edges();
        int expectedSize = GgnnProgramGraph.EdgeType.values().length;
        assertThat(edges).hasSize(expectedSize);
    }
}
