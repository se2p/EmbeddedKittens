package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.DisjunctionToIfElseFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class DisjunctionToIfElseTest implements JsonTest {

    @Test
    public void testDisjunctionToIfElseFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/disjunctionToIfElse.json");
        DisjunctionToIfElseFinder finder = new DisjunctionToIfElseFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(DisjunctionToIfElse.class);
    }

    @Test
    public void testDisjunctionToIfElseRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/disjunctionToIfElse.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();

        IfThenStmt ifStatement1 = (IfThenStmt) stmtList.getStatement(0);
        Or disjunction = (Or) ifStatement1.getBoolExpr();

        DisjunctionToIfElse refactoring = new DisjunctionToIfElse(ifStatement1);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(1);

        IfElseStmt ifElse = (IfElseStmt) refactoredStmtList.getStatement(0);
        IfThenStmt ifStatement2 = (IfThenStmt) ifElse.getElseStmts().getStatement(0);

        assertThat(disjunction.getOperand1()).isEqualTo(ifElse.getBoolExpr());
        assertThat(disjunction.getOperand2()).isEqualTo(ifStatement2.getBoolExpr());
        assertThat(ifElse.getThenStmts()).isEqualTo(ifStatement1.getThenStmts());
        assertThat(ifStatement2.getThenStmts()).isEqualTo(ifStatement2.getThenStmts());
    }
}
