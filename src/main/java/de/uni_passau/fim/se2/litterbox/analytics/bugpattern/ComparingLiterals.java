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

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.analytics.hint.ComparingLiteralsHintFactory;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.ComparableExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Equals;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.WaitUntil;

import java.util.Collection;

/**
 * Reporter blocks are used to evaluate the truth value of certain expressions.
 * Not only is it possible to compare literals to variables or the results of other reporter blocks, literals can
 * also be compared to literals.
 * Since this will lead to the same result in each execution this construct is unnecessary and can obscure the fact
 * that certain blocks will never or always be executed.
 */
public class ComparingLiterals extends AbstractIssueFinder {

    public static final String NAME = "comparing_literals";
    private boolean inWait;

    @Override
    public void visit(Equals node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint;
            ASTNode parent = node.getParentNode();

            if (node.getOperand1() instanceof NumberLiteral operand1 && node.getOperand2() instanceof NumberLiteral operand2) {
                double text1 = operand1.getValue();
                double text2 = operand2.getValue();
                hint = ComparingLiteralsHintFactory.generateHint(text1 == text2, inWait, parent, null, null, node, currentActor, program, currentProcedure, currentScript);
            } else if (node.getOperand1() instanceof StringLiteral operand1 && node.getOperand2() instanceof StringLiteral operand2) {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());

                int result = text1.compareTo(text2);
                hint = ComparingLiteralsHintFactory.generateHint(result == 0, inWait, parent, operand1.getText(), operand2.getText(), node, currentActor, program, currentProcedure, currentScript);
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());

                int result = text1.compareTo(text2);
                hint = ComparingLiteralsHintFactory.generateHint(result == 0, inWait, parent, possibleVariableName(node.getOperand1(), node.getOperand2()), null, node, currentActor, program, currentProcedure, currentScript);
            }

            addIssue(node, node.getMetadata(), IssueSeverity.HIGH, hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(WaitUntil node) {
        inWait = true;
        super.visit(node);
        inWait = false;
    }

    private String possibleVariableName(ComparableExpr node1, ComparableExpr node2) {
        if (node1 instanceof StringLiteral stringLiteral) {
            return stringLiteral.getText();
        } else {
            return ((StringLiteral) node2).getText();
        }
    }

    private String getLiteralValue(ComparableExpr node) {
        String text1;
        if (node instanceof StringLiteral stringLiteral) {
            text1 = stringLiteral.getText();
        } else {
            text1 = String.valueOf(((NumberLiteral) node).getValue());
        }
        return text1;
    }

    @Override
    public void visit(LessThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint;
            ASTNode parent = node.getParentNode();

            if (node.getOperand1() instanceof NumberLiteral operand1 && node.getOperand2() instanceof NumberLiteral operand2) {
                double text1 = operand1.getValue();
                double text2 = operand2.getValue();
                hint = ComparingLiteralsHintFactory.generateHint(text1 < text2, inWait, parent, null, null, node, currentActor, program, currentProcedure, currentScript);
            } else if (node.getOperand1() instanceof StringLiteral operand1 && node.getOperand2() instanceof StringLiteral operand2) {
                String text1 = getLiteralValue(operand1);
                String text2 = getLiteralValue(operand2);
                int result = text1.compareTo(text2);
                hint = ComparingLiteralsHintFactory.generateHint(result < 0, inWait, parent, operand1.getText(), operand2.getText(), node, currentActor, program, currentProcedure, currentScript);
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());
                int result = text1.compareTo(text2);
                hint = ComparingLiteralsHintFactory.generateHint(result < 0, inWait, parent, possibleVariableName(node.getOperand1(), node.getOperand2()), null, node, currentActor, program, currentProcedure, currentScript);
            }
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH, hint);
        }
        visitChildren(node);
    }

    @Override
    public void visit(BiggerThan node) {
        if ((node.getOperand1() instanceof StringLiteral || node.getOperand1() instanceof NumberLiteral)
                && (node.getOperand2() instanceof StringLiteral || node.getOperand2() instanceof NumberLiteral)) {
            Hint hint;
            ASTNode parent = node.getParentNode();

            if (node.getOperand1() instanceof NumberLiteral operand1 && node.getOperand2() instanceof NumberLiteral operand2) {
                double text1 = operand1.getValue();
                double text2 = operand2.getValue();
                hint = ComparingLiteralsHintFactory.generateHint(text1 > text2, inWait, parent, null, null, node, currentActor, program, currentProcedure, currentScript);
            } else if (node.getOperand1() instanceof StringLiteral operand1 && node.getOperand2() instanceof StringLiteral operand2) {
                String text1 = getLiteralValue(operand1);
                String text2 = getLiteralValue(operand2);
                int result = text1.compareTo(text2);

                hint = ComparingLiteralsHintFactory.generateHint(result > 0, inWait, parent, operand1.getText(), operand2.getText(), node, currentActor, program, currentProcedure, currentScript);
            } else {
                String text1 = getLiteralValue(node.getOperand1());
                String text2 = getLiteralValue(node.getOperand2());

                int result = text1.compareTo(text2);
                hint = ComparingLiteralsHintFactory.generateHint(result > 0, inWait, parent, possibleVariableName(node.getOperand1(), node.getOperand2()), null, node, currentActor, program, currentProcedure, currentScript);
            }
            addIssue(node, node.getMetadata(), IssueSeverity.HIGH, hint);
        }
        visitChildren(node);
    }

    @Override
    public boolean isSubsumedBy(Issue theIssue, Issue other) {
        if (theIssue.getFinder() != this) {
            return super.isSubsumedBy(theIssue, other);
        }

        if (other.getFinder() instanceof VariableAsLiteral) {
            return theIssue.getCodeLocation().equals(other.getCodeLocation());
        }

        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.BUG;
    }

    @Override
    public Collection<String> getHintKeys() {
        return ComparingLiteralsHintFactory.getHintKeys();
    }
}
