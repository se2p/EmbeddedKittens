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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.ScriptEntity;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BoolExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.ColorTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.SpriteTouchingColor;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Touching;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.*;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.MergeDoubleIf;

import java.util.List;

public class DoubleIf extends AbstractIssueFinder {

    private static final String NAME = "double_if";
    private boolean movedInIf = false;
    private boolean checkingForMove = false;

    @Override
    public void visit(StmtList node) {
        if (!checkingForMove) {
            movedInIf = false;
            final List<Stmt> stmts = node.getStmts();

            IfStmt lastIf = null;
            for (Stmt s : stmts) {
                if (s instanceof IfStmt ifStmt) {
                    BoolExpr condition = ifStmt.getBoolExpr();
                    if (lastIf != null) {
                        checkingForMove = true;
                        visitChildren(node);
                        checkingForMove = false;
                        boolean touchingCondition = condition instanceof Touching
                                || condition instanceof ColorTouchingColor || condition instanceof SpriteTouchingColor;

                        if (lastIf.getBoolExpr().equals(condition) && !(movedInIf && touchingCondition)) {
                            MergeDoubleIf merge = new MergeDoubleIf(lastIf, ifStmt);
                            ScriptEntity refactoring = merge.apply(getCurrentScriptEntity());

                            IssueBuilder builder = prepareIssueBuilder(s)
                                    .withSeverity(IssueSeverity.LOW)
                                    .withHint(new Hint(getName()))
                                    .withRefactoring(refactoring);

                            addIssue(builder);
                        }
                    }
                    lastIf = ifStmt;
                } else {
                    // even if we already have a condition from an ifstmt before, it only counts if a second ifstmt
                    // follows directly after the first.
                    lastIf = null;
                }
            }
        }

        visitChildren(node);
    }

    @Override
    public void visit(ChangeXBy node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(ChangeYBy node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(GlideSecsTo node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(GlideSecsToXY node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(GoToPos node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(GoToPosXY node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(MoveSteps node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(SetXTo node) {
        if (checkingForMove) {
            movedInIf = true;
        }
    }

    @Override
    public void visit(SetYTo node) {
        if (checkingForMove) {
            movedInIf = true;
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
}
