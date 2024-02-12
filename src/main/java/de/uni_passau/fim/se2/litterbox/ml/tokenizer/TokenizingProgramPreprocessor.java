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
package de.uni_passau.fim.se2.litterbox.ml.tokenizer;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.MLProgramPreprocessor;
import de.uni_passau.fim.se2.litterbox.ml.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

public class TokenizingProgramPreprocessor extends MLProgramPreprocessor<TokenSequence> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final BiFunction<Program, ASTNode, List<String>> tokenizeFunction;
    private final boolean sequencePerScript;

    public TokenizingProgramPreprocessor(
        final MLPreprocessorCommonOptions commonOptions, final MaskingStrategy maskingStrategy,
        final boolean abstractFixedNodeOption, final boolean statementLevel, final boolean sequencePerScript
    ) {
        super(commonOptions);

        Preconditions.checkArgument(
            !(commonOptions.wholeProgram() && sequencePerScript),
            "Cannot generate one sequence for the whole program and sequences per script at the same time."
        );

        this.sequencePerScript = sequencePerScript;

        if (statementLevel) {
            tokenizeFunction = (program, astNode) -> StatementLevelTokenizer
                .tokenize(program, astNode, commonOptions.abstractTokens(), maskingStrategy);
        }
        else {
            tokenizeFunction = (program, astNode) -> Tokenizer
                .tokenize(program, astNode, commonOptions.abstractTokens(), abstractFixedNodeOption, maskingStrategy);
        }
    }

    @Override
    public Stream<TokenSequence> process(Program program) {
        final Stream<ActorDefinition> actors = getActors(program);

        final Stream<TokenSequence> result;
        if (commonOptions.wholeProgram()) {
            final List<String> tokens = actors
                .flatMap(actor -> getTokenSequencesForActor(program, actor))
                .flatMap(List::stream)
                .toList();
            result = Stream.of(TokenSequenceBuilder.build(program.getIdent().getName(), List.of(tokens)));
        }
        else {
            result = actors.flatMap(actor -> generateSequenceForActor(program, actor).stream());
        }

        return result;
    }

    @Override
    protected String resultToString(TokenSequence result) {
        try {
            return OBJECT_MAPPER.writeValueAsString(result);
        }
        catch (JsonProcessingException e) {
            // should never happen
            throw new RuntimeException(e);
        }
    }

    private Stream<ActorDefinition> getActors(final Program program) {
        if (commonOptions.includeDefaultSprites()) {
            return AstNodeUtil.getActors(program, commonOptions.includeStage());
        }
        else {
            return AstNodeUtil.getActorsWithoutDefaultSprites(program, commonOptions.includeStage());
        }
    }

    private Optional<TokenSequence> generateSequenceForActor(final Program program, final ActorDefinition actor) {
        return commonOptions.actorNameNormalizer().normalizeName(actor).map(label -> {
            final List<List<String>> tokens = getTokenSequencesForActor(program, actor).toList();
            return TokenSequenceBuilder.build(label, tokens);
        });
    }

    private Stream<List<String>> getTokenSequencesForActor(final Program program, final ActorDefinition actor) {
        if (sequencePerScript) {
            final Stream<ASTNode> scripts = actor.getScripts().getScriptList()
                .stream().map(ASTNode.class::cast);
            final Stream<ASTNode> procedures = actor.getProcedureDefinitionList()
                .getList().stream().map(ASTNode.class::cast);

            return Stream.concat(procedures, scripts)
                .map(node -> tokenizeFunction.apply(program, node));
        }
        else {
            return Stream.of(tokenizeFunction.apply(program, actor));
        }
    }
}
