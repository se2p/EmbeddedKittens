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
package de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration;

import java.util.*;
import java.util.stream.Stream;

import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.program_relation.ProgramRelationFactory;
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.visitor.ExtractSpriteLeavesVisitor;
import de.uni_passau.fim.se2.embedded_kittens.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.embedded_kittens.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public final class SpritePathGenerator extends PathGenerator {

    private final ActorNameNormalizer actorNameNormalizer;
    private final Map<ActorDefinition, List<ASTNode>> leavesMap;

    public SpritePathGenerator(
        Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites,
        PathFormatOptions pathFormatOptions, ProgramRelationFactory programRelationFactory,
        ActorNameNormalizer actorNameNormalizer
    ) {
        super(program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions, programRelationFactory);

        this.actorNameNormalizer = actorNameNormalizer;
        this.leavesMap = Collections.unmodifiableMap(extractASTLeaves());
    }

    private Map<ActorDefinition, List<ASTNode>> extractASTLeaves() {
        ExtractSpriteLeavesVisitor spriteVisitor = new ExtractSpriteLeavesVisitor(
            program.getProcedureMapping(), includeStage
        );
        program.accept(spriteVisitor);
        return spriteVisitor.getLeaves();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        final List<ProgramFeatures> spriteFeatures = new ArrayList<>();
        for (final Map.Entry<ActorDefinition, List<ASTNode>> entry : leavesMap.entrySet()) {
            final ActorDefinition actor = entry.getKey();
            final List<ASTNode> leaves = entry.getValue();
            final Optional<ProgramFeatures> singleSpriteFeatures = generatePathsForSprite(actor, leaves);
            singleSpriteFeatures.filter(features -> !features.isEmpty()).ifPresent(spriteFeatures::add);
        }
        return spriteFeatures;
    }

    private Optional<ProgramFeatures> generatePathsForSprite(final ActorDefinition sprite, final List<ASTNode> leaves) {
        final Optional<String> spriteName = actorNameNormalizer.normalizeName(sprite);
        return spriteName
            .filter(name -> includeDefaultSprites || !NodeNameUtil.hasDefaultName(sprite))
            .map(name -> getProgramFeatures(name, leaves));
    }

    @Override
    public Stream<ASTNode> getLeaves() {
        return leavesMap.values().stream().flatMap(Collection::stream);
    }
}
