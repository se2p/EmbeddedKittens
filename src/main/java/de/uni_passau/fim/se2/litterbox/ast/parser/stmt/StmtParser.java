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
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.uni_passau.fim.se2.litterbox.ast.Constants;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.UnspecifiedStmt;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.*;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.mblock.*;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParserState;
import de.uni_passau.fim.se2.litterbox.ast.parser.stmt.mblock.*;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;
import de.uni_passau.fim.se2.litterbox.utils.PropertyLoader;

import java.util.logging.Logger;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.OPCODE_KEY;

public class StmtParser {

    private static final String STOP_OPTION = "STOP_OPTION";

    public static Stmt parse(final ProgramParserState state, String blockId, JsonNode blocks) throws ParsingException {
        Preconditions.checkNotNull(blockId);
        Preconditions.checkNotNull(blocks);
        Preconditions.checkState(blocks.has(blockId), "No block for id %s", blockId);

        JsonNode current = blocks.get(blockId);
        if (current instanceof ArrayNode) {
            return ExpressionStmtParser.parse(state, blockId, current, blocks);
        } else {
            final String opcode = current.get(Constants.OPCODE_KEY).asText();

            if (isTerminationStmt(current, opcode)) {
                return TerminationStmtParser.parseTerminationStmt(blockId, current, blocks);
            } else if (ActorLookStmtOpcode.contains(opcode)) {
                return ActorLookStmtParser.parse(state, blockId, current, blocks);
            } else if (ControlStmtOpcode.contains(opcode)) {
                return ControlStmtParser.parse(state, blockId, current, blocks);
            } else if (BoolExprOpcode.contains(opcode) || NumExprOpcode.contains(opcode) || StringExprOpcode
                    .contains(opcode)) {
                return ExpressionStmtParser.parse(state, blockId, current, blocks);
            } else if (CommonStmtOpcode.contains(opcode)) {
                return CommonStmtParser.parse(state, blockId, current, blocks);
            } else if (SpriteMotionStmtOpcode.contains(opcode)) {
                return SpriteMotionStmtParser.parse(state, blockId, current, blocks);
            } else if (SpriteLookStmtOpcode.contains(opcode)) {
                return SpriteLookStmtParser.parse(state, blockId, current, blocks);
            } else if (ActorSoundStmtOpcode.contains(opcode)) {
                return ActorSoundStmtParser.parse(state, blockId, current, blocks);
            } else if (CallStmtOpcode.contains(opcode)) {
                return CallStmtParser.parse(state, blockId, current, blocks);
            } else if (ListStmtOpcode.contains(opcode)) {
                return ListStmtParser.parse(state, blockId, current, blocks);
            } else if (SetStmtOpcode.contains(opcode)) {
                return SetStmtParser.parse(state, blockId, current, blocks);
            } else if (PenOpcode.contains(opcode)) {
                return PenStmtParser.parse(state, blockId, current, blocks);
            } else if (TextToSpeechOpcode.contains(opcode)) {
                return TextToSpeechParser.parse(state, blockId, current, blocks);
            } else if (ProcedureOpcode.argument_reporter_boolean.name().equals(opcode)
                    || ProcedureOpcode.argument_reporter_string_number.name().equals(opcode)) {

                return ExpressionStmtParser.parseParameter(blockId, current);
            }             // mBlock Opcodes
            else if (EmotionStmtOpcode.contains(opcode)) {
                return EmotionStmtParser.parse(blockId, current, blocks);
            } else if (LEDMatrixStmtOpcode.contains(opcode)) {
                return LEDMatrixStmtParser.parse(state, blockId, current, blocks);
            } else if (LEDStmtOpcode.contains(opcode)) {
                return LEDStmtParser.parse(state, blockId, current, blocks);
            } else if (SpeakerStmtOpcode.contains(opcode)) {
                return SpeakerStmtParser.parse(state, blockId, current, blocks);
            } else if (RobotMoveStmtOpcode.contains(opcode)) {
                return RobotMoveStmtParser.parse(state, blockId, current, blocks);
            } else if (ResetStmtOpcode.contains(opcode)) {
                return ResetStmtParser.parse(blockId, current, blocks);
            } else if (IRStmtOpcode.contains(opcode)) {
                return IRStmtParser.parse(state, blockId, current, blocks);
            } else if (MusicOpcode.contains(opcode)) {
                return MusicStmtParser.parse(state, blockId, current, blocks);
            } else {
                if (PropertyLoader.getSystemBooleanProperty("parser.log_unknown_opcode")) {
                    Logger.getGlobal().warning("Block with ID " + blockId + " and unknown opcode "
                            + current.get(OPCODE_KEY) + ". ");
                }

                return new UnspecifiedStmt();
            }
        }
    }

    private static boolean isTerminationStmt(JsonNode current, String opcode) {
        boolean hasStopOption = current.get(Constants.FIELDS_KEY).has(STOP_OPTION);
        boolean otherScriptsExist = hasStopOption && (
                hasStopOptionFieldValue(current, "other scripts in sprite")
                        || hasStopOptionFieldValue(current, "other scripts in stage")
        );
        return TerminationStmtOpcode.contains(opcode) && !otherScriptsExist;
    }

    private static boolean hasStopOptionFieldValue(final JsonNode node, final String value) {
        return value.equals(node.get(Constants.FIELDS_KEY).get(STOP_OPTION).get(Constants.FIELD_VALUE).asText());
    }
}
