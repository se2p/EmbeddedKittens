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
package de.uni_passau.fim.se2.litterbox.analytics.codeperfumes;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.UnspecifiedBoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;

import java.util.ArrayList;
import java.util.List;

public class WaitingCheckToStop extends AbstractIssueFinder {
    public static final String NAME = "waiting_check_stop";

    @Override
    public void visit(Script node) {
        currentScript = node;
        currentProcedure = null;
        if (node.getStmtList().getStmts().size() >= 2) {
            checkStmts(node.getStmtList().getStmts());
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentScript = null;
        currentProcedure = node;
        if (node.getStmtList().getStmts().size() >= 2) {
            checkStmts(node.getStmtList().getStmts());
        }
    }

    private void checkStmts(List<Stmt> stmts) {
        boolean hasWait = false;
        WaitUntil wait = null;
        for (int i = 0; i < stmts.size() - 1 && !hasWait; i++) {
            Stmt stmt = stmts.get(i);
            if (stmt instanceof WaitUntil waitUntil) {
                if (!(waitUntil.getUntil() instanceof UnspecifiedBoolExpr)) {
                    hasWait = true;
                    wait = waitUntil;
                }
            }
        }
        if (hasWait && stmts.get(stmts.size() - 1) instanceof StopAll) {
            List<ASTNode> foundIssue = new ArrayList<>();
            foundIssue.add(wait);
            foundIssue.add(stmts.get(stmts.size() - 1));
            MultiBlockIssue issue;
            if (currentProcedure == null) {
                issue = new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, currentScript, foundIssue, wait.getMetadata(), new Hint(NAME));
            } else {
                issue = new MultiBlockIssue(this, IssueSeverity.MEDIUM, program, currentActor, currentProcedure, foundIssue, wait.getMetadata(), new Hint(NAME));
            }
            addIssue(issue);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.PERFUME;
    }
}
