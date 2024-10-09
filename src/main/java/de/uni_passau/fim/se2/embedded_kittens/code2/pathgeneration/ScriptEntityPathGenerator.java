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
import de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.visitor.ExtractScriptLeavesVisitor;
import de.uni_passau.fim.se2.embedded_kittens.util.NodeNameUtil;
import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

public final class ScriptEntityPathGenerator extends PathGenerator {

    private final Map<ScriptEntity, List<ASTNode>> leavesMap;

    public ScriptEntityPathGenerator(
        Program program, int maxPathLength, boolean includeStage, boolean includeDefaultSprites,
        PathFormatOptions pathFormatOptions, ProgramRelationFactory programRelationFactory
    ) {
        super(program, maxPathLength, includeStage, includeDefaultSprites, pathFormatOptions, programRelationFactory);

        Stream<ActorDefinition> sprites = AstNodeUtil.getActors(program, includeStage);
        this.leavesMap = Collections.unmodifiableMap(extractASTLeaves(sprites));
    }

    private Map<ScriptEntity, List<ASTNode>> extractASTLeaves(Stream<ActorDefinition> sprites) {
        ExtractScriptLeavesVisitor extractionVisitor = new ExtractScriptLeavesVisitor(program.getProcedureMapping());

        sprites.sequential().forEach(sprite -> {
            for (Script script : sprite.getScripts().getScriptList()) {
                script.accept(extractionVisitor);
            }

            for (ProcedureDefinition procedure : sprite.getProcedureDefinitionList().getList()) {
                procedure.accept(extractionVisitor);
            }
        });

        return extractionVisitor.getLeaves();
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        final List<ProgramFeatures> scriptFeatures = new ArrayList<>();

        for (final var entry : leavesMap.entrySet()) {
            final ScriptEntity script = entry.getKey();

            NodeNameUtil.getScriptEntityName(script).ifPresent(scriptName -> {
                final List<ASTNode> leaves = entry.getValue();
                final ProgramFeatures singleScriptFeatures = super.getProgramFeatures(scriptName, leaves);

                if (!singleScriptFeatures.isEmpty()) {
                    scriptFeatures.add(singleScriptFeatures);
                }
            });
        }

        return scriptFeatures;
    }

    @Override
    public Stream<ASTNode> getLeaves() {
        return leavesMap.values().stream().flatMap(Collection::stream);
    }
}
