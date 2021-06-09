package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.JsonTest;
import de.uni_passau.fim.se2.litterbox.analytics.refactorings.ConjunctionToIfsFinder;
import de.uni_passau.fim.se2.litterbox.ast.ParsingException;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.Script;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.And;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class ConjunctionToIfsTest implements JsonTest {

    @Test
    public void testConjunctionToIfsFinder() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/conjunctionToIfs.json");
        ConjunctionToIfsFinder finder = new ConjunctionToIfsFinder();
        List<Refactoring> refactorings = finder.check(program);
        assertThat(refactorings).hasSize(1);
        assertThat(refactorings.get(0)).isInstanceOf(ConjunctionToIfs.class);
    }

    @Test
    public void testConjunctionToIfsRefactoring() throws ParsingException, IOException {
        Program program = getAST("src/test/fixtures/refactoring/conjunctionToIfs.json");
        Script script = program.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList stmtList = script.getStmtList();
        IfThenStmt ifStatement = (IfThenStmt) stmtList.getStatement(0);
        And conjunction = (And) ifStatement.getBoolExpr();

        ConjunctionToIfs refactoring = new ConjunctionToIfs(ifStatement);
        Program refactored = refactoring.apply(program);

        Script refactoredScript = refactored.getActorDefinitionList().getDefinitions().get(1).getScripts().getScriptList().get(0);
        StmtList refactoredStmtList = refactoredScript.getStmtList();
        assertThat(refactoredStmtList.getNumberOfStatements()).isEqualTo(1);

        IfThenStmt if1 = (IfThenStmt) refactoredStmtList.getStatement(0);
        IfThenStmt if2 = (IfThenStmt) if1.getThenStmts().getStatement(0);

        assertThat(conjunction.getOperand1()).isEqualTo(if1.getBoolExpr());
        assertThat(conjunction.getOperand2()).isEqualTo(if2.getBoolExpr());
        assertThat(if2.getThenStmts()).isEqualTo(ifStatement.getThenStmts());
    }
}
