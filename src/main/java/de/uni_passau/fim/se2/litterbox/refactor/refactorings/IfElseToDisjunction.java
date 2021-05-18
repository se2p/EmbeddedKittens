package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.expression.bool.Or;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfStmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class IfElseToDisjunction extends CloneVisitor implements Refactoring {

    public static final String NAME = "ifelse_to_disjunction";

    private final IfStmt if1;
    private final IfThenStmt if2;
    private final IfThenStmt replacement;

    /*
    TODO: This refactoring could also handle:
      if A:
        foo
      if B:
        foo

      to

      if A or B:
        foo

      but it is currently only applied to:

      if A:
        foo
      else:
        if B:
          foo
     */
    public IfElseToDisjunction(IfStmt if1, IfThenStmt if2) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = Preconditions.checkNotNull(if2);

        Preconditions.checkArgument(if1.getThenStmts().equals(if2.getThenStmts()));
        Preconditions.checkArgument(!if1.getBoolExpr().equals(if2.getBoolExpr()));

        CloneVisitor cloneVisitor = new CloneVisitor();
        Or disjunction = new Or(
                cloneVisitor.apply(if1.getBoolExpr()),
                cloneVisitor.apply(if2.getBoolExpr()),
                if2.getMetadata());

        replacement = new IfThenStmt(disjunction,
                cloneVisitor.apply(if1.getThenStmts()),
                cloneVisitor.apply(if1.getMetadata()));
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (stmt == if1) {
                statements.add(replacement);
            } else if (stmt != if2) {
                statements.add(apply(stmt));
            }
        }
        return new StmtList(statements);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof IfElseToDisjunction)) {
            return false;
        }
        return if1.equals(((IfElseToDisjunction) other).if1)
                && if2.equals(((IfElseToDisjunction) other).if2);
    }

    @Override
    public int hashCode() {
        return if1.hashCode() + if2.hashCode();
    }
}
