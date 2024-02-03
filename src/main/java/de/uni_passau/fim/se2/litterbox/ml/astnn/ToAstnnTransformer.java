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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.AstnnAstNodeFactory;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.AstnnNode;
import de.uni_passau.fim.se2.litterbox.ml.shared.ActorNameNormalizer;

import java.util.List;
import java.util.stream.Stream;

class ToAstnnTransformer {
    private final ActorNameNormalizer actorNameNormalizer;
    private final boolean abstractTokens;

    ToAstnnTransformer(final ActorNameNormalizer actorNameNormalizer, final boolean abstractTokens) {
        this.actorNameNormalizer = actorNameNormalizer;
        this.abstractTokens = abstractTokens;
    }

    public AstnnNode transform(final Program program, boolean includeStage, boolean includeDefaultSprites) {
        final Stream<ActorDefinition> actors;
        if (includeDefaultSprites) {
            actors = AstNodeUtil.getActors(program, includeStage);
        } else {
            actors = AstNodeUtil.getActorsWithoutDefaultSprites(program, includeStage);
        }

        final List<AstnnNode> nodes = actors
                .map(actor -> transform(program, actor))
                .toList();
        return AstnnAstNodeFactory.program(program.getIdent().getName(), nodes);
    }

    public AstnnNode transform(final Program program, final ASTNode node) {
        final AstnnTransformationVisitor visitor = new AstnnTransformationVisitor(
                program.getProcedureMapping(), actorNameNormalizer, abstractTokens
        );
        node.accept(visitor);
        return visitor.getResult();
    }
}
