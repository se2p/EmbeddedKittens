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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.AbstractIssueFinder;
import de.uni_passau.fim.se2.litterbox.analytics.Hint;
import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.ReceptionOfMessage;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.utils.IssueTranslator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MiddleMan extends AbstractIssueFinder {

    private static final String NAME = "middle_man";
    private static final String BROADCAST_HINT = "middle_man_broadcast";
    private static final String PROCEDURE_HINT = "middle_man_procedure";

    @Override
    public void visit(Script script) {
        currentProcedure = null;
        currentScript = script;
        Event event = script.getEvent();
        if (event instanceof ReceptionOfMessage receptionOfMessage) {
            List<Stmt> stmts = script.getStmtList().getStmts();
            if (stmts.size() == 1 && (stmts.get(0) instanceof Broadcast || stmts.get(0) instanceof BroadcastAndWait)) {
                Hint hint = new Hint(BROADCAST_HINT);
                String message = ((StringLiteral) (receptionOfMessage.getMsg()).getMessage()).getText();
                hint.setParameter(Hint.HINT_MESSAGE_MIDDLE, message);
                Stmt broadcast = stmts.get(0);
                StringExpr stringExpr;
                if (broadcast instanceof Broadcast normalBroadcast) {
                    stringExpr = normalBroadcast.getMessage().getMessage();
                } else {
                    stringExpr = ((BroadcastAndWait) broadcast).getMessage().getMessage();
                }
                if (stringExpr instanceof StringLiteral stringLiteral) {
                    hint.setParameter(Hint.HINT_BLOCKNAME_FINAL, stringLiteral.getText());
                } else {
                    hint.setParameter(Hint.HINT_BLOCKNAME_FINAL, IssueTranslator.getInstance().getInfo("message"));
                }
                addIssue(event, event.getMetadata(), IssueSeverity.MEDIUM, hint);
            }
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        currentScript = null;
        List<Stmt> stmts = node.getStmtList().getStmts();
        if (stmts.size() == 1 && stmts.get(0) instanceof CallStmt callStmt) {
            if (!callStmt.getIdent().getName().equals(node.getIdent().getName())) {
                Hint hint = new Hint(PROCEDURE_HINT);
                hint.setParameter(Hint.HINT_BLOCKNAME_MIDDLE, node.getIdent().getName());
                hint.setParameter(Hint.HINT_BLOCKNAME_FINAL, callStmt.getIdent().getName());
                addIssue(node, node.getMetadata().getDefinition(), IssueSeverity.MEDIUM, hint);
            }
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Collection<String> getHintKeys() {
        List<String> keys = new ArrayList<>();
        keys.add(BROADCAST_HINT);
        keys.add(PROCEDURE_HINT);
        return keys;
    }
}
