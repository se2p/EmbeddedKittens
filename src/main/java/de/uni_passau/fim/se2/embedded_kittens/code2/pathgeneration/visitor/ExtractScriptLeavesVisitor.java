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
package de.uni_passau.fim.se2.embedded_kittens.code2.pathgeneration.visitor;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTLeaf;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

public class ExtractScriptLeavesVisitor extends ExtractLeavesVisitor<ScriptEntity> {

    private boolean insideScript = false;

    public ExtractScriptLeavesVisitor(final ProcedureDefinitionNameMapping procedures) {
        super(procedures);
    }

    @Override
    public void visit(ProcedureDefinition node) {
        visitScript(node);
    }

    @Override
    public void visit(Script node) {
        visitScript(node);
    }

    private void visitScript(final ScriptEntity script) {
        insideScript = true;

        visitChildren(script);
        saveLeaves(script);

        insideScript = false;
    }

    @Override
    public void visit(ASTNode node) {
        if (insideScript && node instanceof ASTLeaf leaf && !AstNodeUtil.isMetadata(node)) {
            addLeaf(leaf);
        }
        else {
            visitChildren(node);
        }
    }
}
