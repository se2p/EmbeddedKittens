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
package de.uni_passau.fim.se2.litterbox.utils;

import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NoBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.MoveSteps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class EqualsTest {

    @Test
    public void testScriptEquals() {
        NumberLiteral num = new NumberLiteral(2);
        MoveSteps goTo = new MoveSteps(new NumberLiteral(12), new NoBlockMetadata());
        List<Stmt> list = new ArrayList<>();
        list.add(goTo);
        RepeatTimesStmt repeat = new RepeatTimesStmt(num, new StmtList(list), NonDataBlockMetadata.emptyNonBlockMetadata());
        NumberLiteral num2 = new NumberLiteral(2);
        Assertions.assertEquals(num, num2);
        MoveSteps goTo2 = new MoveSteps(new NumberLiteral(12), new NoBlockMetadata());
        Assertions.assertEquals(goTo, goTo2);
        List<Stmt> list2 = new ArrayList<>();
        list2.add(goTo2);

        RepeatTimesStmt repeat2 = new RepeatTimesStmt(num2, new StmtList(list2), NonDataBlockMetadata.emptyNonBlockMetadata());
        Assertions.assertEquals(repeat, repeat2);
    }
}
