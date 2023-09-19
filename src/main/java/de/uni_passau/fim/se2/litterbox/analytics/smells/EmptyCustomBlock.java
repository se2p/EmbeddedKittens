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
package de.uni_passau.fim.se2.litterbox.analytics.smells;

import de.uni_passau.fim.se2.litterbox.analytics.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;

public class EmptyCustomBlock extends AbstractIssueFinder {
    public static final String NAME = "empty_custom_block";

    @Override
    public void visit(Script node) {
        //NOP should not be detected in Scripts
    }

    @Override
    public void visit(ProcedureDefinition node) {
        currentProcedure = node;
        currentScript = null;
        if (node.getStmtList().getStmts().isEmpty()) {
            String name = AstNodeUtil.replaceProcedureParams(
                    procMap.get(node.getIdent()).getName(), "()", "<>", "()");
            Hint hint = new Hint(getName());
            hint.setParameter(Hint.BLOCK_NAME, "define " + name);
            addIssue(node, node.getMetadata().getDefinition(), IssueSeverity.LOW, hint);
        }
        visitChildren(node);
        currentProcedure = null;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public IssueType getIssueType() {
        return IssueType.SMELL;
    }
}
