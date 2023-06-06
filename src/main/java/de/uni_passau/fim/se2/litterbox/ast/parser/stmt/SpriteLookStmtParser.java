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
package de.uni_passau.fim.se2.litterbox.ast.parser.stmt;

import com.fasterxml.jackson.databind.JsonNode;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.ElementChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.NumExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.StringExpr;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteLookStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.CostumeChoiceParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;
import static de.uni_passau.fim.se2.litterbox.ast.opcodes.SpriteLookStmtOpcode.looks_gotofrontback;

public class SpriteLookStmtParser {

    public static SpriteLookStmt parse(final ProgramParserState state, String identifier, JsonNode current,
                                       JsonNode allBlocks)
            throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(SpriteLookStmtOpcode.contains(opcodeString),
                        "Given blockID does not point to a sprite look block. Opcode is " + opcodeString);

        final SpriteLookStmtOpcode opcode = SpriteLookStmtOpcode.valueOf(opcodeString);
        StringExpr stringExpr;
        NumExpr numExpr;
        BlockMetadata metadata = BlockMetadataParser.parse(identifier, current);
        return switch (opcode) {
            case looks_show -> new Show(metadata);
            case looks_hide -> new Hide(metadata);
            case looks_sayforsecs -> {
                stringExpr = StringExprParser.parseStringExpr(state, current, MESSAGE_KEY, allBlocks);
                numExpr = NumExprParser.parseNumExpr(state, current, SECS_KEY, allBlocks);
                yield new SayForSecs(stringExpr, numExpr, metadata);
            }
            case looks_say -> {
                stringExpr = StringExprParser.parseStringExpr(state, current, MESSAGE_KEY, allBlocks);
                yield new Say(stringExpr, metadata);
            }
            case looks_thinkforsecs -> {
                stringExpr = StringExprParser.parseStringExpr(state, current, MESSAGE_KEY, allBlocks);
                numExpr = NumExprParser.parseNumExpr(state, current, SECS_KEY, allBlocks);
                yield new ThinkForSecs(stringExpr, numExpr, metadata);
            }
            case looks_think -> {
                stringExpr = StringExprParser.parseStringExpr(state, current, MESSAGE_KEY, allBlocks);
                yield new Think(stringExpr, metadata);
            }
            case looks_nextcostume -> new NextCostume(metadata);
            case looks_switchcostumeto -> {
                ElementChoice costumeChoice = CostumeChoiceParser.parse(state, current, allBlocks);
                yield new SwitchCostumeTo(costumeChoice, metadata);
            }
            case looks_changesizeby -> {
                numExpr = NumExprParser.parseNumExpr(state, current, CHANGE_KEY, allBlocks);
                yield new ChangeSizeBy(numExpr, metadata);
            }
            case looks_setsizeto -> {
                numExpr = NumExprParser.parseNumExpr(state, current, SIZE_KEY_CAP, allBlocks);
                yield new SetSizeTo(numExpr, metadata);
            }
            case looks_gotofrontback -> parseGoToLayer(current, metadata);
            case looks_goforwardbackwardlayers -> parseGoForwardBackwardLayer(state, current, allBlocks, metadata);
        };
    }

    private static SpriteLookStmt parseGoForwardBackwardLayer(final ProgramParserState state, JsonNode current,
                                                              JsonNode allBlocks, BlockMetadata metadata)
            throws ParsingException {
        JsonNode frontBack = current.get(FIELDS_KEY).get("FORWARD_BACKWARD").get(FIELD_VALUE);

        NumExpr num = NumExprParser.parseNumExpr(state, current, NUM_KEY, allBlocks);

        String layerOption = frontBack.asText();
        return new ChangeLayerBy(num, new ForwardBackwardChoice(layerOption), metadata);
    }

    private static SpriteLookStmt parseGoToLayer(JsonNode current, BlockMetadata metadata)
            throws ParsingException {
        Preconditions.checkArgument(current.get(OPCODE_KEY).asText().equals(looks_gotofrontback.toString()));

        JsonNode frontBack = current.get(FIELDS_KEY).get("FRONT_BACK").get(FIELD_VALUE);
        String layerOption = frontBack.asText();
        try {
            return new GoToLayer(new LayerChoice(layerOption), metadata);
        } catch (IllegalArgumentException e) {
            throw new ParsingException("Unknown LayerChoice label for GoToLayer.");
        }
    }
}
