/*
 * Copyright (C) 2021-2024 EmbeddedKittens contributors
 *
 * This file is part of EmbeddedKittens.
 *
 * EmbeddedKittens is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * EmbeddedKittens is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EmbeddedKittens. If not, see <http://www.gnu.org/licenses/>.
 *
 * SPDX-FileCopyrightText: 2021-2024 EmbeddedKittens contributors
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package de.uni_passau.fim.se2.embedded_kittens.tokenizer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public enum Token {

    UNKNOWN("UNKNOWN"),
    MASK("[MASK]"),
    NOTHING("NOTHING"),
    ELSE("ELSE"),

    BEGIN_BOOL_EXPR("<"),
    BEGIN_NUM_STR_EXPR("("),
    BEGIN_PROCEDURE("BEGIN_PROCEDURE"),
    BEGIN_SCRIPT("BEGIN_SCRIPT"),
    BEGIN_SPRITE("BEGIN_SPRITE"),
    BEGIN_SUBSTACK("BEGIN_SUBSTACK"),
    END_BOOL_EXPR(">"),
    END_NUM_STR_EXPR(")"),
    END_PROCEDURE("END_PROCEDURE"),
    END_SCRIPT("END_SCRIPT"),
    END_SPRITE("END_SPRITE"),
    END_SUBSTACK("END_SUBSTACK"),

    ATTRIBUTE("attribute"),
    KEY("key"),
    NUM_FUNCT("num_funct"),
    TIME_COMP("time_comp"),

    CONTROL_CREATE_CLONE_OF("control_create_clone_of"),
    CONTROL_DELETE_THIS_CLONE("control_delete_this_clone"),
    CONTROL_FOREVER("control_forever"),
    CONTROL_IF("control_if"),
    CONTROL_IF_ELSE("control_if_else"),
    CONTROL_REPEAT("control_repeat"),
    CONTROL_REPEAT_UNTIL("control_repeat_until"),
    CONTROL_START_AS_CLONE("control_start_as_clone"),
    CONTROL_STOP("control_stop"),
    CONTROL_WAIT("control_wait"),
    CONTROL_WAIT_UNTIL("control_wait_until"),
    DATA_ADDTOLIST("data_addtolist"),
    DATA_CHANGEVARIABLEBY("data_changevariableby"),
    DATA_DELETEALLOFLIST("data_deletealloflist"),
    DATA_DELETEOFLIST("data_deleteoflist"),
    DATA_HIDELIST("data_hidelist"),
    DATA_HIDEVARIABLE("data_hidevariable"),
    DATA_INSERTATLIST("data_insertatlist"),
    DATA_ITEMNUMOFLIST("data_itemnumoflist"),
    DATA_ITEMOFLIST("data_itemoflist"),
    DATA_LENGTHOFLIST("data_lengthoflist"),
    DATA_LISTCONTAINSITEM("data_listcontainsitem"),
    DATA_REPLACEITEMOFLIST("data_replaceitemoflist"),
    DATA_SETVARIABLETO("data_setvariableto"),
    DATA_SHOWLIST("data_showlist"),
    DATA_SHOWVARIABLE("data_showvariable"),
    DATA_VARIABLE("data_variable"),
    EVENT_ATTRIBUTE("event_attribute"),
    EVENT_BROADCAST("event_broadcast"),
    EVENT_BROADCASTANDWAIT("event_broadcastandwait"),
    EVENT_MESSAGE("event_message"),
    EVENT_NEVER("event_never"),
    EVENT_WHENBACKDROPSWITCHESTO("event_whenbackdropswitchesto"),
    EVENT_WHENBROADCASTRECEIVED("event_whenbroadcastreceived"),
    EVENT_WHENFLAGCLICKED("event_whenflagclicked"),
    EVENT_WHENGREATERTHAN("event_whengreaterthan"),
    EVENT_WHENKEYPRESSED("event_whenkeypressed"),
    EVENT_WHENSTAGECLICKED("event_whenstageclicked"),
    EVENT_WHENTHISSPRITECLICKED("event_whenthisspriteclicked"),
    LOOKS_BACKDROP("looks_backdrop"),
    LOOKS_BACKDROPNUMBERNAME("looks_backdropnumbername"),
    LOOKS_CHANGEEFFECTBY("looks_changeeffectby"),
    LOOKS_CHANGESIZEBY("looks_changesizeby"),
    LOOKS_CLEARGRAPHICEFFECTS("looks_cleargraphiceffects"),
    LOOKS_COSTUMENUMBERNAME("looks_costumenumbername"),
    LOOKS_FORWARDBACKWARD("looks_forwardbackward"),
    LOOKS_GOFORWARDBACKWARDLAYERS("looks_goforwardbackwardlayers"),
    LOOKS_GOTOFRONTBACK("looks_gotofrontback"),
    LOOKS_GRAPHICEFFECT("looks_graphiceffect"),
    LOOKS_HIDE("looks_hide"),
    LOOKS_LAYERCHOICE("looks_layerchoice"),
    LOOKS_NEXTBACKDROP("looks_nextbackdrop"),
    LOOKS_NEXTBACKDROPCHOICE("looks_nextbackdropchoice"),
    LOOKS_NEXTCOSTUME("looks_nextcostume"),
    LOOKS_PREVBACKDROPCHOICE("looks_prevbackdropchoice"),
    LOOKS_RANDOMBACKDROPCHOICE("looks_randombackdropchoice"),
    LOOKS_SAY("looks_say"),
    LOOKS_SAYFORSECS("looks_sayforsecs"),
    LOOKS_SETEFFECTTO("looks_seteffectto"),
    LOOKS_SETSIZETO("looks_setsizeto"),
    LOOKS_SHOW("looks_show"),
    LOOKS_SIZE("looks_size"),
    LOOKS_SWITCHBACKDROPTO("looks_switchbackdropto"),
    LOOKS_SWITCHBACKDROPTOANDWAIT("looks_switchbackdroptoandwait"),
    LOOKS_SWITCHCOSTUMETO("looks_switchcostumeto"),
    LOOKS_THINK("looks_think"),
    LOOKS_THINKFORSECS("looks_thinkforsecs"),
    MOTION_CHANGEXBY("motion_changexby"),
    MOTION_CHANGEYBY("motion_changeyby"),
    MOTION_DIRECTION("motion_direction"),
    MOTION_DRAGMODE("motion_dragmode"),
    MOTION_GLIDESECSTO("motion_glidesecsto"),
    MOTION_GLIDESECSTOXY("motion_glidesecstoxy"),
    MOTION_GOTO("motion_goto"),
    MOTION_GOTOXY("motion_gotoxy"),
    MOTION_IFONEDGEBOUNCE("motion_ifonedgebounce"),
    MOTION_MOUSEPOINTER("motion_mousepointer"),
    MOTION_MOUSEPOS("motion_mousepos"),
    MOTION_MOVESTEPS("motion_movesteps"),
    MOTION_POINTINDIRECTION("motion_pointindirection"),
    MOTION_POINTTOWARDS("motion_pointtowards"),
    MOTION_RANDOMPOS("motion_randompos"),
    MOTION_ROTATIONSTYLE("motion_rotationstyle"),
    MOTION_SETROTATIONSTYLE("motion_setrotationstyle"),
    MOTION_SETX("motion_setx"),
    MOTION_SETY("motion_sety"),
    MOTION_TURNLEFT("motion_turnleft"),
    MOTION_TURNRIGHT("motion_turnright"),
    MOTION_XPOSITION("motion_xposition"),
    MOTION_YPOSITION("motion_yposition"),
    MUSIC_CHANGETEMPOBY("music_changetempoby"),
    MUSIC_LITERAL_DRUM("music_drumliteral"),
    MUSIC_LITERAL_INSTRUMENT("music_instrumentliteral"),
    MUSIC_LITERAL_NOTE("music_noteliteral"),
    MUSIC_PLAYDRUMFORBEATS("music_playdrumforbeats"),
    MUSIC_PLAYNOTEFORBEATS("music_playnoteforbeats"),
    MUSIC_RESTFORBEATS("music_restforbeats"),
    MUSIC_SETINSTRUMENTTO("music_setinstrumentto"),
    MUSIC_SETTEMPOTO("music_settempoto"),
    MUSIC_TEMPO("music_tempo"),
    OPERATOR_ADD("operator_add"),
    OPERATOR_AND("operator_and"),
    OPERATOR_CONTAINS("operator_contains"),
    OPERATOR_DIVIDE("operator_divide"),
    OPERATOR_EQUALS("operator_equals"),
    OPERATOR_GT("operator_gt"),
    OPERATOR_JOIN("operator_join"),
    OPERATOR_LENGTH("operator_length"),
    OPERATOR_LETTER_OF("operator_letter_of"),
    OPERATOR_LT("operator_lt"),
    OPERATOR_MATHOP("operator_mathop"),
    OPERATOR_MOD("operator_mod"),
    OPERATOR_MULTIPLY("operator_multiply"),
    OPERATOR_NOT("operator_not"),
    OPERATOR_OR("operator_or"),
    OPERATOR_RANDOM("operator_random"),
    OPERATOR_ROUND("operator_round"),
    OPERATOR_SUBTRACT("operator_subtract"),
    PEN_CHANGECOLORBY("pen_changecolorby"),
    PEN_CHANGEPENSIZEBY("pen_changepensizeby"),
    PEN_CLEAR("pen_clear"),
    PEN_PENDOWN("pen_pendown"),
    PEN_PENUP("pen_penup"),
    PEN_SETCOLORTO("pen_setcolorto"),
    PEN_SETPENCOLORTOCOLOR("pen_setpencolortocolor"),
    PEN_SETPENSIZETO("pen_setpensizeto"),
    PEN_STAMP("pen_stamp"),
    SENSING_ANSWER("sensing_answer"),
    SENSING_ASKANDWAIT("sensing_askandwait"),
    SENSING_COLORISTOUCHINGCOLOR("sensing_coloristouchingcolor"),
    SENSING_CURRENT("sensing_current"),
    SENSING_DAYSSINCE2000("sensing_dayssince2000"),
    SENSING_DISTANCETO("sensing_distanceto"),
    SENSING_EDGE("sensing_edge"),
    SENSING_KEYPRESSED("sensing_keypressed"),
    SENSING_LOUDNESS("sensing_loudness"),
    SENSING_MOUSEDOWN("sensing_mousedown"),
    SENSING_MOUSEX("sensing_mousex"),
    SENSING_MOUSEY("sensing_mousey"),
    SENSING_OF("sensing_of"),
    SENSING_RESETTIMER("sensing_resettimer"),
    SENSING_SETDRAGMODE("sensing_setdragmode"),
    SENSING_TIMER("sensing_timer"),
    SENSING_TOUCHINGCOLOR("sensing_touchingcolor"),
    SENSING_TOUCHINGOBJECT("sensing_touchingobject"),
    SENSING_USERNAME("sensing_username"),
    SOUND_CHANGEEFFECTBY("sound_changeeffectby"),
    SOUND_CHANGEVOLUMEBY("sound_changevolumeby"),
    SOUND_CLEAREFFECTS("sound_cleareffects"),
    SOUND_EFFECT("sound_effectchoice"),
    SOUND_PLAY("sound_play"),
    SOUND_PLAYUNTILDONE("sound_playuntildone"),
    SOUND_SETEFFECTTO("sound_seteffectto"),
    SOUND_SETVOLUMETO("sound_setvolumeto"),
    SOUND_STOPALLSOUNDS("sound_stopallsounds"),
    SOUND_VOLUME("sound_volume"),
    TTS_LANGUAGE("tts_language"),
    TTS_SETLANGUAGE("tts_setlanguage"),
    TTS_SETVOICE("tts_setvoice"),
    TTS_SPEAK("tts_speak"),
    TTS_VOICE("tts_voice");

    private static final Set<Token> SPECIAL_TOKENS = Arrays.stream(Token.values())
        .filter(token -> !token.getStrRep().matches("^[a-z0-9_]+$"))
        .collect(Collectors.toUnmodifiableSet());

    Token(final String strRep) {
        this.strRep = strRep;
    }

    private final String strRep;

    public String getStrRep() {
        return strRep;
    }

    /**
     * Retrieves all special tokens that do not represent a concrete block.
     *
     * <p>
     * Examples: {@link Token#ELSE}, {@link Token#BEGIN_SUBSTACK}, {@link Token#BEGIN_NUM_STR_EXPR}, …
     *
     * @return A set of special tokens.
     */
    public static Set<Token> getSpecialTokens() {
        return SPECIAL_TOKENS;
    }

    @Override
    public String toString() {
        return strRep;
    }
}
