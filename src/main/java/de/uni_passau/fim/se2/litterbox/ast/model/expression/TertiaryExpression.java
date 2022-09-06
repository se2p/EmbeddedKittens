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
package de.uni_passau.fim.se2.litterbox.ast.model.expression;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

public abstract class TertiaryExpression<A extends ASTNode, B extends ASTNode, C extends ASTNode> extends AbstractNode {

    private final A operand1;
    private final B operand2;
    private final C operand3;
    private final BlockMetadata metadata;

    protected TertiaryExpression(A operand1, B operand2, C operand3, BlockMetadata metadata) {
        super(operand1, operand2, operand3, metadata);
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operand3 = operand3;
        this.metadata = metadata;
    }

    public A getOperand1() {
        return operand1;
    }

    public B getOperand2() {
        return operand2;
    }

    public C getOperand3() {
        return operand3;
    }

    @Override
    public BlockMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}
