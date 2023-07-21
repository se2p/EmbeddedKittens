/*
 * Copyright (C) 2019-2022 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.analytics.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.type.BooleanType;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ArgumentInfo;

public class IllegalParameterRefactor extends AbstractIssueFinder {
    public static final String NAME = "illegal_parameter_refactor";
    private ArgumentInfo[] currentArguments;
    private boolean insideProcedure;

    @Override
    public void visit(Script node) {
        //NOP should not be detected in Scripts
    }

    private void checkBool(BoolExpr boolExpr) {
        if (boolExpr instanceof AsBool asBool && asBool.getOperand1() instanceof Parameter ident) {
            for (ArgumentInfo currentArgument : currentArguments) {
                if (currentArgument.getName().equals(ident.getName().getName())
                        && !(currentArgument.getType() instanceof BooleanType)) {
                    addIssue(ident, ident.getMetadata(), IssueSeverity.MEDIUM);
                }
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentArguments = procMap.get(node.getIdent()).getArguments();
        super.visit(node);
        insideProcedure = false;
    }

    @Override
    public void visit(IfElseStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        visitChildren(node);
    }

    @Override
    public void visit(IfThenStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        if (insideProcedure) {
            checkBool(node.getUntil());
        }
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        if (insideProcedure) {
            checkBool(node.getBoolExpr());
        }
        visitChildren(node);
    }

    @Override
    public void visit(Not node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
        }
        visitChildren(node);
    }

    @Override
    public void visit(And node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        visitChildren(node);
    }

    @Override
    public void visit(Or node) {
        if (insideProcedure) {
            checkBool(node.getOperand1());
            checkBool(node.getOperand2());
        }
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
