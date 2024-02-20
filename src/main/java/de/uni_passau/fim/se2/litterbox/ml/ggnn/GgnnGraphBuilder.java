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

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraph;
import de.uni_passau.fim.se2.litterbox.cfg.ControlFlowGraphVisitor;
import de.uni_passau.fim.se2.litterbox.dependency.DataDependenceGraph;
import de.uni_passau.fim.se2.litterbox.ml.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.ml.util.ProcedureMapping;
import de.uni_passau.fim.se2.litterbox.utils.Pair;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class GgnnGraphBuilder {

    private static final Logger log = Logger.getLogger(GgnnGraphBuilder.class.getName());

    /**
     * Remove the parameters from the procedure definition and call block names.
     */
    private static final String PROCEDURE_PARAM_REPLACEMENT = "";

    private final Program program;
    private final ASTNode astRoot;
    private final ProcedureMapping procedureMapping;

    public GgnnGraphBuilder(final Program program) {
        this(program, program);
    }

    public GgnnGraphBuilder(final Program program, final ActorDefinition astRoot) {
        this(program, (ASTNode) astRoot);
    }

    private GgnnGraphBuilder(final Program program, final ASTNode astRoot) {
        Preconditions.checkAllArgsNotNull(List.of(program, astRoot));

        this.program = program;
        this.astRoot = astRoot;
        this.procedureMapping = new ProcedureMapping(program);
    }

    /**
     * Builds the GGNN context graph.
     *
     * @return The context graph for a program.
     */
    public GgnnProgramGraph.ContextGraph build() {
        final List<Pair<ASTNode>> childEdges = GgnnGraphEdgesVisitor.getChildEdges(astRoot);
        final List<Pair<ASTNode>> nextTokenEdges = GgnnGraphEdgesVisitor.getNextTokenEdges(astRoot);
        final List<Pair<ASTNode>> guardedByEdges = GgnnGraphEdgesVisitor.getGuardedByEdges(astRoot);
        final List<Pair<ASTNode>> computedFromEdges = GgnnGraphEdgesVisitor.getComputedFromEdges(astRoot);
        final List<Pair<ASTNode>> parameterPassingEdges = GgnnGraphEdgesVisitor
            .getParameterPassingEdges(program, astRoot);
        final List<Pair<ASTNode>> messagePassingEdges = GgnnGraphEdgesVisitor.getMessagePassingEdges(astRoot);
        final List<Pair<ASTNode>> dataDependencies = getDataDependencies();
        final List<Pair<ASTNode>> returnToEdges = GgnnGraphEdgesVisitor.getReturnToEdges(astRoot);

        final Set<ASTNode> allNodes = getAllNodes(
            childEdges, nextTokenEdges, guardedByEdges, computedFromEdges,
            parameterPassingEdges, messagePassingEdges, dataDependencies, returnToEdges
        );
        final Map<ASTNode, Integer> nodeIndices = getNodeIndices(allNodes);

        final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges = new EnumMap<>(GgnnProgramGraph.EdgeType.class);
        edges.put(GgnnProgramGraph.EdgeType.CHILD, getIndexedEdges(nodeIndices, childEdges));
        edges.put(GgnnProgramGraph.EdgeType.NEXT_TOKEN, getIndexedEdges(nodeIndices, nextTokenEdges));
        edges.put(GgnnProgramGraph.EdgeType.GUARDED_BY, getIndexedEdges(nodeIndices, guardedByEdges));
        edges.put(GgnnProgramGraph.EdgeType.COMPUTED_FROM, getIndexedEdges(nodeIndices, computedFromEdges));
        edges.put(GgnnProgramGraph.EdgeType.DATA_DEPENDENCY, getIndexedEdges(nodeIndices, dataDependencies));
        edges.put(GgnnProgramGraph.EdgeType.PARAMETER_PASSING, getIndexedEdges(nodeIndices, parameterPassingEdges));
        edges.put(GgnnProgramGraph.EdgeType.MESSAGE_PASSING, getIndexedEdges(nodeIndices, messagePassingEdges));
        edges.put(GgnnProgramGraph.EdgeType.RETURN_TO, getIndexedEdges(nodeIndices, returnToEdges));

        Set<Integer> usedNodes = getUsedNodes(edges);
        final Map<Integer, String> nodeLabels = getNodeLabels(nodeIndices, usedNodes);
        final Map<Integer, String> nodeTypes = getNodeTypes(nodeIndices, usedNodes);

        return new GgnnProgramGraph.ContextGraph(edges, nodeLabels, nodeTypes);
    }

    @SafeVarargs
    private Set<ASTNode> getAllNodes(final List<Pair<ASTNode>>... nodes) {
        // identity hash set instead of regular set, as variable nodes with the same name have the same hash code
        final Supplier<Set<ASTNode>> allNodesSet = () -> Collections.newSetFromMap(new IdentityHashMap<>());
        return Arrays.stream(nodes)
            .flatMap(List::stream)
            .flatMap(Pair::stream)
            .collect(Collectors.toCollection(allNodesSet));
    }

    private Map<ASTNode, Integer> getNodeIndices(final Collection<ASTNode> nodes) {
        final Map<ASTNode, Integer> nodeIndices = new IdentityHashMap<>(nodes.size());
        int idx = 0;
        for (ASTNode node : nodes) {
            nodeIndices.put(node, idx++);
        }
        return nodeIndices;
    }

    private Set<Pair<Integer>> getIndexedEdges(
        final Map<ASTNode, Integer> nodeIndices,
        final List<Pair<ASTNode>> edges
    ) {
        return edges.stream().map(edge -> {
            Integer idxFrom = nodeIndices.get(edge.getFst());
            Integer idxTo = nodeIndices.get(edge.getSnd());
            return Pair.of(idxFrom, idxTo);
        }).collect(Collectors.toSet());
    }

    private Set<Integer> getUsedNodes(final Map<GgnnProgramGraph.EdgeType, Set<Pair<Integer>>> edges) {
        return edges.values()
            .stream()
            .flatMap(Set::stream)
            .flatMap(Pair::stream)
            .collect(Collectors.toSet());
    }

    private Map<Integer, String> getNodeLabels(
        final Map<ASTNode, Integer> nodeIndices,
        final Set<Integer> usedIndices
    ) {
        return getNodeInformation(nodeIndices, usedIndices, this::getNodeLabel);
    }

    private String getNodeLabel(final ASTNode node) {
        if (node instanceof StrId && node.getParentNode() instanceof ProcedureDefinition procedureDefinition) {
            final String name = procedureMapping.getName(procedureDefinition);
            return AstNodeUtil.replaceProcedureParams(name, PROCEDURE_PARAM_REPLACEMENT);
        }
        else if (node instanceof StrId && node.getParentNode() instanceof CallStmt callStmt) {
            final Optional<ProcedureDefinition> definition = procedureMapping.findCalledProcedure(callStmt);
            if (definition.isEmpty()) {
                log.info("Could not find procedure for custom block call: " + callStmt.getIdent().getName());
                return TokenVisitorFactory.getNormalisedToken(node);
            }
            else {
                final String name = procedureMapping.getName(definition.get());
                return AstNodeUtil.replaceProcedureParams(name, PROCEDURE_PARAM_REPLACEMENT);
            }
        }
        else {
            return TokenVisitorFactory.getNormalisedToken(node);
        }
    }

    private Map<Integer, String> getNodeTypes(final Map<ASTNode, Integer> nodeIndices, final Set<Integer> usedIndices) {
        return getNodeInformation(nodeIndices, usedIndices, node -> node.getClass().getSimpleName());
    }

    private Map<Integer, String> getNodeInformation(
        final Map<ASTNode, Integer> nodeMap, final Set<Integer> usedIndices,
        final Function<ASTNode, String> infoExtractor
    ) {
        final Map<Integer, String> nodeLabels = new HashMap<>();

        for (Map.Entry<ASTNode, Integer> entry : nodeMap.entrySet()) {
            ASTNode node = entry.getKey();
            Integer idx = entry.getValue();

            if (usedIndices.contains(idx)) {
                String label = infoExtractor.apply(node);
                nodeLabels.put(idx, label);
            }
        }

        return nodeLabels;
    }

    private List<Pair<ASTNode>> getDataDependencies() {
        if (astRoot instanceof Program programRoot) {
            return programRoot.getActorDefinitionList().getDefinitions()
                .stream()
                .flatMap(this::getDataDependencies)
                .toList();
        }
        else if (astRoot instanceof ActorDefinition actorDefinition) {
            return getDataDependencies(actorDefinition).toList();
        }
        else {
            throw new UnsupportedOperationException("Can only extract data dependencies from programs and actors!");
        }
    }

    private Stream<Pair<ASTNode>> getDataDependencies(final ActorDefinition actor) {
        ControlFlowGraphVisitor v = new ControlFlowGraphVisitor(program, actor);
        actor.accept(v);
        ControlFlowGraph cfg = v.getControlFlowGraph();
        DataDependenceGraph ddg = new DataDependenceGraph(cfg);

        return ddg.getEdges().stream().map(edge -> Pair.of(edge.source().getASTNode(), edge.target().getASTNode()));
    }
}
