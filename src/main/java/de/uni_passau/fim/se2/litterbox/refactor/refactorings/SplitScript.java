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

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.OnlyCodeCloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class SplitScript extends OnlyCodeCloneVisitor implements Refactoring {

    public static final String NAME = "split_script";

    private final Script script;

    private final Stmt splitPoint;

    private final Script replacementScript1;

    private final Script replacementScript2;

    public SplitScript(Script script, Stmt splitPoint) {
        this.script = Preconditions.checkNotNull(script);
        this.splitPoint = Preconditions.checkNotNull(splitPoint);

        List<Stmt> remainingStatements = apply(script.getStmtList()).getStmts();
        List<Stmt> initialStatements = apply(script.getStmtList()).getStmts();

        Iterator<Stmt> originalIterator  = script.getStmtList().getStmts().iterator();
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

        replacementScript1 = new Script(apply(script.getEvent()), subStatements1);
        replacementScript2 = new Script(apply(script.getEvent()), subStatements2);
    }

    @Override
    public <T extends ASTNode> T apply(T node) {
        return (T) node.accept(this);
    }

    @Override
    public ASTNode visit(ScriptList node) {
        List<Script> scripts = new ArrayList<>();
        for (Script currentScript : node.getScriptList()) {
            if (currentScript == this.script) {
                scripts.add(replacementScript1);
                scripts.add(replacementScript2);
            } else {
                scripts.add(apply(currentScript));
            }
        }
        return new ScriptList(scripts);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SplitScript that)) {
            return false;
        }
        return Objects.equals(script, that.script)
                && Objects.equals(splitPoint, that.splitPoint)
                && Objects.equals(replacementScript1, that.replacementScript1)
                && Objects.equals(replacementScript2, that.replacementScript2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(script, splitPoint, replacementScript1, replacementScript2);
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() + "Splitting" + System.lineSeparator() + script.getScratchBlocks() + " at " + splitPoint.getScratchBlocks() + System.lineSeparator()
                + "Script 1:" + System.lineSeparator() + replacementScript1.getScratchBlocks() +  System.lineSeparator()
                + "Script 2:" + System.lineSeparator() + replacementScript2.getScratchBlocks() +  System.lineSeparator();
    }
}
