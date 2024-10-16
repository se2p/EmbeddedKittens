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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

public final class ProgramPathGenerator extends PathGenerator {

    private final Map<ActorDefinition, List<ASTNode>> leavesMap;

    public ProgramPathGenerator(
        Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites,
        PathFormatOptions pathFormatOptions, ProgramRelationFactory programRelationFactory
    ) {
        super(program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions, programRelationFactory);
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
        return generatePathsWholeProgram().stream().toList();
    }

    private Optional<ProgramFeatures> generatePathsWholeProgram() {
        List<ASTNode> leaves = leavesMap.values().stream().flatMap(Collection::stream).toList();
        final ProgramFeatures programFeatures = super.getProgramFeatures("program", leaves);
        return Optional.of(programFeatures).filter(features -> !features.isEmpty());
    }

    @Override
    protected Stream<ASTNode> getLeaves() {
        return leavesMap.values().stream().flatMap(Collection::stream);
    }
}
