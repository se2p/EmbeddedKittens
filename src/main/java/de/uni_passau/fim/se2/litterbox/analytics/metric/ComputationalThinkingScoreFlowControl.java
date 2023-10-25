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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatForeverStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.UntilStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public class ComputationalThinkingScoreFlowControl implements MetricExtractor<Program>, ScratchVisitor {
    public static final String NAME = "ct_score_flow_control";

    private int score = 0;

    @Override
    public double calculateMetric(Program program) {
        score = 0;
        program.accept(this);
        return score;
    }

    @Override
    public void visit(StmtList node) {
        if (node.getStmts().size() > 1) {
            score = Math.max(1, score);
        }
        visitChildren(node);
    }

    @Override
    public void visit(RepeatTimesStmt node) {
        score = Math.max(2, score);
        visitChildren(node);
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        score = Math.max(2, score);
        visitChildren(node);
    }

    @Override
    public void visit(UntilStmt node) {
        // TODO: Why not wait-until?
        score = 3;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
