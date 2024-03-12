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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.parser.Scratch3Parser;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;
import de.uni_passau.fim.se2.litterbox.ml.JsonTest;
import de.uni_passau.fim.se2.litterbox.ml.MLOutputPath;
import de.uni_passau.fim.se2.litterbox.ml.MLPreprocessorCommonOptions;
import de.uni_passau.fim.se2.litterbox.ml.shared.ActorNameNormalizer;
import de.uni_passau.fim.se2.litterbox.ml.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.ml.util.MaskingStrategy;

class TokenizingAnalyzerTest implements JsonTest {

    private final String STAGE_LABEL = "stage";
    private final String SPRITE_LABEL = "sprite";

    private final String BEGIN_SUBSTACK_TOKEN = "BEGIN_SUBSTACK";
    private final String END_SUBSTACK_TOKEN = "END_SUBSTACK";
    private final String BEGIN_SPRITE_TOKEN = "BEGIN_SPRITE";
    private final String END_SPRITE_TOKEN = "END_SPRITE";
    private final String BEGIN_SCRIPT_TOKEN = "BEGIN_SCRIPT";
    private final String END_SCRIPT_TOKEN = "END_SCRIPT";
    private final String BEGIN_PROCEDURE_TOKEN = "BEGIN_PROCEDURE";
    private final String END_PROCEDURE_TOKEN = "END_PROCEDURE";
    private final String LPAREN = "(";
    private final String RPAREN = ")";
    private final String LANGLE = "<";
    private final String RANGLE = ">";

    private final String EVENT_WHENFLAG_TOKEN = "event_whenflagclicked";
    private final String LOOKS_SAY_TOKEN = "looks_say";
    private final String MOTION_MOVESTEPS_TOKEN = "motion_movesteps";
    private final String OPERATOR_ADD_TOKEN = "operator_add";
    private final String SOUND_CHANGEVOLUMEBY_TOKEN = "sound_changevolumeby";

    private final List<TokenSequence> concreteScriptSequences = List.of(
        TokenSequenceBuilder.build(
            STAGE_LABEL,
            List.of(
                List.of(
                    BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, "control_repeat", LPAREN, "10", RPAREN,
                    BEGIN_SUBSTACK_TOKEN, END_SUBSTACK_TOKEN, END_SCRIPT_TOKEN
                )
            )
        ),
        TokenSequenceBuilder.build(
            "cat",
            List.of(
                List.of(
                    BEGIN_SCRIPT_TOKEN, "event_whenkeypressed", "key", LPAREN, "39", RPAREN, LOOKS_SAY_TOKEN, LPAREN,
                    "hi_!", RPAREN, "looks_show", END_SCRIPT_TOKEN
                )
            )
        ),
        TokenSequenceBuilder.build(
            "abby",
            List.of(
                List.of(
                    BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, LOOKS_SAY_TOKEN, LPAREN, "hello_!", RPAREN,
                    END_SCRIPT_TOKEN
                )
            )
        )
    );

    private final List<TokenSequence> concreteSpriteSequences = List.of(
        TokenSequenceBuilder.build(
            STAGE_LABEL,
            List.of(
                List.of(
                    BEGIN_SPRITE_TOKEN, BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, "control_repeat", LPAREN, "10",
                    RPAREN, BEGIN_SUBSTACK_TOKEN, END_SUBSTACK_TOKEN, END_SCRIPT_TOKEN, END_SPRITE_TOKEN
                )
            )
        ),
        new TokenSequence(
            "cat",
            List.of("cat"),
            List.of(
                List.of(
                    BEGIN_SPRITE_TOKEN, BEGIN_SCRIPT_TOKEN, "event_whenkeypressed", "key", LPAREN, "39", RPAREN,
                    LOOKS_SAY_TOKEN, LPAREN, "hi_!", RPAREN, "looks_show", END_SCRIPT_TOKEN, END_SPRITE_TOKEN
                )
            ),
            List.of(
                List.of(
                    BEGIN_SPRITE_TOKEN, BEGIN_SCRIPT_TOKEN, "event", "whenkeypressed", "key", LPAREN, "39",
                    RPAREN, "looks", "say", LPAREN, "hi", "!", RPAREN, "looks", "show", END_SCRIPT_TOKEN,
                    END_SPRITE_TOKEN
                )
            )
        ),
        TokenSequenceBuilder.build(
            "abby",
            List.of(
                List.of(
                    BEGIN_SPRITE_TOKEN, BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, LOOKS_SAY_TOKEN, LPAREN, "hello_!",
                    RPAREN, END_SCRIPT_TOKEN, END_SPRITE_TOKEN
                )
            )
        )
    );

    private final List<TokenSequence> abstractScriptSequences = List.of(
        new TokenSequence(
            STAGE_LABEL,
            List.of(STAGE_LABEL),
            List.of(
                List.of(
                    BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, "control_repeat", LPAREN, "LITERAL_NUMBER", RPAREN,
                    BEGIN_SUBSTACK_TOKEN, END_SUBSTACK_TOKEN, END_SCRIPT_TOKEN
                )
            ),
            List.of(
                List.of(
                    BEGIN_SCRIPT_TOKEN, "event", "whenflagclicked", "control", "repeat", LPAREN, "literal", "number",
                    RPAREN, BEGIN_SUBSTACK_TOKEN, END_SUBSTACK_TOKEN, END_SCRIPT_TOKEN
                )
            )
        ),
        TokenSequenceBuilder.build(
            "cat",
            List.of(
                List.of(
                    BEGIN_SCRIPT_TOKEN, "event_whenkeypressed", "key", LPAREN, "keyid", RPAREN, LOOKS_SAY_TOKEN, LPAREN,
                    "LITERAL_STRING", RPAREN, "looks_show", END_SCRIPT_TOKEN
                )
            )
        ),
        TokenSequenceBuilder.build(
            "abby",
            List.of(
                List.of(
                    BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, LOOKS_SAY_TOKEN, LPAREN, "LITERAL_STRING", RPAREN,
                    END_SCRIPT_TOKEN
                )
            )
        )
    );

    @Test
    void testNoCrashOnUnparseableProgam(@TempDir Path outputDir) throws IOException {
        final MLPreprocessorCommonOptions common = new MLPreprocessorCommonOptions(
            MLOutputPath.directory(outputDir),
            true,
            false,
            true,
            false,
            ActorNameNormalizer.getDefault()
        );
        final TokenizingPreprocessor analyzer = new TokenizingPreprocessor(
            common, true, false, false, MaskingStrategy.none()
        );
        analyzer.process(getFixture("unparseable.json"));

        assertThat(Files.walk(outputDir)).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testLiteralTokenization(boolean fewerParentheses) throws ParsingException, IOException {
        final var output = tokenizeFirstSprite("ml_preprocessing/tokenizer/literals.json", fewerParentheses);

        final List<String> expected;
        if (fewerParentheses) {
            expected = List.of(
                BEGIN_SPRITE_TOKEN, BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, MOTION_MOVESTEPS_TOKEN, LPAREN,
                OPERATOR_ADD_TOKEN, "2", "5", RPAREN, SOUND_CHANGEVOLUMEBY_TOKEN, LPAREN, "-10", RPAREN,
                END_SCRIPT_TOKEN, END_SPRITE_TOKEN
            );
        }
        else {
            expected = List.of(
                BEGIN_SPRITE_TOKEN, BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, MOTION_MOVESTEPS_TOKEN, LPAREN,
                OPERATOR_ADD_TOKEN, LPAREN, "2", RPAREN, LPAREN, "5", RPAREN, RPAREN, SOUND_CHANGEVOLUMEBY_TOKEN,
                LPAREN, "-10", RPAREN, END_SCRIPT_TOKEN, END_SPRITE_TOKEN
            );
        }

        assertThat(output).containsExactlyElementsIn(expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testVariableTokenization(boolean fewerParentheses) throws ParsingException, IOException {
        final var output = tokenizeFirstSprite("ml_preprocessing/tokenizer/variables.json", fewerParentheses);

        final List<String> expected;
        if (fewerParentheses) {
            expected = List.of(
                BEGIN_SPRITE_TOKEN, BEGIN_PROCEDURE_TOKEN, "custom_block", "motion_turnright", LPAREN,
                OPERATOR_ADD_TOKEN, "2", "block_param", RPAREN, "motion_turnleft", LPAREN, "block_param", RPAREN,
                END_PROCEDURE_TOKEN, BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, MOTION_MOVESTEPS_TOKEN, LPAREN,
                OPERATOR_ADD_TOKEN, "my_variable", "5", RPAREN, SOUND_CHANGEVOLUMEBY_TOKEN, LPAREN, "my_variable",
                RPAREN, SOUND_CHANGEVOLUMEBY_TOKEN, LPAREN, "list_var", RPAREN, END_SCRIPT_TOKEN, END_SPRITE_TOKEN
            );
        }
        else {
            expected = List.of(
                BEGIN_SPRITE_TOKEN, BEGIN_PROCEDURE_TOKEN, "custom_block", "motion_turnright", LPAREN,
                OPERATOR_ADD_TOKEN, LPAREN, "2", RPAREN, LPAREN, "block_param", RPAREN, RPAREN, "motion_turnleft",
                LPAREN, "block_param", RPAREN, END_PROCEDURE_TOKEN, BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN,
                MOTION_MOVESTEPS_TOKEN, LPAREN, OPERATOR_ADD_TOKEN, LPAREN, "my_variable", RPAREN, LPAREN, "5", RPAREN,
                RPAREN, SOUND_CHANGEVOLUMEBY_TOKEN, LPAREN, "my_variable", RPAREN, SOUND_CHANGEVOLUMEBY_TOKEN, LPAREN,
                "list_var", RPAREN, END_SCRIPT_TOKEN, END_SPRITE_TOKEN
            );
        }

        assertThat(output).containsExactlyElementsIn(expected);
    }

    private List<String> tokenizeFirstSprite(
        final String fixturePath, final boolean fewerParentheses
    ) throws ParsingException, IOException {
        final Program program = getAST("src/test/fixtures/" + fixturePath);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefinitions().stream()
            .filter(ActorDefinition::isSprite)
            .findFirst()
            .orElseThrow();

        if (fewerParentheses) {
            return Tokenizer.tokenizeWithReducedParentheses(program, sprite, false, false, MaskingStrategy.none());
        }
        else {
            return Tokenizer.tokenize(program, sprite, false, false, MaskingStrategy.none());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testWholeProgramSingleSequence(boolean includeStage) throws ParsingException, IOException {
        final var analyzer = getAnalyzer(includeStage, true, false, false);

        final var output = analyzer.process(inputFile("multipleSprites.json")).collect(Collectors.toList());
        assertThat(output).hasSize(1);
        assertThat(output.get(0).label()).isEqualTo("multipleSprites");

        final List<String> tokens = output.get(0).tokens().get(0);

        int expectedSize = includeStage ? 34 : 23;
        assertThat(tokens).hasSize(expectedSize);

        int expectedSpriteCount = includeStage ? 3 : 2;
        final String beginSprite = Token.BEGIN_SPRITE.getStrRep();
        assertThat(tokens.stream().filter(beginSprite::equals).count()).isEqualTo(expectedSpriteCount);

        assertThat(tokens).containsAtLeastElementsIn(concreteScriptSequences.get(1).tokens().get(0));
        assertThat(tokens).containsAtLeastElementsIn(concreteScriptSequences.get(2).tokens().get(0));
        if (includeStage) {
            assertThat(tokens).containsAtLeastElementsIn(concreteScriptSequences.get(0).tokens().get(0));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSequencePerSprite(boolean includeStage) throws ParsingException, IOException {
        final var analyzer = getAnalyzer(includeStage, false, false, false);

        final var output = analyzer.process(inputFile("multipleSprites.json")).collect(Collectors.toList());

        int expectedSpriteCount = includeStage ? 3 : 2;
        assertThat(output).hasSize(expectedSpriteCount);

        if (includeStage) {
            assertThat(output).containsExactlyElementsIn(concreteSpriteSequences);
        }
        else {
            assertThat(output).containsExactly(concreteSpriteSequences.get(1), concreteSpriteSequences.get(2));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSequencePerScript(boolean abstractTokens) throws ParsingException, IOException {
        final var analyzer = getAnalyzer(true, false, abstractTokens, true);

        final var output = analyzer.process(inputFile("multipleSprites.json")).collect(Collectors.toList());

        if (abstractTokens) {
            assertEquals(abstractScriptSequences, output);
        }
        else {
            assertEquals(concreteScriptSequences, output);
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testSequencePerProcedureDefinition(boolean abstractTokens) throws ParsingException, IOException {
        final var analyzer = getAnalyzer(true, false, abstractTokens, true);

        final var output = analyzer.process(inputFile("customBlocks.json")).collect(Collectors.toList());
        assertThat(output).hasSize(2);

        final List<TokenSequence> expectedOutput;

        if (abstractTokens) {
            expectedOutput = List.of(
                TokenSequenceBuilder.build(STAGE_LABEL, Collections.emptyList()),
                TokenSequenceBuilder.build(
                    SPRITE_LABEL, List.of(
                        List.of(
                            BEGIN_PROCEDURE_TOKEN, "PROCEDURE_DEFINITION", "looks_say", LPAREN, "LITERAL_STRING",
                            RPAREN, MOTION_MOVESTEPS_TOKEN, LPAREN, "LITERAL_NUMBER", RPAREN, "control_stop",
                            "stop_target", END_PROCEDURE_TOKEN
                        ),
                        List.of(
                            BEGIN_PROCEDURE_TOKEN, "PROCEDURE_DEFINITION", MOTION_MOVESTEPS_TOKEN, LPAREN, "PARAMETER",
                            RPAREN, "control_if", LANGLE, "PARAMETER", RANGLE, BEGIN_SUBSTACK_TOKEN,
                            MOTION_MOVESTEPS_TOKEN, LPAREN, "LITERAL_NUMBER", RPAREN, END_SUBSTACK_TOKEN,
                            "control_stop", "stop_target", END_PROCEDURE_TOKEN
                        )
                    )
                )
            );
        }
        else {
            expectedOutput = List.of(
                TokenSequenceBuilder.build(STAGE_LABEL, Collections.emptyList()),
                TokenSequenceBuilder.build(
                    SPRITE_LABEL, List.of(
                        List.of(
                            BEGIN_PROCEDURE_TOKEN, "block_no_inputs", "looks_say", LPAREN, "hello_!", RPAREN,
                            MOTION_MOVESTEPS_TOKEN, LPAREN, "10", RPAREN, "control_stop", "this_script",
                            END_PROCEDURE_TOKEN
                        ),
                        List.of(
                            BEGIN_PROCEDURE_TOKEN, "block_with_inputs", MOTION_MOVESTEPS_TOKEN, LPAREN, "num_input",
                            RPAREN, "control_if", LANGLE, "boolean", RANGLE, BEGIN_SUBSTACK_TOKEN,
                            MOTION_MOVESTEPS_TOKEN, LPAREN, "10", RPAREN, END_SUBSTACK_TOKEN, "control_stop",
                            "this_script", END_PROCEDURE_TOKEN
                        )
                    )
                )
            );
        }

        assertThat(output).containsExactlyElementsIn(expectedOutput);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testCustomProcedureCall(boolean abstractTokens) throws ParsingException, IOException {
        final var analyzer = getAnalyzer(false, false, abstractTokens, true);

        final Program input = inputFile("ml_preprocessing/tokenizer/custom_procedure_call.json");
        final List<TokenSequence> output = analyzer.process(input).collect(Collectors.toList());

        final TokenSequence expectedOutput;
        if (abstractTokens) {
            expectedOutput = TokenSequenceBuilder.build(
                SPRITE_LABEL, List.of(
                    List.of(
                        BEGIN_PROCEDURE_TOKEN, "PROCEDURE_DEFINITION", "control_wait", LPAREN, "LITERAL_NUMBER", RPAREN,
                        END_PROCEDURE_TOKEN
                    ),
                    List.of(
                        BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, "CUSTOM_BLOCK", LPAREN, "sound_volume", RPAREN,
                        LANGLE, "sensing_mousedown", RANGLE, LPAREN, "sensing_mousey", RPAREN, "END_SCRIPT"
                    )
                )
            );
        }
        else {
            expectedOutput = TokenSequenceBuilder.build(
                SPRITE_LABEL, List.of(
                    List.of(
                        BEGIN_PROCEDURE_TOKEN, "my_proc_defn", "control_wait", LPAREN, "1", RPAREN, END_PROCEDURE_TOKEN
                    ),
                    List.of(
                        BEGIN_SCRIPT_TOKEN, EVENT_WHENFLAG_TOKEN, "my_proc_defn", LPAREN, "sound_volume", RPAREN,
                        LANGLE, "sensing_mousedown", RANGLE, LPAREN, "sensing_mousey", RPAREN, "END_SCRIPT"
                    )
                )
            );
        }

        assertThat(output).containsExactly(expectedOutput);
    }

    @Test
    void testTransformPenBlocks() throws ParsingException, IOException {
        final var analyzer = getAnalyzer(false, true, true, false);

        final Program input = inputFile("ml_preprocessing/shared/pen_blocks.json");
        final var output = analyzer.process(input).collect(Collectors.toList());

        assertThat(output).hasSize(1);
        assertThat(output.get(0).tokens().get(0)).hasSize(47);
        assertThat(output.get(0).tokens().get(0)).containsAtLeast(
            "pen_clear", "pen_stamp", "pen_pendown", "pen_penup", "pen_setpencolortocolor", "LITERAL_COLOR",
            "pen_changecolorby", "pen_setcolorto", "pen_changepensizeby", "pen_setpensizeto"
        );
    }

    @Test
    void testTransformTtsBlocks() throws ParsingException, IOException {
        final var analyzer = getAnalyzer(false, true, true, false);

        final Program input = inputFile("ml_preprocessing/shared/tts_blocks.json");
        final var output = analyzer.process(input).collect(Collectors.toList());

        assertThat(output).hasSize(1);
        assertThat(output.get(0).tokens()).hasSize(1);
        assertThat(output.get(0).tokens().get(0)).hasSize(15);
        assertThat(output.get(0).tokens().get(0))
            .containsAtLeast("tts_speak", "tts_setvoice", "tts_voice", "tts_setlanguage", "tts_language");
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testTransformMusicBlocks(boolean abstractTokens) throws ParsingException, IOException {
        final var analyzer = getAnalyzer(false, true, abstractTokens, false);

        final Program input = inputFile("ml_preprocessing/shared/music_blocks.json");
        final var output = analyzer.process(input).collect(Collectors.toList());

        assertThat(output).hasSize(1);
        assertThat(output.get(0).tokens()).hasSize(1);
        assertThat(output.get(0).tokens().get(0)).hasSize(36);
        assertThat(output.get(0).tokens().get(0))
            .containsAtLeast(
                "music_playdrumforbeats", "music_restforbeats", "music_playnoteforbeats",
                "music_playnoteforbeats", "music_setinstrumentto", "music_settempoto", "music_tempo",
                "music_changetempoby"
            );

        if (abstractTokens) {
            assertThat(output.get(0).tokens().get(0))
                .containsAtLeast("music_noteliteral", "music_drumliteral", "music_instrumentliteral");
        }
        else {
            assertThat(output.get(0).tokens().get(0))
                .containsAtLeast("BASS_DRUM", "0.25", "60", "my_variable", "ORGAN");
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "src/test/fixtures/allBlocks.json",
            "src/test/fixtures/ml_preprocessing/shared/pen_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/tts_blocks.json",
            "src/test/fixtures/ml_preprocessing/shared/music_blocks.json"
        }
    )
    void testAllBlocksNoSpaces(final String filename) throws Exception {
        final Program program = getAST(filename);
        final NoSpacesChecker noSpacesChecker = new NoSpacesChecker();
        program.accept(noSpacesChecker);
    }

    @Test
    void testTokenizeUnconnectedScript() throws ParsingException, IOException {
        final var program = getAST("src/test/fixtures/ml_preprocessing/tokenizer/unconnected_script.json");
        final var analyzer = getAnalyzer(
            true, false, true, false,
            MaskingStrategy.block("NeSwTQKd7cASL.mXXiMu")
        );
        final var tokenSequence = analyzer.process(program);
        final var tokens = tokenSequence
            .flatMap(sequence -> sequence.tokens().stream().findFirst().stream())
            .filter(sequence -> sequence.contains(Token.MASK.getStrRep()))
            .findFirst();
        assertThat(tokens.isPresent()).isTrue();
        assertThat(tokens.get()).isEqualTo(
            List.of(
                BEGIN_SPRITE_TOKEN, BEGIN_SCRIPT_TOKEN, "event_never", LANGLE, "operator_and", LANGLE,
                Token.MASK.getStrRep(), RANGLE, LANGLE, "NOTHING", RANGLE, RANGLE, END_SCRIPT_TOKEN, END_SPRITE_TOKEN
            )
        );
    }

    static class NoSpacesChecker implements ScratchVisitor {

        @Override
        public void visit(ASTNode node) {
            final String token = TokenVisitorFactory.getNormalisedToken(node);
            assertThat(token).doesNotContain(" ");

            visitChildren(node);
        }
    }

    private Program inputFile(final String fixture) throws ParsingException, IOException {
        final File programFile = Path.of("src", "test", "fixtures").resolve(fixture).toFile();
        return new Scratch3Parser().parseFile(programFile);
    }

    private Path getFixture(final String fixture) {
        return Path.of("src", "test", "fixtures").resolve(fixture);
    }

    private TokenizingProgramPreprocessor getAnalyzer(
        boolean includeStage,
        boolean wholeProgram,
        boolean abstractTokens,
        boolean sequencePerScript
    ) {
        return getAnalyzer(includeStage, wholeProgram, abstractTokens, sequencePerScript, MaskingStrategy.none());
    }

    private TokenizingProgramPreprocessor getAnalyzer(
        boolean includeStage,
        boolean wholeProgram,
        boolean abstractTokens,
        boolean sequencePerScript,
        MaskingStrategy maskingStrategy
    ) {
        final MLPreprocessorCommonOptions common = new MLPreprocessorCommonOptions(
            MLOutputPath.console(),
            includeStage,
            wholeProgram,
            true,
            abstractTokens,
            ActorNameNormalizer.getDefault()
        );
        return new TokenizingProgramPreprocessor(common, maskingStrategy, abstractTokens, false, sequencePerScript);
    }
}
