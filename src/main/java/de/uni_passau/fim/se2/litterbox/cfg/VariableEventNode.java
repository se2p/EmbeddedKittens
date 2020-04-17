package de.uni_passau.fim.se2.litterbox.cfg;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.event.VariableAboveValue;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;

public class VariableEventNode extends CFGNode {

    private Variable variable;

    private NumExpr expression;

    private VariableAboveValue node;

    public VariableEventNode(VariableAboveValue node) {
        this.node = node;
        this.variable = node.getVariable();
        this.expression = node.getValue();
    }

    @Override
    public String toString() {
        return "Variable above value: "+variable.getUniqueName();
    }

    @Override
    public ASTNode getASTNode() {
        return node;
    }

    // TODO: Need equals/hashcode to support multiple events for the same variable+expression
}
