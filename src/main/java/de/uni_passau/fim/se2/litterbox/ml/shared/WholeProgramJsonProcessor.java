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
package de.uni_passau.fim.se2.litterbox.ml.shared;

import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.MLProgramPreprocessor;

public class WholeProgramJsonProcessor<T> extends MLProgramPreprocessor<WholeProgramOutput<T>> {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final MLProgramPreprocessor<T> preprocessor;

    public WholeProgramJsonProcessor(
        final MLPreprocessorCommonOptions commonOptions,
        final MLProgramPreprocessor<T> programPreprocessor
    ) {
        super(commonOptions);
        this.preprocessor = programPreprocessor;
    }

    @Override
    public String resultToString(final WholeProgramOutput<T> result) {
        try {
            return objectMapper.writeValueAsString(result);
        }
        catch (JsonProcessingException e) {
            throw new IllegalStateException(
                "The object to JSON conversion is misconfigured. Cannot convert result to JSON string.",
                e
            );
        }
    }

    @Override
    public Stream<WholeProgramOutput<T>> processSprites(final Program program) {
        throw new UnsupportedOperationException("Can only process programs as a whole!");
    }

    @Override
    public Stream<WholeProgramOutput<T>> processWholeProgram(final Program program) {
        final List<T> perSpriteOutputs = preprocessor.processSprites(program).toList();
        return Stream.of(new WholeProgramOutput<>(program.getIdent().getName(), perSpriteOutputs));
    }
}
