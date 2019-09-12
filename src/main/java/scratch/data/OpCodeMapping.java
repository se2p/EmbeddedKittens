package scratch.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static scratch.data.OpCodeMapping.BlockShape.BOOLEAN;
import static scratch.data.OpCodeMapping.BlockShape.C;
import static scratch.data.OpCodeMapping.BlockShape.CAP;
import static scratch.data.OpCodeMapping.BlockShape.HAT;
import static scratch.data.OpCodeMapping.BlockShape.REPORTER;
import static scratch.data.OpCodeMapping.BlockShape.STACK;

/**
 * The OpCodeMapping Class contains lists of all block opcodes of Scratch 2 and 3
 * sorted by their Block Shape and maps every block opcode to its
 * corresponding Block Shape.
 */
public class OpCodeMapping {

    /**
     * List of all opcodes of blocks which are Hat Blocks.
     * Scratch 3 opcodes are followed by their equivalent in Scratch 2.
     */
    public static final List<String> hatBlockOpcodes = new ArrayList<>(Arrays.asList(
            "event_whenflagclicked",
            "whenGreenFlag",
            "event_whenkeypressed",
            "whenKeyPressed",
            "event_whenthisspriteclicked",
            "whenClicked",
            "event_whenbroadcastreceived",
            "whenIReceive",
            "event_whenbackdropswitchesto",
            "whenSceneStarts",
            "control_start_as_clone",
            "whenCloned",
            "event_whengreaterthan",
            "whenSensorGreaterThan",
            "procedures_definition"
            // TODO find Scratch 2 opcode of custom block definition
    ));

    /**
     * List of all opcodes of blocks which are Stack Blocks.
     * Scratch 3 opcodes are followed by their equivalent in Scratch 2.
     */
    public static final List<String> stackBlockOpcodes = new ArrayList<>(Arrays.asList(
            "motion_movesteps",
            "forward:",
            "motion_turnright",
            "turnRight:",
            "motion_turnleft",
            "turnLeft:",
            "motion_goto",
            "gotoSpriteOrMouse:",
            "motion_gotoxy",
            "gotoX:y:",
            "motion_glideto",
            // There is no Glide Secs to block in Scratch 2.
            "motion_glidesecstoxy",
            "glideSecs:toX:y:elapsed:from:",
            "motion_pointindirection",
            "heading:",
            "motion_pointtowards",
            "pointTowards:",
            "motion_changexby",
            "changeXposBy:",
            "motion_setx",
            "xpos:",
            "motion_changeyby",
            "changeYposBy:",
            "motion_sety",
            "ypos:",
            "motion_ifonedgebounce",
            "bounceOffEdge",
            "motion_setrotationstyle",
            "setRotationStyle",
            "looks_sayforsecs",
            "say:duration:elapsed:from:",
            "looks_say",
            "say:",
            "looks_thinkforsecs",
            "hink:duration:elapsed:from:",
            "looks_think",
            "think:",
            "looks_switchcostumeto",
            "lookLike:",
            "looks_switchbackdropto",
            "startScene",
            "looks_nextcostume",
            "nextCostume",
            "looks_nextbackdrop",
            "nextScene",
            "looks_changesizeby",
            "changeSizeBy:",
            "looks_setsizeto",
            "setSizeTo:",
            "looks_changeeffectby",
            "changeGraphicEffect:by:",
            "looks_seteffectto",
            "setGraphicEffect:to:",
            "looks_cleargraphiceffects",
            "filterReset",
            "looks_show",
            "show",
            "looks_hide",
            "hide",
            "looks_gotofrontback",
            "comeToFront",
            "looks_goforwardbackwardlayers",
            "sound_playuntildone",
            "doPlaySoundAndWait",
            "sound_play",
            "playSound:",
            "sound_stopallsounds",
            "stopAllSounds",
            "sound_changeeffectby",
            "changeGraphicEffect:by:",
            "sound_seteffectto",
            "setGraphicEffect:to:",
            "sound_cleareffects",
            // There is no Clear Sound Effects block in Scratch 2.
            "sound_changevolumeby",
            "changeVolumeBy:",
            "sound_setvolumeto",
            "setVolumeTo:",
            "event_broadcast",
            "broadcast:",
            "event_broadcastandwait",
            "doBroadcastAndWait",
            "control_wait",
            "wait:elapsed:from:",
            "control_wait_until",
            "doWaitUntil",
            "control_create_clone_of",
            "createCloneOf",
            "sensing_askandwait",
            "doAsk",
            "sensing_setdragmode",
            // There is no Set Drag Mode block in Scratch 2.
            "sensing_resettimer",
            "timerReset",
            "data_setvariableto",
            "setVar:to:",
            "data_changevariableby",
            "changeVar:by:",
            "data_showvariable",
            "showVariable:",
            "data_hidevariable",
            "hideVariable:",
            "data_addtolist",
            "append:toList:",
            "data_deleteoflist",
            "deleteLine:ofList:",
            "data_deletealloflist",
            // There is no Delete All of block in Scratch 2.
            "data_insertatlist",
            "setLine:ofList:to:",
            "data_replaceitemoflist",
            "insert:at:ofList:",
            "data_showlist",
            "showList:",
            "data_hidelist",
            "hideList:",
            "procedures_call"
            // TODO find Scratch 2 opcode of custom block call
    ));

    /**
     * List of all opcodes of blocks which are Boolean Blocks.
     * Scratch 3 opcodes are followed by their equivalent in Scratch 2.
     */
    public static final List<String> booleanBlockOpcodes = new ArrayList<>(Arrays.asList(
            "sensing_touchingobject",
            "touching:",
            "sensing_touchingcolor",
            "touchingColor:",
            "sensing_coloristouchingcolor",
            "color:sees:",
            "sensing_keypressed",
            "keyPressed:",
            "sensing_mousedown",
            "mousePressed",
            "operator_gt",
            ">",
            "operator_lt",
            "<",
            "operator_equals",
            "=",
            "operator_and",
            "&",
            "operator_or",
            "|",
            "operator_not",
            "not",
            "operator_contains",
            "list:contains:",
            "data_listcontainsitem",
            "list:contains:"
    ));

    /**
     * List of all opcodes of blocks which are Reporter Blocks.
     * Scratch 3 opcodes are followed by their equival'ent in Scratch 2.
     * Variables and Lists are Reporter Blocks, too, but do not have an opcode.
     */
    public static final List<String> reporterBlockOpcodes = new ArrayList<>(Arrays.asList(
            "motion_xposition",
            "xpos",
            "motion_yposition",
            "ypos",
            "motion_direction",
            "heading",
            "looks_size",
            "scale",
            "looks_costumenumbername",
            "costumeIndex",
            "looks_backdropnumbername",
            "backgroundIndex ",
            "sound_volume",
            "volume",
            "sensing_distanceto",
            "distanceTo: ",
            "sensing_answer",
            "answer",
            "sensing_mousex",
            "mouseX",
            "sensing_mousey",
            "mouseY",
            "sensing_loudness",
            "soundLevel",
            "sensing_timer",
            "timer",
            "sensing_of",
            "getAttribute:of:",
            "sensing_current",
            "timeAndDate ",
            "sensing_dayssince2000",
            "timestamp",
            "sensing_username",
            "getUserName",
            "operator_add",
            "+",
            "operator_subtract",
            "-",
            "operator_multiply",
            "*",
            "operator_divide",
            "/",
            "operator_random",
            "randomFrom:to:",
            "operator_join",
            "concatenate:with:",
            "operator_letter_of",
            "letter:of: ",
            "operator_length",
            "stringLength: ",
            "operator_mod",
            "%",
            "operator_round",
            "round",
            "operator_mathop",
            "abs",
            "data_itemoflist",
            // There is no item Number of in block in Scratch 2.
            "data_itemnumoflist",
            "getLine:ofList:",
            "data_lengthoflist",
            "lineCountOfList:"
    ));

    /**
     * List of all opcodes of blocks which are C Blocks.
     * Scratch 3 opcodes are followed by their equivalent in Scratch 2.
     */
    public static final List<String> cBlockOpcodes = new ArrayList<>(Arrays.asList(
            "control_repeat",
            "doRepeat",
            "control_forever",
            "doForever",
            "control_if",
            "doIf",
            "control_if_else",
            "doIfElse",
            "control_repeat_until",
            "doUntil"
    ));

    /**
     * List of all opcodes of blocks which are Cap Blocks.
     * Scratch 3 opcodes are followed by their equivalent in Scratch 2.
     */
    public static final List<String> capBlockOpcodes = new ArrayList<>(Arrays.asList(
            "control_stop",
            "stopAll",
            "control_delete_this_clone",
            "deleteClone"
    ));

    /**
     * Maps every every block opcode to its corresponding Block Shape.
     * Contains both Scratch 2 and 3 block opcodes.
     */
    public static Map<String, BlockShape> opcodeShape;
    static {
        opcodeShape = new HashMap<>();
        for (String opcode : hatBlockOpcodes) {
            opcodeShape.put(opcode, HAT);
        }
        for (String opcode: stackBlockOpcodes) {
            opcodeShape.put(opcode, STACK);
        }
        for (String opcode: booleanBlockOpcodes) {
            opcodeShape.put(opcode, BOOLEAN);
        }
        for (String opcode: reporterBlockOpcodes) {
            opcodeShape.put(opcode, REPORTER);
        }
        for (String opcode: cBlockOpcodes) {
            opcodeShape.put(opcode, C);
        }
        for (String opcode: capBlockOpcodes) {
            opcodeShape.put(opcode, CAP);
        }
    }

    /**
     * All available Block Shapes of Scratch.
     */
    public enum BlockShape {
        HAT, STACK, BOOLEAN, REPORTER, C, CAP
    }
}
