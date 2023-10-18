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
package de.uni_passau.fim.se2.litterbox.analytics.metric;

import de.uni_passau.fim.se2.litterbox.analytics.MetricExtractor;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Event;
import de.uni_passau.fim.se2.litterbox.ast.model.event.Never;
import de.uni_passau.fim.se2.litterbox.ast.model.event.StartedAsClone;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.Broadcast;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.BroadcastAndWait;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class EventsBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "events_block_count";

    private int count = 0;

    @Override
    public MetricResult calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return new MetricResult(NAME, count);
    }

    @Override
    public void visit(Event node) {
        //StartedAsClone is not in the Events category in Scratch
        if (node instanceof StartedAsClone || node instanceof Never) {
            return;
        }
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Broadcast node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(BroadcastAndWait node) {
        count++;
        visitChildren(node);
    }

    @Override
    public String getName() {
        return NAME;
    }
}
