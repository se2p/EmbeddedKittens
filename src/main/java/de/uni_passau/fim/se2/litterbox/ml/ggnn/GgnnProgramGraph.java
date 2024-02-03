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

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public record GgnnProgramGraph(
    String filename,
    String label,
    Set<Integer> labelNodes,
    ContextGraph contextGraph
) {

    public record ContextGraph(
        Map<EdgeType, Set<Pair<Integer>>> edges,
        @JsonProperty("nodeLabelMap") Map<Integer, String> nodeLabels,
        @JsonProperty("nodeTypeMap") Map<Integer, String> nodeTypes
    ) {

        public ContextGraph {
            for (EdgeType edgeType : EdgeType.values()) {
                Preconditions.checkArgument(
                    edges.containsKey(edgeType),
                    "The context graph is missing edges of type %s!",
                    edgeType
                );
            }
        }

        public Set<Pair<Integer>> getEdges(EdgeType edgeType) {
            return edges.get(edgeType);
        }
    }

    public enum EdgeType {
        /**
         * Links a parent to its children.
         */
        CHILD,
        /**
         * Links each token to the following one.
         */
        NEXT_TOKEN,
        /**
         * Links nodes with data dependencies.
         */
        DATA_DEPENDENCY,
        /**
         * Links all variables and attributes on the right-hand side of an assignment to the variable on the left.
         */
        COMPUTED_FROM,
        /**
         * Links variables and attributes occurring in an if-condition to their uses in the then- and else-blocks.
         */
        GUARDED_BY,
        /**
         * Links arguments passed into custom blocks to the parameter definition.
         */
        PARAMETER_PASSING,
        /**
         * Links sending and receiving blocks of messages.
         */
        MESSAGE_PASSING,
    }
}
