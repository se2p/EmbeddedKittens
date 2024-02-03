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
package de.uni_passau.fim.se2.litterbox.ml.astnn;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.AstnnAstNodeFactory;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.AstnnNode;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.NodeType;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.StatementTreeSequence;
import de.uni_passau.fim.se2.litterbox.ml.shared.ActorNameNormalizer;

class StatementTreeSequenceBuilder {

    private final ActorNameNormalizer actorNameNormalizer;
    private final ToAstnnTransformer toAstnnTransformer;

    StatementTreeSequenceBuilder(final ActorNameNormalizer actorNameNormalizer, final boolean abstractTokens) {
        this.actorNameNormalizer = actorNameNormalizer;
        this.toAstnnTransformer = new ToAstnnTransformer(actorNameNormalizer, abstractTokens);
    }

    public StatementTreeSequence build(
        final Program program, boolean includeStage, boolean includeDefaultSprites
    ) {
        final String label = program.getIdent().getName();
        final AstnnNode node = toAstnnTransformer.transform(program, includeStage, includeDefaultSprites);
        final List<AstnnNode> statementTrees = build(node);

        return new StatementTreeSequence(label, label, statementTrees);
    }

    public Stream<StatementTreeSequence> buildPerActor(
        final Program program, boolean includeStage, boolean includeDefaultSprites
    ) {
        final Stream<ActorDefinition> actors;
        if (includeDefaultSprites) {
            actors = AstNodeUtil.getActors(program, includeStage);
        }
        else {
            actors = AstNodeUtil.getActorsWithoutDefaultSprites(program, includeStage);
        }

        return actors.map(actor -> build(program, actor));
    }

    /**
     * Builds the statement tree sequence for the given actor node.
     *
     * @param program The program the actor belongs to. Required to be able to resolve custom procedure names.
     * @param actor   The actor for which the statement trees should be generated.
     * @return The statement tree sequence for the actor.
     */
    public StatementTreeSequence build(final Program program, final ActorDefinition actor) {
        String originalLabel = actor.getIdent().getName();
        if (originalLabel.isEmpty()) {
            originalLabel = NodeType.EMPTY_STRING.toString();
        }

        final String label = actorNameNormalizer.normalizeName(actor).orElse(NodeType.EMPTY_STRING.toString());
        final AstnnNode node = toAstnnTransformer.transform(program, actor);
        final List<AstnnNode> statementTrees = build(node);

        return new StatementTreeSequence(originalLabel, label, statementTrees);
    }

    private static List<AstnnNode> build(final AstnnNode rootNode) {
        final List<AstnnNode> list = new ArrayList<>();
        build(rootNode, list);
        return list;
    }

    private static void build(final AstnnNode node, final List<AstnnNode> sequence) {
        if (node.isStatement()) {
            sequence.add(node.asStatementTree());
        }
        for (AstnnNode c : node.children()) {
            build(c, sequence);
        }
        if (node.hasBlock()) {
            sequence.add(AstnnAstNodeFactory.blockEnd());
        }
    }
}
