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
package de.uni_passau.fim.se2.litterbox.ml.astnn;

import java.util.logging.Logger;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.MLProgramPreprocessor;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.AstnnNode;
import de.uni_passau.fim.se2.litterbox.ml.astnn.model.StatementTreeSequence;

public class AstnnProgramPreprocessor extends MLProgramPreprocessor<StatementTreeSequence> {

    private static final Logger log = Logger.getLogger(AstnnProgramPreprocessor.class.getName());

    private final ObjectMapper objectMapper;
    private final StatementTreeSequenceBuilder statementTreeSequenceBuilder;

    public AstnnProgramPreprocessor(final MLPreprocessorCommonOptions commonOptions) {
        super(commonOptions);

        this.objectMapper = new ObjectMapper();
        statementTreeSequenceBuilder = new StatementTreeSequenceBuilder(
            commonOptions.actorNameNormalizer(), commonOptions.abstractTokens()
        );
    }

    @Override
    public Stream<StatementTreeSequence> process(final Program program) {
        final Stream<StatementTreeSequence> nodes;
        if (commonOptions.wholeProgram()) {
            nodes = Stream.of(
                statementTreeSequenceBuilder
                    .build(program, commonOptions.includeStage(), commonOptions.includeDefaultSprites())
            );
        }
        else {
            nodes = statementTreeSequenceBuilder
                .buildPerActor(program, commonOptions.includeStage(), commonOptions.includeDefaultSprites());
        }

        return nodes.filter(this::isValidStatementSequence);
    }

    /**
     * We are not interested in trees that either have got no label, or are empty.
     *
     * @param sequence A statement tree sequence for a sprite or program.
     * @return If the sequence is usable for the ML task.
     */
    private boolean isValidStatementSequence(final StatementTreeSequence sequence) {
        final boolean hasEmptyName = sequence.label().isBlank();
        // the actor definition might be the top-most "statement", so we check for actual blocks inside the actor, too
        final boolean hasNoStatements = sequence.statements().isEmpty()
            || sequence.statements().stream().allMatch(AstnnNode::isLeaf);

        return !hasEmptyName && !hasNoStatements;
    }

    @Override
    protected String resultToString(StatementTreeSequence result) {
        return sequenceToString(result);
    }

    private String sequenceToString(final StatementTreeSequence sequence) {
        try {
            return objectMapper.writeValueAsString(sequence);
        }
        catch (JsonProcessingException ex) {
            // If this breaks: check that Jackson has reflection access to the classes
            log.warning("The ASTNN node cannot be converted to JSON. Please report this bug to the developers.");
            ex.printStackTrace();
            return null;
        }
    }
}
