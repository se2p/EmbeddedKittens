package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.literals.NumberLiteral;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.RepeatTimesStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.StatementReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SequenceToLoop extends CloneVisitor implements Refactoring {

    public static final String NAME = "sequence_to_loop";

    private final int times;
    private final Stmt targetStatement;
    private final List<Stmt> repeatedStatements;
    private final List<Stmt> allRepeatedStatements;
    private final RepeatTimesStmt replacementLoop;

    public SequenceToLoop(StmtList stmtList, List<Stmt> repeatedStatements, int times) {
        Preconditions.checkArgument(repeatedStatements.size() > 0);
        this.targetStatement = repeatedStatements.get(0);
        this.repeatedStatements = repeatedStatements;
        this.times = times;

        this.allRepeatedStatements = new ArrayList<>();
        int startingpoint = stmtList.getStmts().indexOf(targetStatement);
        for (int i = startingpoint; i < startingpoint + (times * repeatedStatements.size()); i++) {
            allRepeatedStatements.add(stmtList.getStatement(i));
        }

        StmtList loopBody = new StmtList(repeatedStatements);
        replacementLoop = new RepeatTimesStmt(new NumberLiteral(times), loopBody, apply(targetStatement.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(new StatementReplacementVisitor(targetStatement, allRepeatedStatements, Arrays.asList(replacementLoop)));
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + System.lineSeparator() +
                "Summarised " + times +" repetitions to:" + System.lineSeparator() +
                replacementLoop.getScratchBlocks() + System.lineSeparator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SequenceToLoop)) return false;
        SequenceToLoop that = (SequenceToLoop) o;
        return times == that.times && Objects.equals(targetStatement, that.targetStatement) && Objects.equals(repeatedStatements, that.repeatedStatements) && Objects.equals(replacementLoop, that.replacementLoop);
    }

    @Override
    public int hashCode() {
        return Objects.hash(times, targetStatement, repeatedStatements, replacementLoop);
    }
}
