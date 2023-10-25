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
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;

public class CategoryEntropy<T extends ASTNode> implements MetricExtractor<T>, ScratchVisitor {
    public static final String NAME = "category_entropy";

    private double programCount = 0;

    @Override
    public double calculateMetric(T node) {
        Preconditions.checkNotNull(node);
        programCount = 0;
        node.accept(this);
        return -programCount;
    }

    @Override
    public void visit(Script node) {
        ArrayList<MetricExtractor<Script>> list = new ArrayList<>();
        list.add(new EventsBlockCount<>()); //TODO if you doesnt want to count events as kind of blocks
                                            // add corner case for count_different_blocks == 0
        list.add(new SoundBlockCount<>());
        list.add(new MotionBlockCount<>());
        list.add(new LooksBlockCount<>());
        list.add(new ControlBlockCount<>());
        list.add(new SensingBlockCount<>());
        list.add(new VariablesBlockCount<>());
        list.add(new OperatorsBlockCount<>());


        double count = new BlockCount<Script>().calculateMetric(node);

        // Empty program
        if (count == 0) {
            return;
        }

        double localEntropy = 0.0; // Compute script category entropy

        for (MetricExtractor<Script> extractor : list) {
            double pX = extractor.calculateMetric(node) / count;
            if (pX == 0)
                continue;
            double categoryEntropy = pX * (Math.log(pX) / Math.log(2.0));
            localEntropy += categoryEntropy;
        }

        this.programCount += localEntropy;
    }

    @Override
    public void visit(ProcedureDefinition node) {
        ArrayList<MetricExtractor<ProcedureDefinition>> list = new ArrayList<>();
        list.add(new EventsBlockCount<>());
        list.add(new SoundBlockCount<>());
        list.add(new MotionBlockCount<>());
        list.add(new LooksBlockCount<>());
        list.add(new ControlBlockCount<>());
        list.add(new SensingBlockCount<>());
        list.add(new VariablesBlockCount<>());
        list.add(new OperatorsBlockCount<>());


        double count = new BlockCount<ProcedureDefinition>().calculateMetric(node);

        // Empty program
        if (count == 0)
            return;

        double localEntropy = 0.0; // Compute script category entropy

        for (MetricExtractor<ProcedureDefinition> extractor : list) {
            double pX =  extractor.calculateMetric(node) / count;
            if (pX == 0)
                continue;
            double categoryEntropy = pX * (Math.log(pX) / Math.log(2.0));
            localEntropy += categoryEntropy;
        }

        this.programCount += localEntropy;

    }

    @Override
    public String getName() {
        return NAME;
    }
}
