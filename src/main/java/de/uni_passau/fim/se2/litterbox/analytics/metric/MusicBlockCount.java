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
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.*;
import de.uni_passau.fim.se2.litterbox.ast.visitor.MusicExtensionVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class MusicBlockCount<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor, MusicExtensionVisitor {

    public static final String NAME = "music_block_count";
    private int count = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        count = 0;
        node.accept(this);
        return count;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visitParentVisitor(MusicBlock node) {
        visitDefaultVisitor(node);
    }

    @Override
    public void visit(MusicBlock node) {
        node.accept((MusicExtensionVisitor) this);
    }

    @Override
    public void visit(SetTempoTo node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(ChangeTempoBy node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(RestForBeats node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(PlayDrumForBeats node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(PlayNoteForBeats node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(SetInstrumentTo node) {
        count++;
        visitChildren(node);
    }

    @Override
    public void visit(Tempo node) {
        count++;
    }
}
