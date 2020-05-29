/*
 * Copyright (C) 2019 LitterBox contributors
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
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Identifier;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.UnspecifiedId;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.BlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.*;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.opcodes.ActorLookStmtOpcode;
import de.uni_passau.fim.se2.litterbox.ast.parser.ElementChoiceParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.NumExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.ProgramParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.StringExprParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.metadata.BlockMetadataParser;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ExpressionListInfo;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.VariableInfo;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import static de.uni_passau.fim.se2.litterbox.ast.Constants.*;

public class ActorLookStmtParser {

    public static ActorLookStmt parse(String blockId, JsonNode current, JsonNode allBlocks) throws ParsingException {
        Preconditions.checkNotNull(current);
        Preconditions.checkNotNull(allBlocks);

        final String opcodeString = current.get(OPCODE_KEY).asText();
        Preconditions
                .checkArgument(ActorLookStmtOpcode.contains(opcodeString), "Given blockID does not point to an event " +
                        "block.");

        final ActorLookStmtOpcode opcode = ActorLookStmtOpcode.valueOf(opcodeString);
        BlockMetadata metadata = BlockMetadataParser.parse(blockId, current);

        String variableName;
        String variableID;
        VariableInfo variableInfo;
        String actorName;
        Identifier var;
        ExpressionListInfo expressionListInfo;

        switch (opcode) {
            case sensing_askandwait:
                StringExpr question = StringExprParser.parseStringExpr(current, QUESTION_KEY, allBlocks);
                return new AskAndWait(question, metadata);

            case looks_nextbackdrop:
                return new NextBackdrop(metadata);
            case looks_switchbackdropto:
                ElementChoice elementChoice = ElementChoiceParser.parse(current, allBlocks);
                return new SwitchBackdrop(elementChoice, metadata);

            case looks_switchbackdroptoandwait:
                elementChoice = ElementChoiceParser.parse(current, allBlocks);
                return new SwitchBackdropAndWait(elementChoice, metadata);

            case looks_cleargraphiceffects:
                return new ClearGraphicEffects(metadata);

            case data_hidevariable:
                variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_NAME_POS).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).asText();
                if (!ProgramParser.symbolTable.getVariables().containsKey(variableID)) {
                    var = new UnspecifiedId();
                } else {
                    variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                    actorName = variableInfo.getActor();
                    var = new Qualified(new StrId(actorName), new Variable(new StrId(variableName)));
                }
                return new HideVariable(var, metadata);

            case data_showvariable:
                variableName = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_NAME_POS).asText();
                variableID = current.get(FIELDS_KEY).get(VARIABLE_KEY).get(VARIABLE_IDENTIFIER_POS).asText();
                if (!ProgramParser.symbolTable.getVariables().containsKey(variableID)) {
                    var = new UnspecifiedId();
                } else {
                    variableInfo = ProgramParser.symbolTable.getVariables().get(variableID);
                    actorName = variableInfo.getActor();
                    var = new Qualified(new StrId(actorName),
                            new Variable(new StrId(variableName)));
                }
                return new ShowVariable(var, metadata);

            case data_showlist:
                variableName = current.get(FIELDS_KEY).get(LIST_KEY).get(LIST_NAME_POS).asText();
                variableID = current.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                if (!ProgramParser.symbolTable.getLists().containsKey(variableID)) {
                    var = new UnspecifiedId();
                } else {
                    expressionListInfo = ProgramParser.symbolTable.getLists().get(variableID);
                    actorName = expressionListInfo.getActor();
                    var = new Qualified(new StrId(actorName), new ScratchList(new StrId(variableName)));
                }
                return new ShowList(var, metadata);

            case data_hidelist:
                variableName = current.get(FIELDS_KEY).get(LIST_KEY).get(LIST_NAME_POS).asText();
                variableID = current.get(FIELDS_KEY).get(LIST_KEY).get(LIST_IDENTIFIER_POS).asText();
                if (!ProgramParser.symbolTable.getLists().containsKey(variableID)) {
                    var = new UnspecifiedId();
                } else {
                    expressionListInfo = ProgramParser.symbolTable.getLists().get(variableID);
                    actorName = expressionListInfo.getActor();
                    var = new Qualified(new StrId(actorName), new ScratchList(new StrId(variableName)));
                }
                return new HideList(var, metadata);

            case looks_seteffectto:
                return parseSetLookEffect(current, allBlocks, metadata);

            case looks_changeeffectby:
                NumExpr numExpr = NumExprParser.parseNumExpr(current, CHANGE_KEY, allBlocks);
                String effectName = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).asText();
                return new ChangeGraphicEffectBy(GraphicEffect.fromString(effectName), numExpr, metadata);
            default:
                throw new ParsingException("No parser for opcode " + opcodeString);
        }
    }

    private static ActorLookStmt parseSetLookEffect(JsonNode current, JsonNode allBlocks, BlockMetadata metadata) throws ParsingException {
        String effect = current.get(FIELDS_KEY).get(EFFECT_KEY).get(0).asText();
        Preconditions.checkArgument(GraphicEffect.contains(effect));
        return new SetGraphicEffectTo(GraphicEffect.fromString(effect), NumExprParser.parseNumExpr(current, VALUE_KEY,
                allBlocks), metadata);
    }
}
