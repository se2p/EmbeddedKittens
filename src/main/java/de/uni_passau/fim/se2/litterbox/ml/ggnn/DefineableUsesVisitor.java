/*
 * Copyright (C) 2021-2024 LitterBox-ML contributors
 *
 * This file is part of LitterBox-ML.
 *
 * LitterBox-ML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox-ML is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox-ML. If not, see <http://www.gnu.org/licenses/>.
 */
package de.uni_passau.fim.se2.litterbox.ml.ggnn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.LocalIdentifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.HideVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.ShowVariable;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.ChangeVariableBy;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.SetVariableTo;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.list.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.DataExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

/**
 * Finds all {@link Variable Variables} and attributes that are used in the AST.
 */
class DefineableUsesVisitor implements ScratchVisitor {

    private final Map<String, List<Expression>> variables = new HashMap<>();
    private final List<ASTNode> attributes = new ArrayList<>();

    public static DefineableUsesVisitor visitNode(final ASTNode node) {
        DefineableUsesVisitor v = new DefineableUsesVisitor();
        node.accept(v);
        return v;
    }

    public Map<String, List<Expression>> getVariables() {
        return variables;
    }

    public List<ASTNode> getAttributes() {
        return attributes;
    }

    private void addVariable(final DataExpr variable) {
        variables.compute(variable.getName().getName(), (name, vars) -> {
            if (vars == null) {
                vars = new ArrayList<>();
            }
            vars.add(variable);
            return vars;
        });
    }

    private void addVariable(final Identifier identifier) {
        variables.compute(getName(identifier), (name, vars) -> {
            if (vars == null) {
                vars = new ArrayList<>();
            }
            vars.add(identifier);
            return vars;
        });
    }

    private String getName(final Identifier identifier) {
        if (identifier instanceof Qualified q) {
            return q.getFirst().getName();
        }
        else if (identifier instanceof LocalIdentifier l) {
            return l.getName();
        }
        else {
            throw new IllegalArgumentException(
                "Unknown identifier type '"
                    + identifier.getClass().getName()
                    + "'. Cannot continue data dependency edge computation!"
            );
        }
    }

    @Override
    public void visit(Variable node) {
        addVariable(node);
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(SetVariableTo node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(ChangeVariableBy node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(ShowVariable node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(HideVariable node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    // region lists

    @Override
    public void visit(ScratchList node) {
        addVariable(node);
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(AddTo node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(DeleteOf node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(DeleteAllOf node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(InsertAt node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(ReplaceItem node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(ItemOfVariable node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(IndexOf node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(LengthOfVar node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(ShowList node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    @Override
    public void visit(HideList node) {
        addVariable(node.getIdentifier());
        ScratchVisitor.super.visit(node);
    }

    // endregion lists

    @Override
    public void visit(Backdrop node) {
        attributes.add(node);
    }

    @Override
    public void visit(Costume node) {
        attributes.add(node);
    }

    @Override
    public void visit(Direction node) {
        attributes.add(node);
    }

    @Override
    public void visit(Loudness node) {
        attributes.add(node);
    }

    @Override
    public void visit(PositionX node) {
        attributes.add(node);
    }

    @Override
    public void visit(PositionY node) {
        attributes.add(node);
    }

    @Override
    public void visit(Size node) {
        attributes.add(node);
    }

    @Override
    public void visit(Volume node) {
        attributes.add(node);
    }

    // not directly attributes but external values:
    // included as they can be used like variables and attributes within expressions and therefore are of interest
    // especially for COMPUTED_FROM and GUARDED_BY edges

    @Override
    public void visit(Answer node) {
        attributes.add(node);
    }

    @Override
    public void visit(AttributeOf node) {
        attributes.add(node);
    }

    @Override
    public void visit(Current node) {
        attributes.add(node);
    }

    @Override
    public void visit(DaysSince2000 node) {
        attributes.add(node);
    }

    @Override
    public void visit(DistanceTo node) {
        attributes.add(node);
    }

    @Override
    public void visit(MouseX node) {
        attributes.add(node);
    }

    @Override
    public void visit(MouseY node) {
        attributes.add(node);
    }

    @Override
    public void visit(Timer node) {
        attributes.add(node);
    }

    @Override
    public void visit(Username node) {
        attributes.add(node);
    }
}
