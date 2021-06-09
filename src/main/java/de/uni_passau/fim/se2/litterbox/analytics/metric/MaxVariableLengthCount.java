/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MaxVariableLengthCount<T extends ASTNode> implements ScratchVisitor, MetricExtractor<T> {
    public static final String NAME = "max_variable_length_count";
    private boolean insideScript = false;
    private boolean insideProcedure = false;
    private List<String> variables;


    @Override
    public void visit(Variable node) {
        if (insideScript || insideProcedure) {
            this.variables.add(node.getName().getName());
        }
    }

    @Override
    public void visit(ScratchList node) {
        if (insideScript || insideProcedure) {
            this.variables.add(node.getName().getName());
        }
    }

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        double count = 0;
        this.variables = new ArrayList<>();
        insideProcedure = false;
        insideScript = false;
        node.accept(this);
        count = getMaxVariableLengthCount();
        return count;
    }

    private double getMaxVariableLengthCount() {
        List<String> allVariables = getVariables();
        Set<String> variables = new HashSet<String>(allVariables);
        double maxVarLen = 0;
        for (String var : variables) { // loop through the list of strings
            if (maxVarLen < var.length()) {
                maxVarLen = var.length();
            }
        }
        return maxVarLen;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        insideScript = false;
        visitChildren(node);
        insideProcedure = false;
    }

    @Override
    public void visit(Script node) {
        insideScript = true;
        insideProcedure = false;
        visitChildren(node);
        insideScript = false;
    }

    @Override
    public void visit(Stmt node) {
        if (!(insideProcedure || insideScript)) {
            return;
        }
        visitChildren(node);
    }

    public List<String> getVariables() {
        return this.variables;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
