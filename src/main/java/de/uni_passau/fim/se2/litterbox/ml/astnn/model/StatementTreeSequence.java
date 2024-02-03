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
package de.uni_passau.fim.se2.litterbox.ml.astnn.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * A sequence of statement trees as required for the ASTNN model together with a label.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record StatementTreeSequence(String originalLabel, String label, List<AstnnNode> statements) {

    public int getMaxDepth() {
        return statements.stream().mapToInt(AstnnNode::getTreeDepth).max().orElse(0);
    }
}
