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

import java.util.List;
import java.util.Map;
import java.util.Set;

import de.uni_passau.fim.se2.litterbox.utils.Pair;

public class GgnnProgramGraphDotGraphBuilder {

    private GgnnProgramGraphDotGraphBuilder() {
    }

    /**
     * Builds a Graphviz DOT-graph representation of the graphs.
     *
     * <p>
     * Creates one overall graph with the given label with each of the given graphs being its own subgraph beneath it.
     *
     * @param graphs Some GGNN program graphs.
     * @param label  The label for the overall graph.
     * @return A graph in Graphviz DOT-format.
     */
    public static String asDotGraph(final List<GgnnProgramGraph> graphs, final String label) {
        final StringBuilder sb = new StringBuilder();
        int graphId = 1;
        sb.append("digraph \"").append(label).append("\" {\n");
        sb.append("fontname=\"serif\";\n");
        sb.append("label=\"").append(label).append("\";\n");
        for (GgnnProgramGraph graph : graphs) {
            sb.append(GgnnProgramGraphDotGraphBuilder.asDotSubgraph(graph, graphId));
            graphId += 1;
        }
        sb.append("}\n");

        return sb.toString();
    }

    public static String asDotGraph(final GgnnProgramGraph programGraph) {
        return asDotGraph(programGraph.contextGraph(), programGraph.label(), 0, false);
    }

    public static String asDotSubgraph(final GgnnProgramGraph programGraph, int graphId) {
        return asDotGraph(programGraph.contextGraph(), programGraph.label(), graphId, true);
    }

    private static String asDotGraph(
        final GgnnProgramGraph.ContextGraph graph, final String label, int graphId,
        boolean isSubgraph
    ) {
        StringBuilder sb = new StringBuilder();

        if (isSubgraph) {
            sb.append("subgraph \"cluster_");
        }
        else {
            sb.append("digraph \"");
        }
        sb.append(label).append("\" {\n");
        sb.append("label=\"").append(label).append("\";\n");
        sb.append("fontname=\"serif\";\n");
        sb.append("node[fontname=\"serif\"];\n");

        for (Map.Entry<Integer, String> node : graph.nodeLabels().entrySet()) {
            appendNode(sb, graphId, node.getKey());
            sb.append(" [label=\"").append(node.getValue()).append("\"];\n");
        }
        sb.append('\n');

        for (Map.Entry<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edgeGroup : graph.edges().entrySet()) {
            String edgeColor = getEdgeColor(edgeGroup.getKey());

            for (Pair<Integer> edge : edgeGroup.getValue()) {
                appendNode(sb, graphId, edge.getFst());
                sb.append(" -> ");
                appendNode(sb, graphId, edge.getSnd());
                sb.append(' ').append(edgeColor).append(";\n");
            }
        }

        sb.append("}\n");

        return sb.toString();
    }

    private static void appendNode(final StringBuilder sb, int graphId, int nodeId) {
        sb.append(graphId).append('.').append(nodeId);
    }

    private static String getEdgeColor(GgnnProgramGraph.EdgeType edgeType) {
        return switch (edgeType) {
            case CHILD -> "[color=black]";
            case NEXT_TOKEN -> "[color=blue]";
            case DATA_DEPENDENCY -> "[color=green]";
            case COMPUTED_FROM -> "[color=orange]";
            case GUARDED_BY -> "[color=maroon]";
            case PARAMETER_PASSING -> "[color=purple]";
            case MESSAGE_PASSING -> "[color=darkcyan]";
            case RETURN_TO -> "[color=cornflowerblue]";
        };
    }
}
