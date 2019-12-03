package scratch.ast.parser;

import static junit.framework.TestCase.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.truth.Truth;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import scratch.ast.ParsingException;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.Script;
import scratch.ast.model.StmtList;
import scratch.ast.model.expression.bool.And;
import scratch.ast.model.expression.bool.BiggerThan;
import scratch.ast.model.expression.bool.ColorTouches;
import scratch.ast.model.expression.bool.Equals;
import scratch.ast.model.expression.bool.ExpressionContains;
import scratch.ast.model.expression.bool.IsKeyPressed;
import scratch.ast.model.expression.bool.IsMouseDown;
import scratch.ast.model.expression.bool.LessThan;
import scratch.ast.model.expression.bool.Not;
import scratch.ast.model.expression.bool.Or;
import scratch.ast.model.expression.bool.Touching;
import scratch.ast.model.expression.num.AsNumber;
import scratch.ast.model.expression.string.AttributeOf;
import scratch.ast.model.literals.ColorLiteral;
import scratch.ast.model.literals.NumberLiteral;
import scratch.ast.model.literals.StringLiteral;
import scratch.ast.model.statement.Stmt;
import scratch.ast.model.statement.common.WaitSeconds;
import scratch.ast.model.statement.common.WaitUntil;
import scratch.ast.model.statement.control.IfElseStmt;
import scratch.ast.model.statement.control.IfThenStmt;
import scratch.ast.model.statement.control.RepeatTimesStmt;
import scratch.ast.model.touchable.Edge;

public class BoolExprParserTest {

    private static JsonNode project;

    @BeforeAll
    public static void setup() {
        String path = "src/test/fixtures/boolExprBlocks.json";
        File file = new File(path);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            project = objectMapper.readTree(file);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void testContains() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(1);
        Truth.assertThat(stmt).isInstanceOf(IfThenStmt.class);

        IfThenStmt ifThenStmt = (IfThenStmt) stmt;
        ExpressionContains containsExpr = (ExpressionContains) ifThenStmt.getBoolExpr();
        Truth.assertThat(containsExpr.getContaining()).isInstanceOf(AttributeOf.class);
        Truth.assertThat(containsExpr.getContained()).isInstanceOf(StringLiteral.class);
    }

    @Test
    public void testOr() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(2);
        Truth.assertThat(stmt).isInstanceOf(RepeatTimesStmt.class);

        RepeatTimesStmt repeatTimesStmt = (RepeatTimesStmt) stmt;
        StmtList substack = repeatTimesStmt.getStmtList();

        final Stmt subStackStmt = substack.getStmts().getListOfStmt().get(0);
        Truth.assertThat(subStackStmt).isInstanceOf(IfElseStmt.class);

        IfElseStmt ifThenStmt = (IfElseStmt) subStackStmt;
        Or orExpr = (Or) ifThenStmt.getBoolExpr();
        Truth.assertThat(orExpr.getOperand1()).isInstanceOf(LessThan.class);
        Truth.assertThat(orExpr.getOperand2()).isInstanceOf(Equals.class);
    }

    @Test
    public void testWaitUntil() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(3);
        Truth.assertThat(stmt).isInstanceOf(WaitUntil.class);

        WaitUntil waitUntil = (WaitUntil) stmt;
        IsKeyPressed expr = (IsKeyPressed) waitUntil.getUntil();
        Truth.assertThat(((NumberLiteral) expr.getKey().getKey()).getValue()).isEqualTo( 32);
    }

    @Test
    public void testAnd() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(4);
        Truth.assertThat(stmt).isInstanceOf(WaitSeconds.class);

        WaitSeconds wait = (WaitSeconds) stmt;
        And andExpr = (And) ((AsNumber) wait.getSeconds()).getExpr();
        Truth.assertThat(andExpr.getOperand1()).isInstanceOf(IsMouseDown.class);
        Truth.assertThat(andExpr.getOperand2()).isInstanceOf(IsMouseDown.class);
    }

    @Test
    public void testNot() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(0);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(5);
        Truth.assertThat(stmt).isInstanceOf(RepeatTimesStmt.class);

        RepeatTimesStmt repeatTimesStmt = (RepeatTimesStmt) stmt;
        IfElseStmt ifElseStmt = (IfElseStmt) repeatTimesStmt.getStmtList().getStmts().getListOfStmt().get(0);
        Truth.assertThat(ifElseStmt.getBoolExpr()).isInstanceOf(Not.class);
        Not boolExpr = (Not) ifElseStmt.getBoolExpr();
        Truth.assertThat(boolExpr.getOperand1()).isInstanceOf(BiggerThan.class);

    }

    @Test
    public void testBTLiteral() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(1);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(0);
        Truth.assertThat(stmt).isInstanceOf(IfThenStmt.class);

        IfThenStmt ifThenStmt = (IfThenStmt) stmt;
        Truth.assertThat(ifThenStmt.getBoolExpr()).isInstanceOf(BiggerThan.class);
        Truth.assertThat(((BiggerThan) ifThenStmt.getBoolExpr()).getOperand1()).isInstanceOf(NumberLiteral.class);
        Truth.assertThat(((BiggerThan) ifThenStmt.getBoolExpr()).getOperand2()).isInstanceOf(NumberLiteral.class);

    }

    @Test
    public void testLTLiteral() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(1);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(1);
        Truth.assertThat(stmt).isInstanceOf(IfThenStmt.class);

        IfThenStmt ifThenStmt = (IfThenStmt) stmt;
        Truth.assertThat(ifThenStmt.getBoolExpr()).isInstanceOf(LessThan.class);
        Truth.assertThat(((LessThan) ifThenStmt.getBoolExpr()).getOperand1()).isInstanceOf(NumberLiteral.class);
        Truth.assertThat(((LessThan) ifThenStmt.getBoolExpr()).getOperand2()).isInstanceOf(NumberLiteral.class);

    }

    @Test
    public void testEqLiteral() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(1);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(2);
        Truth.assertThat(stmt).isInstanceOf(IfThenStmt.class);

        IfThenStmt ifThenStmt = (IfThenStmt) stmt;
        Truth.assertThat(ifThenStmt.getBoolExpr()).isInstanceOf(Equals.class);
        Truth.assertThat(((Equals) ifThenStmt.getBoolExpr()).getOperand1()).isInstanceOf(NumberLiteral.class);
        Truth.assertThat(((Equals) ifThenStmt.getBoolExpr()).getOperand2()).isInstanceOf(NumberLiteral.class);

    }

    @Test
    public void testTouching() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(1);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(3);
        Truth.assertThat(stmt).isInstanceOf(IfThenStmt.class);

        IfThenStmt ifThenStmt = (IfThenStmt) stmt;
        Truth.assertThat(ifThenStmt.getBoolExpr()).isInstanceOf(Touching.class);
        Truth.assertThat(((Touching) ifThenStmt.getBoolExpr()).getTouchable()).isInstanceOf(Edge.class);

    }

    @Test
    public void testTouchingTwoColors() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(1);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(4);
        Truth.assertThat(stmt).isInstanceOf(IfThenStmt.class);

        IfThenStmt ifThenStmt = (IfThenStmt) stmt;
        Truth.assertThat(ifThenStmt.getBoolExpr()).isInstanceOf(ColorTouches.class);
        Truth.assertThat(((ColorTouches) ifThenStmt.getBoolExpr()).getOperand1()).isInstanceOf(ColorLiteral.class);
        Truth.assertThat(((ColorTouches) ifThenStmt.getBoolExpr()).getOperand2()).isInstanceOf(ColorLiteral.class);

    }

    @Test
    public void testTouchingOneColor() throws ParsingException {
        Program program = ProgramParser.parseProgram("ListExpr", project);
        final ActorDefinition sprite = program.getActorDefinitionList().getDefintions().get(1);
        final Script script = sprite.getScripts().getScriptList().get(1);

        final Stmt stmt = script.getStmtList().getStmts().getListOfStmt().get(5);
        Truth.assertThat(stmt).isInstanceOf(IfThenStmt.class);

        IfThenStmt ifThenStmt = (IfThenStmt) stmt;
        Truth.assertThat(ifThenStmt.getBoolExpr()).isInstanceOf(Touching.class);
        Truth.assertThat(((Touching) ifThenStmt.getBoolExpr()).getTouchable()).isInstanceOf(ColorLiteral.class);

    }

}