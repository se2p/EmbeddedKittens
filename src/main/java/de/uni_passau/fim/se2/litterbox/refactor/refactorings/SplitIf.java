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
package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/*
if A:
  B
  C

to

if A:
  B
if A:
  C
 */
public class SplitIf extends OnlyCodeCloneVisitor implements Refactoring {

    private final IfThenStmt ifThenStmt;
    private final Stmt splitPoint;
    private final IfThenStmt replacementIf1;
    private final IfThenStmt replacementIf2;
    public static final String NAME = "split_if";

    public SplitIf(IfThenStmt if1, Stmt splitPoint) {
        this.ifThenStmt = Preconditions.checkNotNull(if1);
        this.splitPoint = Preconditions.checkNotNull(splitPoint);
        Preconditions.checkArgument(ifThenStmt.getThenStmts().getNumberOfStatements() > 1);

        List<Stmt> remainingStatements = apply(if1.getThenStmts()).getStmts();
        List<Stmt> initialStatements   = apply(if1.getThenStmts()).getStmts();

        Iterator<Stmt> originalIterator  = if1.getThenStmts().getStmts().iterator();
        Iterator<Stmt> initialIterator   = initialStatements.iterator();
        Iterator<Stmt> remainingIterator = remainingStatements.iterator();

        boolean inInitial = true;
        while (originalIterator.hasNext()) {
            if (originalIterator.next() == splitPoint) {
                inInitial = false;
            }
            initialIterator.next();
            remainingIterator.next();

            if (inInitial) {
                remainingIterator.remove();
            } else {
                initialIterator.remove();
            }
        }

        StmtList subStatements1 = new StmtList(initialStatements);
        StmtList subStatements2 = new StmtList(remainingStatements);

        replacementIf1 = new IfThenStmt(apply(if1.getBoolExpr()), subStatements1, apply(if1.getMetadata()));
        replacementIf2 = new IfThenStmt(apply(if1.getBoolExpr()), subStatements2, apply(if1.getMetadata()));
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(new StatementReplacementVisitor(ifThenStmt, replacementIf1, replacementIf2));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Split if:" + System.lineSeparator() + ifThenStmt.getScratchBlocks() + System.lineSeparator()
                + "Replacement if 1:" + System.lineSeparator() + replacementIf1.getScratchBlocks() +  System.lineSeparator()
                + "Replacement if 2:" + System.lineSeparator() + replacementIf2.getScratchBlocks() +  System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SplitIf)) return false;
        SplitIf splitIf = (SplitIf) o;
        return Objects.equals(ifThenStmt, splitIf.ifThenStmt) && Objects.equals(splitPoint, splitIf.splitPoint) && Objects.equals(replacementIf1, splitIf.replacementIf1) && Objects.equals(replacementIf2, splitIf.replacementIf2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifThenStmt, splitPoint, replacementIf1, replacementIf2);
    }
}
