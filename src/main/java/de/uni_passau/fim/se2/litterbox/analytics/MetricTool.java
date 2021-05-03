/*
 * Copyright (C) 2019-2021 LitterBox contributors
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
package de.uni_passau.fim.se2.litterbox.analytics;

import de.uni_passau.fim.se2.litterbox.analytics.metric.*;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class MetricTool {

    private List<MetricExtractor> metrics = Arrays.asList(
            new Cohesion<Program>(),
            new CategoryEntropy(),
            new BlockCount<Program>(),
            new HalsteadDifficulty());
            /*
            new ComputationalThinkingAverageScore(),
            new ComputationalThinkingScore(),
            new ComputationalThinkingScoreAbstraction(),
            new ComputationalThinkingScoreDataRepresentation(),
            new ComputationalThinkingScoreFlowControl(),
            new ComputationalThinkingScoreLogic(),
            new ComputationalThinkingScoreParallelization(),
            new ComputationalThinkingScoreSynchronization(),
            new ComputationalThinkingScoreUserInteractivity(),
            new ControlBlockCount<Program>(),
            new EventsBlockCount<Program>(),
            new HalsteadDifficulty<Program>(),
            new HalsteadEffort<Program>(),
            new HalsteadLength<Program>(),
            new HalsteadVocabulary<Program>(),
            new HalsteadVolume<Program>(),
            new HatCount<Program>(),
            new InterproceduralCyclomaticComplexity<Program>(),
            new LengthLongestScript<Program>(),
            new LooksBlockCount<Program>(),
            new MostComplexScript<Program>(),
            new MotionBlockCount<Program>(),
            new MyBlocksBlockCount<Program>(),
            new OperatorsBlockCount<Program>(),
            new PenBlockCount<Program>(),
            new ProcedureCount<Program>(),
            new ProgramUsingPen<Program>(),
            new ScriptCount<Program>(),
            new SensingBlockCount<Program>(),
            new SoundBlockCount<Program>(),
            new SpriteCount<Program>(),
            new StatementCount<Program>(),
            new VariablesBlockCount<Program>(),
            new WeightedMethodCount<Program>(),
            new WeightedMethodCountStrict<Program>());*/

    public List<String> getMetricNames() {
        return metrics.stream().map(MetricExtractor::getName).collect(Collectors.toList());
    }

    public List<MetricExtractor> getAnalyzers() {
        return Collections.unmodifiableList(metrics);
    }

    public void createCSVFile(Program program, String fileName) throws IOException {
        List<String> headers = new ArrayList<>();
        headers.add("project");
        metrics.stream().map(MetricExtractor::getName).forEach(headers::add);
        CSVPrinter printer = getNewPrinter(fileName, headers);
        List<String> row = new ArrayList<>();
        row.add(program.getIdent().getName());

        for (MetricExtractor extractor : metrics) {
            row.add(Double.toString(extractor.calculateMetric(program)));
        }
        printer.printRecord(row);
        printer.flush();
    }

    // TODO: Code clone -- same is in CSVReportGenerator
    protected CSVPrinter getNewPrinter(String name, List<String> heads) throws IOException {

        if (Files.exists(Paths.get(name))) {
            BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(name), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withSkipHeaderRecord());
        } else {
            BufferedWriter writer = Files.newBufferedWriter(
                    Paths.get(name), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(heads.toArray(new String[0])));
        }
    }
}
