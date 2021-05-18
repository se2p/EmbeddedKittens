package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfElseStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.NodeReplacementVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.Objects;

public class DisjunctionToIfElse extends CloneVisitor implements Refactoring {

    public static final String NAME = "disjunction_to_ifelse";

    private final IfThenStmt ifStatement;
    private final IfElseStmt replacementIf;

    public DisjunctionToIfElse(IfThenStmt ifStatement) {
        this.ifStatement = Preconditions.checkNotNull(ifStatement);
        Or disjunction = (Or) ifStatement.getBoolExpr();

        CloneVisitor cloneVisitor = new CloneVisitor();
        IfThenStmt innerIf = new IfThenStmt(cloneVisitor.apply(disjunction.getOperand2()),
                cloneVisitor.apply(ifStatement.getThenStmts()), ifStatement.getMetadata());

        replacementIf = new IfElseStmt(cloneVisitor.apply(disjunction.getOperand1()),
                cloneVisitor.apply(ifStatement.getThenStmts()),
                new StmtList(innerIf),
                cloneVisitor.apply(ifStatement.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        NodeReplacementVisitor replacementVisitor =  new NodeReplacementVisitor(ifStatement, replacementIf);
        return (Program) program.accept(replacementVisitor);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DisjunctionToIfElse)) return false;
        DisjunctionToIfElse that = (DisjunctionToIfElse) o;
        return Objects.equals(ifStatement, that.ifStatement) && Objects.equals(replacementIf, that.replacementIf);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ifStatement, replacementIf);
    }
}
