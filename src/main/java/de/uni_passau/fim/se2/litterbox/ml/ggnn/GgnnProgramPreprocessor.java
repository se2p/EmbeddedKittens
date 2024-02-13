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

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.MLProgramPreprocessor;

public class GgnnProgramPreprocessor extends MLProgramPreprocessor<GgnnAnalyzerOutput> {

    private static final Logger log = Logger.getLogger(GgnnProgramPreprocessor.class.getName());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String labelName;
    private final GgnnOutputFormat outputFormat;

    public GgnnProgramPreprocessor(
        final MLPreprocessorCommonOptions commonOptions, final GgnnOutputFormat outputFormat, final String labelName
    ) {
        super(commonOptions);

        this.outputFormat = outputFormat;
        this.labelName = labelName;
    }

    @Override
    public Stream<GgnnAnalyzerOutput> process(final Program program) {
        GenerateGgnnGraphTask generateGgnnGraphTask = new GenerateGgnnGraphTask(
            program, commonOptions.includeStage(), commonOptions.includeDefaultSprites(), commonOptions.wholeProgram(),
            labelName, commonOptions.actorNameNormalizer()
        );
        if (outputFormat.isDotGraph()) {
            String label = program.getIdent().getName();
            return Stream.of(new GgnnAnalyzerOutput.DotGraph(generateGgnnGraphTask.generateDotGraphData(label)));
        }
        else {
            return generateGgnnGraphTask.generateGraphData().map(GgnnAnalyzerOutput.Graph::new);
        }
    }

    @Override
    protected String resultToString(final GgnnAnalyzerOutput result) {
        if (result instanceof GgnnAnalyzerOutput.DotGraph graph) {
            return graph.dotGraph();
        }
        else if (result instanceof GgnnAnalyzerOutput.Graph graph) {
            return graphToJson(graph.graph()).collect(Collectors.joining("\n"));
        }
        else {
            // todo: refactor into switch pattern when upgrading to Java 21
            throw new IllegalStateException("Unknown GGNN output format!");
        }
    }

    private Stream<String> graphToJson(GgnnProgramGraph graph) {
        try {
            return Stream.of(OBJECT_MAPPER.writeValueAsString(graph));
        }
        catch (JsonProcessingException e) {
            // can only happen in case LitterBox is used as a dependency and e.g., due to
            // multiple competing Jackson versions in the classpath the conversion fails
            log.log(
                Level.SEVERE,
                "Jackson could not convert the GGNN graph to JSON. Probable misconfiguration.",
                e
            );
            return Stream.empty();
        }
    }
}
