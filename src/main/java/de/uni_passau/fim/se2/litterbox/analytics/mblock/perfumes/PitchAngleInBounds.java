package de.uni_passau.fim.se2.litterbox.analytics.mblock.perfumes;

import de.uni_passau.fim.se2.litterbox.analytics.IssueSeverity;
import de.uni_passau.fim.se2.litterbox.analytics.IssueType;
import de.uni_passau.fim.se2.litterbox.analytics.mblock.AbstractRobotFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.BinaryExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.BiggerThan;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.LessThan;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.mblock.expression.num.GyroPitchAngle;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;

public class PitchAngleInBounds extends AbstractRobotFinder {
    private static final String NAME = "pitch_angle_in_bounds";
    private static final int PITCH_ANGLE_MAX = 180;
    private static final int PITCH_ANGLE_MIN = -180;
    private boolean insideComparison;
    private boolean hasPitchAngle;
    private double sensorValue;
    private boolean setValue;
    private boolean firstHasPitchAngle;
    private boolean secondHasPitchAngle;
    private boolean visitFirst;
    private boolean visitSecond;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(BiggerThan node) {
        visitComp(node);
        if (setValue && hasPitchAngle) {
            if (((firstHasPitchAngle && sensorValue >= PITCH_ANGLE_MIN && sensorValue < PITCH_ANGLE_MAX)
                    || (secondHasPitchAngle && sensorValue <= PITCH_ANGLE_MAX && sensorValue > PITCH_ANGLE_MIN)) && (sensorValue % 1) == 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasPitchAngle = false;
        firstHasPitchAngle = false;
        secondHasPitchAngle = false;
    }

    @Override
    public void visit(LessThan node) {
        visitComp(node);
        if (setValue && hasPitchAngle) {
            if (((firstHasPitchAngle && sensorValue <= PITCH_ANGLE_MAX && sensorValue > PITCH_ANGLE_MIN)
                    || (secondHasPitchAngle && sensorValue >= PITCH_ANGLE_MIN && sensorValue < PITCH_ANGLE_MAX)) && (sensorValue % 1) == 0) {
                addIssue(node, IssueSeverity.LOW);
            }
        }
        insideComparison = false;
        hasPitchAngle = false;
        firstHasPitchAngle = false;
        secondHasPitchAngle = false;
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
    public void visit(GyroPitchAngle node) {
        if (insideComparison) {
            hasPitchAngle = true;
        }
        if (visitFirst) {
            firstHasPitchAngle = true;
        } else if (visitSecond) {
            secondHasPitchAngle = true;
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
        return IssueType.PERFUME;
    }
}
