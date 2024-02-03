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

import de.uni_passau.fim.se2.litterbox.ast.model.*;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Next;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Prev;
import de.uni_passau.fim.se2.litterbox.ast.model.elementchoice.Random;
import de.uni_passau.fim.se2.litterbox.ast.model.event.EventAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.Expression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.UnspecifiedExpression;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.num.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.*;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.AttributeFromFixed;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.string.attributes.FixedAttribute;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.Tempo;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.drums.FixedDrum;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.instruments.FixedInstrument;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.music.notes.FixedNote;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.ExprLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.FixedLanguage;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.language.Language;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.ExprVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.FixedVoice;
import de.uni_passau.fim.se2.litterbox.ast.model.extensions.texttospeech.voice.Voice;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.Qualified;
import de.uni_passau.fim.se2.litterbox.ast.model.identifier.StrId;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.BoolLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.ColorLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.StringLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.DataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.metadata.block.NonDataBlockMetadata;
import de.uni_passau.fim.se2.litterbox.ast.model.position.MousePos;
import de.uni_passau.fim.se2.litterbox.ast.model.position.RandomPos;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorlook.GraphicEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.actorsound.SoundEffect;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.common.StopOtherScriptsInSprite;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.*;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.ForwardBackwardChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritelook.LayerChoice;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.DragMode;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.spritemotion.RotationStyle;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopAll;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.termination.StopThisScript;
import de.uni_passau.fim.se2.litterbox.ast.model.timecomp.TimeComp;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.Edge;
import de.uni_passau.fim.se2.litterbox.ast.model.touchable.MousePointer;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Parameter;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.ScratchList;
import de.uni_passau.fim.se2.litterbox.ast.model.variable.Variable;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ml.shared.BaseTokenVisitor;
import de.uni_passau.fim.se2.litterbox.ml.shared.TokenVisitorFactory;
import de.uni_passau.fim.se2.litterbox.ml.util.AbstractToken;
import de.uni_passau.fim.se2.litterbox.ml.util.MaskingStrategy;
import de.uni_passau.fim.se2.litterbox.ml.util.MaskingType;
import de.uni_passau.fim.se2.litterbox.ml.util.StringUtil;

public class Tokenizer extends AbstractTokenizer {

    private final BaseTokenVisitor tokenVisitor = TokenVisitorFactory.getDefaultTokenVisitor(true);
    private final boolean abstractFixedNodeOptions;

    private Tokenizer(
        final ProcedureDefinitionNameMapping procedureNameMapping,
        final boolean abstractTokens,
        final boolean abstractFixedNodeOptions,
        final MaskingStrategy maskingStrategy
    ) {
        super(procedureNameMapping, abstractTokens, maskingStrategy);
        this.abstractFixedNodeOptions = abstractFixedNodeOptions;
    }

    public static List<String> tokenize(
        final Program program,
        final ASTNode node,
        final boolean abstractTokens,
        final boolean abstractFixedNodeOptions,
        final MaskingStrategy maskingStrategy
    ) {
        return tokenize(
            program.getProcedureMapping(), node, abstractTokens, abstractFixedNodeOptions,
            maskingStrategy
        );
    }

    private static List<String> tokenize(
        final ProcedureDefinitionNameMapping procedureNameMapping,
        final ASTNode node,
        final boolean abstractTokens,
        final boolean abstractFixedNodeOptions,
        final MaskingStrategy maskingStrategy
    ) {
        final Tokenizer v = new Tokenizer(
            procedureNameMapping, abstractTokens, abstractFixedNodeOptions,
            maskingStrategy
        );
        node.accept(v);
        return v.getTokens();
    }

    @Override
    protected void visit(final ASTNode node, final Token opcode) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        }
        else {
            addToken(opcode);
            visitChildren(node);
        }
    }

    private boolean shouldBeMasked(final ASTNode node) {
        final MaskingType maskingType = getMaskingStrategy().getMaskingType();
        final boolean shouldMask = MaskingType.Expression.equals(maskingType)
            || MaskingType.Statement.equals(maskingType);

        return shouldMask && getMaskingStrategy().getBlockId().equals(getBlockId(node));
    }

    private void visit(final ASTNode node, final String token) {
        addToken(token);
        visitChildren(node);
    }

    // region motion

    @Override
    public void visit(PositionX node) {
        visit(node, Token.MOTION_XPOSITION);
    }

    @Override
    public void visit(PositionY node) {
        visit(node, Token.MOTION_YPOSITION);
    }

    @Override
    public void visit(Direction node) {
        visit(node, Token.MOTION_DIRECTION);
    }

    @Override
    public void visit(RandomPos node) {
        visit(node, Token.MOTION_RANDOMPOS);
    }

    @Override
    public void visit(MousePos node) {
        visit(node, Token.MOTION_MOUSEPOS);
    }

    @Override
    public void visit(MousePointer node) {
        visit(node, Token.MOTION_MOUSEPOINTER);
    }

    @Override
    public void visit(RotationStyle node) {
        visitFixedNodeOption(node, Token.MOTION_ROTATIONSTYLE);
    }

    @Override
    public void visit(DragMode node) {
        visitFixedNodeOption(node, Token.MOTION_DRAGMODE);
    }

    // endregion motion

    // region looks

    @Override
    public void visit(Costume node) {
        visit(node, Token.LOOKS_COSTUMENUMBERNAME);
    }

    @Override
    public void visit(Backdrop node) {
        visit(node, Token.LOOKS_BACKDROP);
    }

    @Override
    public void visit(Size node) {
        visit(node, Token.LOOKS_SIZE);
    }

    @Override
    public void visit(GraphicEffect node) {
        visitFixedNodeOption(node, Token.LOOKS_GRAPHICEFFECT);
    }

    @Override
    public void visit(ForwardBackwardChoice node) {
        visitFixedNodeOption(node, Token.LOOKS_FORWARDBACKWARD);
    }

    @Override
    public void visit(LayerChoice node) {
        visitFixedNodeOption(node, Token.LOOKS_LAYERCHOICE);
    }

    @Override
    public void visit(Next node) {
        visit(node, Token.LOOKS_NEXTBACKDROPCHOICE);
    }

    @Override
    public void visit(Prev node) {
        visit(node, Token.LOOKS_PREVBACKDROPCHOICE);
    }

    @Override
    public void visit(Random node) {
        visit(node, Token.LOOKS_RANDOMBACKDROPCHOICE);
    }

    // endregion looks

    // region sound

    @Override
    public void visit(Volume node) {
        visit(node, Token.SOUND_VOLUME);
    }

    @Override
    public void visit(SoundEffect node) {
        visitFixedNodeOption(node, Token.SOUND_EFFECT);
    }

    // endregion sound

    // region events

    @Override
    public void visit(EventAttribute node) {
        visitFixedNodeOption(node, Token.EVENT_ATTRIBUTE);
    }

    @Override
    public void visit(Message node) {
        visit(node, Token.EVENT_MESSAGE);
    }

    // endregion events

    // region control

    @Override
    public void visit(RepeatTimesStmt node) {
        visitControlBlockHead(node, Token.CONTROL_REPEAT, node.getTimes());
        visitControlBlockBody(node.getStmtList());
    }

    @Override
    public void visit(RepeatForeverStmt node) {
        if (shouldBeMasked(node)) {
            addToken(Token.MASK);
        }
        else {
            addToken(Token.CONTROL_FOREVER);
        }
        visitControlBlockBody(node.getStmtList());
    }

    @Override
    public void visit(IfThenStmt node) {
        visitControlBlockHead(node, Token.CONTROL_IF, node.getBoolExpr());
        visitControlBlockBody(node.getThenStmts());
    }

    @Override
    public void visit(IfElseStmt node) {
        visitControlBlockHead(node, Token.CONTROL_IF_ELSE, node.getBoolExpr());
        visitControlBlockBody(node.getThenStmts());
        addToken(Token.ELSE);
        visitControlBlockBody(node.getElseStmts());
    }

    @Override
    public void visit(UntilStmt node) {
        visitControlBlockHead(node, Token.CONTROL_REPEAT_UNTIL, node.getBoolExpr());
        visitControlBlockBody(node.getStmtList());
    }

    private void visitControlBlockHead(final Stmt stmt, final Token opcode, final Expression condition) {
        if (shouldBeMasked(stmt)) {
            addToken(Token.MASK);
        }
        else {
            addToken(opcode);
            condition.accept(this);
        }
    }

    private void visitControlBlockBody(final StmtList stmtList) {
        addToken(Token.BEGIN);
        stmtList.accept(this);
        addToken(Token.END);
    }

    @Override
    public void visit(StopAll node) {
        visitStop("all");
    }

    @Override
    public void visit(StopOtherScriptsInSprite node) {
        visitStop("other_scripts");
    }

    @Override
    public void visit(StopThisScript node) {
        visitStop("this_script");
    }

    private void visitStop(final String target) {
        addToken(Token.CONTROL_STOP);
        if (abstractFixedNodeOptions) {
            addToken("stop_target");
        }
        else {
            addToken(target);
        }
    }

    // endregion control

    // region sensing

    @Override
    public void visit(Touching node) {
        visit(node, Token.SENSING_TOUCHINGOBJECT);
    }

    @Override
    public void visit(SpriteTouchingColor node) {
        visit(node, Token.SENSING_TOUCHINGCOLOR);
    }

    @Override
    public void visit(ColorTouchingColor node) {
        visit(node, Token.SENSING_COLORISTOUCHINGCOLOR);
    }

    @Override
    public void visit(DistanceTo node) {
        visit(node, Token.SENSING_DISTANCETO);
    }

    @Override
    public void visit(Answer node) {
        visit(node, Token.SENSING_ANSWER);
    }

    @Override
    public void visit(IsKeyPressed node) {
        visit(node, Token.SENSING_KEYPRESSED);
    }

    @Override
    public void visit(IsMouseDown node) {
        visit(node, Token.SENSING_MOUSEDOWN);
    }

    @Override
    public void visit(MouseX node) {
        visit(node, Token.SENSING_MOUSEX);
    }

    @Override
    public void visit(MouseY node) {
        visit(node, Token.SENSING_MOUSEY);
    }

    @Override
    public void visit(Loudness node) {
        visit(node, Token.SENSING_LOUDNESS);
    }

    @Override
    public void visit(Timer node) {
        visit(node, Token.SENSING_TIMER);
    }

    @Override
    public void visit(AttributeOf node) {
        visit(node, Token.SENSING_OF);
    }

    @Override
    public void visit(Current node) {
        visit(node, Token.SENSING_CURRENT);
    }

    @Override
    public void visit(DaysSince2000 node) {
        visit(node, Token.SENSING_DAYSSINCE2000);
    }

    @Override
    public void visit(Username node) {
        visit(node, Token.SENSING_USERNAME);
    }

    @Override
    public void visit(TimeComp node) {
        visitFixedNodeOption(node, Token.TIME_COMP);
    }

    @Override
    public void visit(Key node) {
        // TODO: Use 'abstractFixedNodeOptions' parameter here
        if (isAbstractTokens()) {
            addToken(Token.KEY);
        }
        else {
            visit(node, Token.KEY);
        }
    }

    @Override
    public void visit(Edge node) {
        visit(node, Token.SENSING_EDGE);
    }

    // endregion sensing

    // region operators

    @Override
    public void visit(Add node) {
        visit(node, Token.OPERATOR_ADD);
    }

    @Override
    public void visit(Minus node) {
        visit(node, Token.OPERATOR_SUBTRACT);
    }

    @Override
    public void visit(Mult node) {
        visit(node, Token.OPERATOR_MULTIPLY);
    }

    @Override
    public void visit(Div node) {
        visit(node, Token.OPERATOR_DIVIDE);
    }

    @Override
    public void visit(PickRandom node) {
        visit(node, Token.OPERATOR_RANDOM);
    }

    @Override
    public void visit(BiggerThan node) {
        visit(node, Token.OPERATOR_GT);
    }

    @Override
    public void visit(LessThan node) {
        visit(node, Token.OPERATOR_LT);
    }

    @Override
    public void visit(Equals node) {
        visit(node, Token.OPERATOR_EQUALS);
    }

    @Override
    public void visit(And node) {
        visit(node, Token.OPERATOR_AND);
    }

    @Override
    public void visit(Or node) {
        visit(node, Token.OPERATOR_OR);
    }

    @Override
    public void visit(Not node) {
        visit(node, Token.OPERATOR_NOT);
    }

    @Override
    public void visit(Join node) {
        visit(node, Token.OPERATOR_JOIN);
    }

    @Override
    public void visit(LetterOf node) {
        visit(node, Token.OPERATOR_LETTER_OF);
    }

    @Override
    public void visit(LengthOfString node) {
        visit(node, Token.OPERATOR_LENGTH);
    }

    @Override
    public void visit(StringContains node) {
        visit(node, Token.OPERATOR_CONTAINS);
    }

    @Override
    public void visit(Mod node) {
        visit(node, Token.OPERATOR_MOD);
    }

    @Override
    public void visit(Round node) {
        visit(node, Token.OPERATOR_ROUND);
    }

    @Override
    public void visit(NumFunctOf node) {
        visit(node, Token.OPERATOR_MATHOP);
    }

    @Override
    public void visit(NumFunct node) {
        visitFixedNodeOption(node, Token.NUM_FUNCT);
    }

    // endregion operators

    // region variables

    @Override
    public void visit(ItemOfVariable node) {
        visit(node, Token.DATA_ITEMOFLIST);
    }

    @Override
    public void visit(IndexOf node) {
        visit(node, Token.DATA_ITEMNUMOFLIST);
    }

    @Override
    public void visit(LengthOfVar node) {
        visit(node, Token.DATA_LENGTHOFLIST);
    }

    @Override
    public void visit(ListContains node) {
        visit(node, Token.DATA_LISTCONTAINSITEM);
    }

    // endregion variables

    // region expressions

    @Override
    public void visit(NumberLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_NUMBER, getNormalisedToken(node));
    }

    @Override
    public void visit(StringLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_STRING, getNormalisedToken(node));
    }

    @Override
    public void visit(BoolLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_BOOL, getNormalisedToken(node));
    }

    @Override
    public void visit(ColorLiteral node) {
        ifAbstractElse(AbstractToken.LITERAL_COLOR, getNormalisedToken(node));
    }

    @Override
    public void visit(UnspecifiedExpression node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedBoolExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedNumExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(UnspecifiedStringExpr node) {
        addToken(Token.NOTHING);
    }

    @Override
    public void visit(NameNum node) {
        visitFixedNodeOption(node, Token.LOOKS_BACKDROPNUMBERNAME);
    }

    @Override
    public void visit(FixedAttribute node) {
        visitFixedNodeOption(node, Token.ATTRIBUTE);
    }

    @Override
    public void visit(StrId node) {
        ifAbstractElse(AbstractToken.NODE_IDENTIFIER, getNormalisedToken(node));
    }

    @Override
    public void visit(Parameter node) {
        if (isAbstractTokens()) {
            addToken(AbstractToken.PARAMETER);
        }
        else {
            node.getName().accept(this);
        }
    }

    @Override
    public void visit(Variable node) {
        final String name = StringUtil.normaliseString(node.getName().getName());
        ifAbstractElse(AbstractToken.VAR, name);
    }

    @Override
    public void visit(Qualified node) {
        // we don't care about the scope of variables
        node.getSecond().accept(this);
    }

    @Override
    public void visit(ScratchList node) {
        final String name = StringUtil.normaliseString(node.getName().getName());
        ifAbstractElse(AbstractToken.LIST, name);
    }

    // endregion expressions

    // region tts

    @Override
    public void visit(Language node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(FixedLanguage node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(ExprLanguage node) {
        visit(node, Token.TTS_LANGUAGE);
    }

    @Override
    public void visit(Voice node) {
        visit(node, Token.TTS_VOICE);
    }

    @Override
    public void visit(FixedVoice node) {
        visit(node, Token.TTS_VOICE);
    }

    @Override
    public void visit(ExprVoice node) {
        visit(node, Token.TTS_VOICE);
    }

    // endregion tts

    // region music

    @Override
    public void visit(Tempo node) {
        visit(node, Token.MUSIC_TEMPO);
    }

    @Override
    public void visit(FixedNote node) {
        ifAbstractElse(Token.MUSIC_LITERAL_NOTE, getNormalisedToken(node));
    }

    @Override
    public void visit(FixedInstrument node) {
        visitFixedNodeOption(node, Token.MUSIC_LITERAL_INSTRUMENT);
    }

    @Override
    public void visit(FixedDrum node) {
        visitFixedNodeOption(node, Token.MUSIC_LITERAL_DRUM);
    }

    // endregion music

    // region helper methods

    private String getBlockId(final ASTNode node) {
        if (node.getMetadata() instanceof DataBlockMetadata block) {
            return block.getBlockId();
        }
        else if (node.getMetadata() instanceof NonDataBlockMetadata block) {
            return block.getBlockId();
        }
        else if (node instanceof AttributeFromFixed attribute) {
            return getBlockId(attribute.getParentNode());
        }

        return null;
    }

    private void visitFixedNodeOption(final FixedNodeOption option, final Token opcode) {
        if (
            MaskingType.FixedOption.equals(getMaskingStrategy().getMaskingType())
                && getMaskingStrategy().getBlockId().equals(getBlockId(option.getParentNode()))
        ) {
            addToken(Token.MASK);
        }
        else {
            if (abstractFixedNodeOptions) {
                visit(option, opcode);
            }
            else {
                visit(option, getNormalisedToken(option));
            }
        }
    }

    private void ifAbstractElse(final Token opcode, final String token) {
        if (isAbstractTokens()) {
            addToken(opcode);
        }
        else {
            addToken(token);
        }
    }

    private void ifAbstractElse(final AbstractToken abstractToken, final String token) {
        if (isAbstractTokens()) {
            addToken(abstractToken);
        }
        else {
            addToken(token);
        }
    }

    private String getNormalisedToken(final ASTNode node) {
        return TokenVisitorFactory.getToken(tokenVisitor, node);
    }

    // endregion helper methods
}
