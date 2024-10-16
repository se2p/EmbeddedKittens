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
package de.uni_passau.fim.se2.embedded_kittens.tokenizer;

import java.util.List;

import de.uni_passau.fim.se2.embedded_kittens.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;

public class StatementLevelTokenizer extends AbstractTokenizer {

    private StatementLevelTokenizer(
        final ProcedureDefinitionNameMapping procedureNameMapping,
        final boolean abstractTokens,
        final MaskingStrategy maskingStrategy
    ) {
        super(procedureNameMapping, abstractTokens, maskingStrategy);
    }

    public static List<String> tokenize(
        final Program program,
        final ASTNode node,
        final boolean abstractTokens,
        final MaskingStrategy maskingStrategy
    ) {
        return tokenize(program.getProcedureMapping(), node, abstractTokens, maskingStrategy);
    }

    private static List<String> tokenize(
        final ProcedureDefinitionNameMapping procedureNameMapping,
        final ASTNode node,
        final boolean abstractTokens,
        final MaskingStrategy maskingStrategy
    ) {
        final StatementLevelTokenizer v = new StatementLevelTokenizer(
            procedureNameMapping, abstractTokens, maskingStrategy
        );
        node.accept(v);
        return v.getTokens();
    }

    private void visitControlBlock(final ASTNode node, final Token opcode) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        }
        else {
            addToken(opcode);
            addToken(Token.BEGIN_SUBSTACK);
            visitChildren(node);
            addToken(Token.END_SUBSTACK);
        }
    }

    @Override
    protected void visit(final ASTNode node, final Token opcode) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        }
        else {
            addToken(opcode);
        }
    }

    private boolean shouldBeMasked(final ASTNode node) {
        final var maskingStrategy = getMaskingStrategy();
        return maskingStrategy instanceof MaskingStrategy.Block && maskingStrategy.shouldBeMasked(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        visitControlBlock(node, Token.CONTROL_REPEAT);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        visitControlBlock(node, Token.CONTROL_FOREVER);
    }

    @Override
    public void visit(IfThenStmt node) {
        visitControlBlock(node, Token.CONTROL_IF);
    }

    @Override
    public void visit(IfElseStmt node) {
        visitControlBlock(node, Token.CONTROL_IF_ELSE);
    }

    @Override
    public void visit(UntilStmt node) {
        visitControlBlock(node, Token.CONTROL_REPEAT_UNTIL);
    }

    @Override
    public void visit(StopAll node) {
        visitStop(node);
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        visitStop(node);
    }

    @Override
    public void visit(StopThisScript node) {
        visitStop(node);
    }

    private void visitStop(ASTNode node) {
        visit(node, Token.CONTROL_STOP);
    }
}
