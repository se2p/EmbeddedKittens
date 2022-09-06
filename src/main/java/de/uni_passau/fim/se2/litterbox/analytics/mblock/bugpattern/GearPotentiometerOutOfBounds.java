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
package de.uni_passau.fim.se2.litterbox.analytics.mblock.bugpattern;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.Potentiometer;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class GearPotentiometerOutOfBounds extends AbstractRobotFinder {
    private static final String NAME = "gear_potentiometer_out_of_bounds";
    private static final int GEAR_MAX = 100;
    private static final int GEAR_MIN = 0;
    private boolean insideComparison;
    private boolean hasGear;
    private double sensorValue;
    private boolean setValue;
    private boolean firstHasGear;
    private boolean secondHasGear;
    private boolean visitFirst;
    private boolean visitSecond;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(Equals node) {
        insideComparison = true;
        setValue = false;
        visitChildren(node);
        if (setValue && hasGear) {
            if (sensorValue > GEAR_MAX || sensorValue < GEAR_MIN) {
                addIssue(node, IssueSeverity.LOW);
            } else if ((sensorValue % 1) != 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasGear = false;
    }

    @Override
    public void visit(BiggerThan node) {
        visitComp(node);
        if (setValue && hasGear) {
            if ((firstHasGear && sensorValue >= GEAR_MAX) || (secondHasGear && sensorValue <= GEAR_MIN)) {
                addIssue(node, IssueSeverity.LOW);
            } else if ((sensorValue % 1) != 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasGear = false;
        firstHasGear = false;
        secondHasGear = false;
    }

    @Override
    public void visit(LessThan node) {
        visitComp(node);
        if (setValue && hasGear) {
            if ((firstHasGear && sensorValue <= GEAR_MIN) || (secondHasGear && sensorValue >= GEAR_MAX)) {
                addIssue(node, IssueSeverity.LOW);
            } else if ((sensorValue % 1) != 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasGear = false;
        firstHasGear = false;
        secondHasGear = false;
    }

    private void visitComp(BinaryExpression node) {
        insideComparison = true;
        setValue = false;
        visitFirst = true;
        node.getOperand1().accept(this);
        visitFirst = false;
        visitSecond = true;
        node.getOperand2().accept(this);
        visitSecond = false;
    }

    @Override
    public void visit(Potentiometer node) {
        if (insideComparison) {
            hasGear = true;
        }
        if (visitFirst) {
            firstHasGear = true;
        } else if (visitSecond) {
            secondHasGear = true;
        }
    }

    @Override
    public void visit(NumberLiteral node) {
        if (insideComparison && !setValue) {
            setValue = true;
            sensorValue = node.getValue();
        }
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }
}
