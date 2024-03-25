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
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import de.uni_passau.fim.se2.litterbox.ml.util.AbstractToken;
import de.uni_passau.fim.se2.litterbox.ml.util.MaskingStrategy;

class TokenizingAnalyzerTest implements JsonTest {

    private final String STAGE_LABEL = "stage";
    private final String SPRITE_LABEL = "sprite";

    private final String BEGIN_SUBSTACK = Token.BEGIN_SUBSTACK.getStrRep();
    private final String END_SUBSTACK = Token.END_SUBSTACK.getStrRep();
    private final String BEGIN_SPRITE = Token.BEGIN_SPRITE.getStrRep();
    private final String END_SPRITE = Token.END_SPRITE.getStrRep();
    private final String BEGIN_SCRIPT = Token.BEGIN_SCRIPT.getStrRep();
    private final String END_SCRIPT = Token.END_SCRIPT.getStrRep();
    private final String BEGIN_PROCEDURE = Token.BEGIN_PROCEDURE.getStrRep();
    private final String END_PROCEDURE = Token.END_PROCEDURE.getStrRep();
    private final String BEGIN_NUM_STR_EXPR = Token.BEGIN_NUM_STR_EXPR.getStrRep();
    private final String END_NUM_STR_EXPR = Token.END_NUM_STR_EXPR.getStrRep();
    private final String BEGIN_BOOL_EXPR = Token.BEGIN_BOOL_EXPR.getStrRep();
    private final String END_BOOL_EXPR = Token.END_BOOL_EXPR.getStrRep();

    private final String EVENT_WHENFLAG_TOKEN = "event_whenflagclicked";
    private final String LOOKS_SAY_TOKEN = "looks_say";
    private final String MOTION_MOVESTEPS_TOKEN = "motion_movesteps";
    private final String OPERATOR_ADD_TOKEN = "operator_add";
    private final String SOUND_CHANGEVOLUMEBY_TOKEN = "sound_changevolumeby";
    private final String CONTROL_IF_ELSE = "control_if_else";
    private final String CONTROL_IF = "control_if";
    private final String SENSING_MOUSEDOWN = "sensing_mousedown";
    private final String CONTROL_WAIT = "control_wait";
    private final String SOUND_STOPALLSOUNDS = "sound_stopallsounds";
    private final String CONTROL_STOP = "control_stop";
    private final String STOP_TARGET = "stop_target";

    private final String MASK = Token.MASK.getStrRep();
    private final String LITERAL_NUMBER = AbstractToken.LITERAL_NUMBER.toString();
    private final String NOTHING = Token.NOTHING.getStrRep();
    private final String ELSE = Token.ELSE.getStrRep();

    private final List<TokenSequence> concreteScriptSequences = List.of(
        TokenSequenceBuilder.build(
            STAGE_LABEL,
            List.of(
                List.of(
                    BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, "control_repeat", BEGIN_NUM_STR_EXPR, "10", END_NUM_STR_EXPR,
                    BEGIN_SUBSTACK, END_SUBSTACK, END_SCRIPT
                )
            )
        ),
        TokenSequenceBuilder.build(
            "cat",
            List.of(
                List.of(
                    BEGIN_SCRIPT, "event_whenkeypressed", "key", BEGIN_NUM_STR_EXPR, "39", END_NUM_STR_EXPR,
                    LOOKS_SAY_TOKEN, BEGIN_NUM_STR_EXPR,
                    "hi_!", END_NUM_STR_EXPR, "looks_show", END_SCRIPT
                )
            )
        ),
        TokenSequenceBuilder.build(
            "abby",
            List.of(
                List.of(
                    BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, LOOKS_SAY_TOKEN, BEGIN_NUM_STR_EXPR, "hello_!",
                    END_NUM_STR_EXPR,
                    END_SCRIPT
                )
            )
        )
    );

    private final List<TokenSequence> concreteSpriteSequences = List.of(
        TokenSequenceBuilder.build(
            STAGE_LABEL,
            List.of(
                List.of(
                    BEGIN_SPRITE, BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, "control_repeat", BEGIN_NUM_STR_EXPR, "10",
                    END_NUM_STR_EXPR, BEGIN_SUBSTACK, END_SUBSTACK, END_SCRIPT, END_SPRITE
                )
            )
        ),
        new TokenSequence(
            "cat",
            List.of("cat"),
            List.of(
                List.of(
                    BEGIN_SPRITE, BEGIN_SCRIPT, "event_whenkeypressed", "key", BEGIN_NUM_STR_EXPR, "39",
                    END_NUM_STR_EXPR,
                    LOOKS_SAY_TOKEN, BEGIN_NUM_STR_EXPR, "hi_!", END_NUM_STR_EXPR, "looks_show", END_SCRIPT, END_SPRITE
                )
            ),
            List.of(
                List.of(
                    BEGIN_SPRITE, BEGIN_SCRIPT, "event", "whenkeypressed", "key", BEGIN_NUM_STR_EXPR, "39",
                    END_NUM_STR_EXPR, "looks", "say", BEGIN_NUM_STR_EXPR, "hi", "!", END_NUM_STR_EXPR, "looks", "show",
                    END_SCRIPT,
                    END_SPRITE
                )
            )
        ),
        TokenSequenceBuilder.build(
            "abby",
            List.of(
                List.of(
                    BEGIN_SPRITE, BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, LOOKS_SAY_TOKEN, BEGIN_NUM_STR_EXPR, "hello_!",
                    END_NUM_STR_EXPR, END_SCRIPT, END_SPRITE
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
                    BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, "control_repeat", BEGIN_NUM_STR_EXPR, "LITERAL_NUMBER",
                    END_NUM_STR_EXPR,
                    BEGIN_SUBSTACK, END_SUBSTACK, END_SCRIPT
                )
            ),
            List.of(
                List.of(
                    BEGIN_SCRIPT, "event", "whenflagclicked", "control", "repeat", BEGIN_NUM_STR_EXPR, "literal",
                    "number",
                    END_NUM_STR_EXPR, BEGIN_SUBSTACK, END_SUBSTACK, END_SCRIPT
                )
            )
        ),
        TokenSequenceBuilder.build(
            "cat",
            List.of(
                List.of(
                    BEGIN_SCRIPT, "event_whenkeypressed", "key", BEGIN_NUM_STR_EXPR, "keyid", END_NUM_STR_EXPR,
                    LOOKS_SAY_TOKEN, BEGIN_NUM_STR_EXPR,
                    "LITERAL_STRING", END_NUM_STR_EXPR, "looks_show", END_SCRIPT
                )
            )
        ),
        TokenSequenceBuilder.build(
            "abby",
            List.of(
                List.of(
                    BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, LOOKS_SAY_TOKEN, BEGIN_NUM_STR_EXPR, "LITERAL_STRING",
                    END_NUM_STR_EXPR,
                    END_SCRIPT
                )
            )
        )
    );

    private void assertSameTokenSequence(final List<String> actual, final List<String> expected) {
        assertThat(actual).containsExactlyElementsIn(expected).inOrder();
    }

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
                BEGIN_SPRITE, BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN,
                MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR, "2", OPERATOR_ADD_TOKEN, "5", END_NUM_STR_EXPR,
                SOUND_CHANGEVOLUMEBY_TOKEN, BEGIN_NUM_STR_EXPR, "-10", END_NUM_STR_EXPR,
                END_SCRIPT, END_SPRITE
            );
        }
        else {
            expected = List.of(
                BEGIN_SPRITE, BEGIN_SCRIPT,
                EVENT_WHENFLAG_TOKEN,
                MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR,
                BEGIN_NUM_STR_EXPR, "2", END_NUM_STR_EXPR,
                OPERATOR_ADD_TOKEN,
                BEGIN_NUM_STR_EXPR, "5", END_NUM_STR_EXPR,
                END_NUM_STR_EXPR,
                SOUND_CHANGEVOLUMEBY_TOKEN, BEGIN_NUM_STR_EXPR, "-10", END_NUM_STR_EXPR,
                END_SCRIPT, END_SPRITE
            );
        }

        assertSameTokenSequence(output, expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testVariableTokenization(boolean fewerParentheses) throws ParsingException, IOException {
        final var output = tokenizeFirstSprite("ml_preprocessing/tokenizer/variables.json", fewerParentheses);

        final List<String> expected;
        if (fewerParentheses) {
            expected = List.of(
                BEGIN_SPRITE, BEGIN_PROCEDURE, "custom_block",
                "motion_turnright", BEGIN_NUM_STR_EXPR, "2", OPERATOR_ADD_TOKEN, "block_param", END_NUM_STR_EXPR,
                "motion_turnleft", BEGIN_NUM_STR_EXPR, "block_param", END_NUM_STR_EXPR,
                END_PROCEDURE,
                BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN,
                MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR, "my_variable", OPERATOR_ADD_TOKEN, "5", END_NUM_STR_EXPR,
                SOUND_CHANGEVOLUMEBY_TOKEN, BEGIN_NUM_STR_EXPR, "my_variable", END_NUM_STR_EXPR,
                SOUND_CHANGEVOLUMEBY_TOKEN, BEGIN_NUM_STR_EXPR, "list_var", END_NUM_STR_EXPR,
                END_SCRIPT, END_SPRITE
            );
        }
        else {
            expected = List.of(
                BEGIN_SPRITE, BEGIN_PROCEDURE, "custom_block",
                "motion_turnright", BEGIN_NUM_STR_EXPR,
                BEGIN_NUM_STR_EXPR, "2", END_NUM_STR_EXPR,
                OPERATOR_ADD_TOKEN,
                BEGIN_NUM_STR_EXPR, "block_param", END_NUM_STR_EXPR,
                END_NUM_STR_EXPR,
                "motion_turnleft", BEGIN_NUM_STR_EXPR, "block_param", END_NUM_STR_EXPR,
                END_PROCEDURE,
                BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN,
                MOTION_MOVESTEPS_TOKEN,
                BEGIN_NUM_STR_EXPR,
                BEGIN_NUM_STR_EXPR, "my_variable", END_NUM_STR_EXPR,
                OPERATOR_ADD_TOKEN,
                BEGIN_NUM_STR_EXPR, "5", END_NUM_STR_EXPR,
                END_NUM_STR_EXPR,
                SOUND_CHANGEVOLUMEBY_TOKEN, BEGIN_NUM_STR_EXPR, "my_variable", END_NUM_STR_EXPR,
                SOUND_CHANGEVOLUMEBY_TOKEN, BEGIN_NUM_STR_EXPR, "list_var", END_NUM_STR_EXPR,
                END_SCRIPT, END_SPRITE
            );
        }

        assertSameTokenSequence(output, expected);
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
                            BEGIN_PROCEDURE, "PROCEDURE_DEFINITION", "looks_say", BEGIN_NUM_STR_EXPR, "LITERAL_STRING",
                            END_NUM_STR_EXPR, MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR, "LITERAL_NUMBER",
                            END_NUM_STR_EXPR, CONTROL_STOP,
                            STOP_TARGET, END_PROCEDURE
                        ),
                        List.of(
                            BEGIN_PROCEDURE, "PROCEDURE_DEFINITION", MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR,
                            "PARAMETER",
                            END_NUM_STR_EXPR, CONTROL_IF, BEGIN_BOOL_EXPR, "PARAMETER", END_BOOL_EXPR, BEGIN_SUBSTACK,
                            MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR, "LITERAL_NUMBER", END_NUM_STR_EXPR,
                            END_SUBSTACK,
                            CONTROL_STOP, STOP_TARGET, END_PROCEDURE
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
                            BEGIN_PROCEDURE, "block_no_inputs", "looks_say", BEGIN_NUM_STR_EXPR, "hello_!",
                            END_NUM_STR_EXPR,
                            MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR, "10", END_NUM_STR_EXPR, CONTROL_STOP,
                            "this_script",
                            END_PROCEDURE
                        ),
                        List.of(
                            BEGIN_PROCEDURE, "block_with_inputs", MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR,
                            "num_input",
                            END_NUM_STR_EXPR, CONTROL_IF, BEGIN_BOOL_EXPR, "boolean", END_BOOL_EXPR, BEGIN_SUBSTACK,
                            MOTION_MOVESTEPS_TOKEN, BEGIN_NUM_STR_EXPR, "10", END_NUM_STR_EXPR, END_SUBSTACK,
                            CONTROL_STOP,
                            "this_script", END_PROCEDURE
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
                        BEGIN_PROCEDURE, "PROCEDURE_DEFINITION", CONTROL_WAIT, BEGIN_NUM_STR_EXPR, "LITERAL_NUMBER",
                        END_NUM_STR_EXPR,
                        END_PROCEDURE
                    ),
                    List.of(
                        BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, "CUSTOM_BLOCK", BEGIN_NUM_STR_EXPR, "sound_volume",
                        END_NUM_STR_EXPR,
                        BEGIN_BOOL_EXPR, SENSING_MOUSEDOWN, END_BOOL_EXPR, BEGIN_NUM_STR_EXPR, "sensing_mousey",
                        END_NUM_STR_EXPR, "END_SCRIPT"
                    )
                )
            );
        }
        else {
            expectedOutput = TokenSequenceBuilder.build(
                SPRITE_LABEL, List.of(
                    List.of(
                        BEGIN_PROCEDURE, "my_proc_defn", CONTROL_WAIT, BEGIN_NUM_STR_EXPR, "1", END_NUM_STR_EXPR,
                        END_PROCEDURE
                    ),
                    List.of(
                        BEGIN_SCRIPT, EVENT_WHENFLAG_TOKEN, "my_proc_defn", BEGIN_NUM_STR_EXPR, "sound_volume",
                        END_NUM_STR_EXPR,
                        BEGIN_BOOL_EXPR, SENSING_MOUSEDOWN, END_BOOL_EXPR, BEGIN_NUM_STR_EXPR, "sensing_mousey",
                        END_NUM_STR_EXPR, "END_SCRIPT"
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

    private Stream<TokenSequence> mask(final String path, final MaskingStrategy strategy)
        throws ParsingException, IOException {
        final var program = getAST(path);
        final var analyzer = getAnalyzer(true, false, true, false, strategy);
        return analyzer.process(program);
    }

    private Optional<List<String>> getMaskedSequence(final Stream<TokenSequence> tokenSequences) {
        return tokenSequences
            .flatMap(sequence -> sequence.tokens().stream().findFirst().stream())
            .filter(sequence -> sequence.contains(MASK))
            .findFirst();
    }

    private void assertMaskingSuccessful(final MaskingStrategy strategy, final List<String> expected)
        throws ParsingException, IOException {
        final String defaultPath = "src/test/fixtures/ml_preprocessing/tokenizer/masking_scenarios.json";
        assertMaskingSuccessful(defaultPath, strategy, expected);
    }

    private void assertMaskingSuccessful(final String path, final MaskingStrategy strategy, final List<String> expected)
        throws ParsingException, IOException {
        final var tokens = getMaskedSequence(mask(path, strategy));
        assertThat(tokens).isPresent();
        assertThat(tokens.get()).isEqualTo(expected);
    }

    private void assertNoMask(final MaskingStrategy strategy) throws ParsingException, IOException {
        final String path = "src/test/fixtures/ml_preprocessing/tokenizer/masking_scenarios.json";
        assertThat(getMaskedSequence(mask(path, strategy))).isEmpty();
        assertThat(mask(path, strategy).toList()).isEqualTo(mask(path, MaskingStrategy.none()).toList());
    }

    @Test
    void testTokenizeUnconnectedScript() throws ParsingException, IOException {
        final var path = "src/test/fixtures/ml_preprocessing/tokenizer/unconnected_script.json";
        final var strategy = MaskingStrategy.block("NeSwTQKd7cASL.mXXiMu");
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT, "event_never",
            BEGIN_BOOL_EXPR,
            BEGIN_BOOL_EXPR, MASK, END_BOOL_EXPR,
            "operator_and",
            BEGIN_BOOL_EXPR, "NOTHING", END_BOOL_EXPR,
            END_BOOL_EXPR,
            END_SCRIPT, END_SPRITE
        );
        assertMaskingSuccessful(path, strategy, expected);
    }

    @Test
    void testMaskStmtBlock() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.block(SOUND_STOPALLSOUNDS);
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT,
            EVENT_WHENFLAG_TOKEN,
            CONTROL_IF, BEGIN_BOOL_EXPR, SENSING_MOUSEDOWN, END_BOOL_EXPR,
            BEGIN_SUBSTACK,
            CONTROL_WAIT, BEGIN_NUM_STR_EXPR, LITERAL_NUMBER, END_NUM_STR_EXPR,
            MASK,
            END_SUBSTACK,
            CONTROL_IF_ELSE, BEGIN_BOOL_EXPR, NOTHING, END_BOOL_EXPR,
            BEGIN_SUBSTACK, END_SUBSTACK,
            ELSE,
            BEGIN_SUBSTACK, END_SUBSTACK,
            CONTROL_STOP, STOP_TARGET,
            END_SCRIPT, END_SPRITE
        );
        assertMaskingSuccessful(strategy, expected);
    }

    @Test
    void testMaskCBlock() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.block(CONTROL_IF);
        // Masking a C block should retain its SUBSTACK(s)
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT,
            EVENT_WHENFLAG_TOKEN,
            MASK,
            BEGIN_SUBSTACK,
            CONTROL_WAIT, BEGIN_NUM_STR_EXPR, LITERAL_NUMBER, END_NUM_STR_EXPR,
            SOUND_STOPALLSOUNDS,
            END_SUBSTACK,
            CONTROL_IF_ELSE, BEGIN_BOOL_EXPR, NOTHING, END_BOOL_EXPR,
            BEGIN_SUBSTACK, END_SUBSTACK,
            ELSE,
            BEGIN_SUBSTACK, END_SUBSTACK,
            CONTROL_STOP, STOP_TARGET,
            END_SCRIPT, END_SPRITE
        );
        assertMaskingSuccessful(strategy, expected);
    }

    @ValueSource(booleans = { true, false })
    @ParameterizedTest
    void testMaskExprBlock(final boolean useDirectReference) throws ParsingException, IOException {

        // Expression blocks can be selected in two ways:
        // (1) Direct reference via block ID
        // (2) Indirect reference via parent's block ID and input key
        final var strategy = useDirectReference
            ? MaskingStrategy.block(SENSING_MOUSEDOWN)
            : MaskingStrategy.input(CONTROL_IF, "CONDITION");

        // Both strategies must produce the same outcome.
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT,
            EVENT_WHENFLAG_TOKEN,
            CONTROL_IF, BEGIN_BOOL_EXPR, MASK, END_BOOL_EXPR,
            BEGIN_SUBSTACK,
            CONTROL_WAIT, BEGIN_NUM_STR_EXPR, LITERAL_NUMBER, END_NUM_STR_EXPR,
            SOUND_STOPALLSOUNDS,
            END_SUBSTACK,
            CONTROL_IF_ELSE, BEGIN_BOOL_EXPR, NOTHING, END_BOOL_EXPR,
            BEGIN_SUBSTACK, END_SUBSTACK,
            ELSE,
            BEGIN_SUBSTACK, END_SUBSTACK,
            CONTROL_STOP, STOP_TARGET,
            END_SCRIPT, END_SPRITE
        );
        assertMaskingSuccessful(strategy, expected);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testMaskSubstacks(final boolean maskEmptySubstack) throws ParsingException, IOException {
        // The MaskingType `Input` is only intended for expressions. Applying it to SUBSTACKs should do nothing;
        // it must neither mask the first statement of the SUBSTACK nor the entire SUBSTACK. This holds for both
        // empty and non-empty SUBSTACKs.
        final String blockId = maskEmptySubstack ? CONTROL_IF_ELSE : CONTROL_IF;
        final var strategy = MaskingStrategy.input(blockId, "SUBSTACK");
        assertNoMask(strategy);
    }

    @Test
    void testMaskEmptyExprInput() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.input(CONTROL_IF_ELSE, "CONDITION");
        // Unlike masking an empty SUBSTACK, masking an empty input must produce a MASK token.
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT,
            EVENT_WHENFLAG_TOKEN,
            CONTROL_IF, BEGIN_BOOL_EXPR, SENSING_MOUSEDOWN, END_BOOL_EXPR,
            BEGIN_SUBSTACK,
            CONTROL_WAIT, BEGIN_NUM_STR_EXPR, LITERAL_NUMBER, END_NUM_STR_EXPR,
            SOUND_STOPALLSOUNDS,
            END_SUBSTACK,
            CONTROL_IF_ELSE, BEGIN_BOOL_EXPR, MASK, END_BOOL_EXPR,
            BEGIN_SUBSTACK, END_SUBSTACK,
            ELSE,
            BEGIN_SUBSTACK, END_SUBSTACK,
            CONTROL_STOP, STOP_TARGET,
            END_SCRIPT, END_SPRITE
        );
        assertMaskingSuccessful(strategy, expected);
    }

    @Test
    void testMaskInvalidBlockId() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.block("invalid");
        assertNoMask(strategy);
    }

    @Test
    void testMaskInvalidInputKey() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.input(CONTROL_WAIT, "INVALID");
        assertNoMask(strategy);
    }

    @Test
    void testMaskPrimitive() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.input(CONTROL_WAIT, "DURATION");
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT,
            EVENT_WHENFLAG_TOKEN,
            CONTROL_IF, BEGIN_BOOL_EXPR, SENSING_MOUSEDOWN, END_BOOL_EXPR,
            BEGIN_SUBSTACK,
            CONTROL_WAIT, BEGIN_NUM_STR_EXPR, MASK, END_NUM_STR_EXPR,
            SOUND_STOPALLSOUNDS,
            END_SUBSTACK,
            CONTROL_IF_ELSE, BEGIN_BOOL_EXPR, NOTHING, END_BOOL_EXPR,
            BEGIN_SUBSTACK, END_SUBSTACK,
            ELSE,
            BEGIN_SUBSTACK, END_SUBSTACK,
            CONTROL_STOP, STOP_TARGET,
            END_SCRIPT, END_SPRITE

        );
        assertMaskingSuccessful(strategy, expected);
    }

    @Test
    void testMaskStopBlock() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.block(CONTROL_STOP);
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT,
            EVENT_WHENFLAG_TOKEN,
            CONTROL_IF, BEGIN_BOOL_EXPR, SENSING_MOUSEDOWN, END_BOOL_EXPR,
            BEGIN_SUBSTACK,
            CONTROL_WAIT, BEGIN_NUM_STR_EXPR, LITERAL_NUMBER, END_NUM_STR_EXPR,
            SOUND_STOPALLSOUNDS,
            END_SUBSTACK,
            CONTROL_IF_ELSE, BEGIN_BOOL_EXPR, NOTHING, END_BOOL_EXPR,
            BEGIN_SUBSTACK, END_SUBSTACK,
            ELSE,
            BEGIN_SUBSTACK, END_SUBSTACK,
            MASK,
            END_SCRIPT, END_SPRITE
        );
        assertMaskingSuccessful(strategy, expected);
    }

    @Test
    void testMaskFixedOption() throws ParsingException, IOException {
        final var strategy = MaskingStrategy.fixedOption(CONTROL_STOP);
        final var expected = List.of(
            BEGIN_SPRITE, BEGIN_SCRIPT,
            EVENT_WHENFLAG_TOKEN,
            CONTROL_IF, BEGIN_BOOL_EXPR, SENSING_MOUSEDOWN, END_BOOL_EXPR,
            BEGIN_SUBSTACK,
            CONTROL_WAIT, BEGIN_NUM_STR_EXPR, LITERAL_NUMBER, END_NUM_STR_EXPR,
            SOUND_STOPALLSOUNDS,
            END_SUBSTACK,
            CONTROL_IF_ELSE, BEGIN_BOOL_EXPR, NOTHING, END_BOOL_EXPR,
            BEGIN_SUBSTACK, END_SUBSTACK,
            ELSE,
            BEGIN_SUBSTACK, END_SUBSTACK,
            CONTROL_STOP, MASK,
            END_SCRIPT, END_SPRITE
        );
        assertMaskingSuccessful(strategy, expected);
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
