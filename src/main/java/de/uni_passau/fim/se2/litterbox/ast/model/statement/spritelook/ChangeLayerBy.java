/*
 * Copyright (C) 2019 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook;

import de.uni_passau.fim.se2.litterbox.ast.model.AbstractNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ChangeLayerBy extends AbstractNode implements SpriteLookStmt {

    private final NumExpr num;
    private final ForwardBackwardChoice forwardBackwardChoice;

    public ChangeLayerBy(NumExpr num, ForwardBackwardChoice forwardBackwardChoice) {
        super(num, forwardBackwardChoice);
        this.num = Preconditions.checkNotNull(num);
        this.forwardBackwardChoice = Preconditions.checkNotNull(forwardBackwardChoice);
    }

    public NumExpr getNum() {
        return num;
    }

    public ForwardBackwardChoice getForwardBackwardChoice() {
        return forwardBackwardChoice;
    }

    @Override
    public void accept(ScratchVisitor visitor) {
        visitor.visit(this);
    }
}