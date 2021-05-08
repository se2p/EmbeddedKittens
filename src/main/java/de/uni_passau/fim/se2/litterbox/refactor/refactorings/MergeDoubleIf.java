package de.uni_passau.fim.se2.litterbox.refactor.refactorings;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.StmtList;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.Stmt;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.control.IfThenStmt;
import de.uni_passau.fim.se2.litterbox.ast.visitor.CloneVisitor;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchBlocksVisitor;
import de.uni_passau.fim.se2.litterbox.utils.Preconditions;

import java.util.ArrayList;
import java.util.List;

public class MergeDoubleIf extends CloneVisitor implements Refactoring {

    private final IfThenStmt if1;
    private final IfThenStmt if2;
    private final IfThenStmt replacement;
    private final String if1String;
    private final String if2String;
    private final String replacementString;
    private static final String NAME = "merge_double_if";

    public MergeDoubleIf(IfThenStmt if1, IfThenStmt if2) {
        this.if1 = Preconditions.checkNotNull(if1);
        this.if2 = Preconditions.checkNotNull(if2);

        CloneVisitor cloneVisitor = new CloneVisitor();
        List<Stmt> mergedListOfStmts = cloneVisitor.apply(if1.getThenStmts()).getStmts();
        mergedListOfStmts.addAll(cloneVisitor.apply(if2.getThenStmts()).getStmts());
        StmtList mergedThenStmts = new StmtList(mergedListOfStmts);
        replacement = new IfThenStmt(cloneVisitor.apply(if1.getBoolExpr()), mergedThenStmts, cloneVisitor.apply(if1.getMetadata()));

        ScratchBlocksVisitor visitor = new ScratchBlocksVisitor();
        if1.accept(visitor);
        if1String = visitor.getScratchBlocks();
        visitor = new ScratchBlocksVisitor();
        if2.accept(visitor);
        if2String = visitor.getScratchBlocks();
        visitor = new ScratchBlocksVisitor();
        replacement.accept(visitor);
        replacementString = visitor.getScratchBlocks();
    }

    @Override
    public Program apply(Program program) {
        return (Program) program.accept(this);
    }

    @Override
    public ASTNode visit(StmtList node) {
        List<Stmt> statements = new ArrayList<>();
        for (Stmt stmt : node.getStmts()) {
            if (stmt != if2) {
                if (stmt == if1) {
                    statements.add(replacement);
                } else {
                    statements.add(apply(stmt));
                }
            }
        }
        return new StmtList(statements);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return NAME + "\nReplaced ifs:\n\n" + if1String + "\n" + if2String + "\nReplacement:\n\n" + replacementString;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MergeDoubleIf)) {
            return false;
        }
        return if1.equals(((MergeDoubleIf) other).if1)
                && if2.equals(((MergeDoubleIf) other).if2);
    }

    @Override
    public int hashCode() {
        return if1.hashCode() + if2.hashCode();
    }
}
