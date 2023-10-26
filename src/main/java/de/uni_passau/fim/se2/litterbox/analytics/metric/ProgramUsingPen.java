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

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class ProgramUsingPen<T extends ASTNode> implements MetricExtractor<T> {
    public static final String NAME = "using_pen";

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        final double penBlocks = new PenBlockCount<>().calculateMetric(node);
        return Math.min(1, penBlocks);
    }
}
