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
package de.uni_passau.fim.se2.embedded_kittens.astnn;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import de.uni_passau.fim.se2.embedded_kittens.astnn.model.AstnnAstNodeFactory;
import de.uni_passau.fim.se2.embedded_kittens.astnn.model.AstnnNode;
import de.uni_passau.fim.se2.embedded_kittens.astnn.model.NodeType;

class AstnnNodeTest {

    @Test
    void emptyLabelLeaf() {
        final AstnnNode node = AstnnAstNodeFactory.build("");
        assertThat(node.label()).isEqualTo(NodeType.EMPTY_STRING.name());
    }

    @Test
    void emptyLabelNode() {
        final AstnnNode child = AstnnAstNodeFactory.build("child");
        final AstnnNode node = AstnnAstNodeFactory.build("", List.of(child));
        assertThat(node.label()).isEqualTo(NodeType.EMPTY_NAME.name());
    }

    @Test
    void blankLabelLeaf() {
        final AstnnNode node = AstnnAstNodeFactory.build("  \t ");
        assertThat(node.label()).isEqualTo(" ");
    }

    @Test
    void blankLabelNode() {
        // custom blocks or parameters with whitespace-only names get the same label
        final AstnnNode child = AstnnAstNodeFactory.build("child");
        final AstnnNode node = AstnnAstNodeFactory.build("  \t\n  ", List.of(child));
        assertThat(node.label()).isEqualTo(NodeType.EMPTY_NAME.name());
    }

    @Test
    void leafDepth() {
        final AstnnNode leaf = AstnnAstNodeFactory.build("abc");
        assertThat(leaf.getTreeDepth()).isEqualTo(1);
    }

    @Test
    void nodeDepth() {
        final AstnnNode leaf1 = AstnnAstNodeFactory.build("a");
        final AstnnNode leaf2 = AstnnAstNodeFactory.build("b");
        final AstnnNode node1 = AstnnAstNodeFactory.build("n1", List.of(leaf1, leaf2));

        final AstnnNode leaf3 = AstnnAstNodeFactory.build("c");
        final AstnnNode node2 = AstnnAstNodeFactory.build("n2", List.of(node1, leaf3));

        assertThat(node2.getTreeDepth()).isEqualTo(3);
    }
}
