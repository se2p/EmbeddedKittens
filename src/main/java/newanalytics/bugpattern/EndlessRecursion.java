package newanalytics.bugpattern;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.ast.model.ASTNode;
import scratch.ast.model.ActorDefinition;
import scratch.ast.model.Program;
import scratch.ast.model.procedure.ProcedureDefinition;
import scratch.ast.model.statement.CallStmt;
import scratch.ast.model.variable.Identifier;
import scratch.ast.parser.symboltable.ProcedureInfo;
import scratch.ast.visitor.ScratchVisitor;
import utils.Preconditions;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EndlessRecursion implements IssueFinder, ScratchVisitor {
    private static final String NOTE1 = "There are no endless recursions in your project.";
    private static final String NOTE2 = "Some of the sprites can contain endless recursions.";
    public static final String NAME = "endless_recursion";
    public static final String SHORT_NAME = "endlssrcrsn";
    private boolean found = false;
    private int count = 0;
    private List<String> actorNames = new LinkedList<>();
    private ActorDefinition currentActor;
    private Map<Identifier, ProcedureInfo> procMap;
    private String currentProcedureName;
    private boolean insideProcedure;

    @Override
    public IssueReport check(Program program) {
        Preconditions.checkNotNull(program);
        procMap = program.getProcedureMapping().getProcedures();
        program.accept(this);
        String notes = NOTE1;
        if (count > 0) {
            notes = NOTE2;
        }
        return new IssueReport(NAME, count, actorNames, notes);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void visit(ActorDefinition actor) {
        currentActor = actor;
        if (!actor.getChildren().isEmpty()) {
            for (ASTNode child : actor.getChildren()) {
                child.accept(this);
            }
        }

        if (found) {
            found = false;
            actorNames.add(currentActor.getIdent().getName());
        }
    }

    @Override
    public void visit(ProcedureDefinition node) {
        insideProcedure = true;
        currentProcedureName = procMap.get(node.getIdent()).getName();
        if (!node.getChildren().isEmpty()) {
            for (ASTNode child : node.getChildren()) {
                child.accept(this);
            }
        }
        insideProcedure = false;
    }

    @Override
    public void visit(CallStmt node) {
        if (insideProcedure) {
            String call = node.getIdent().getName();
            if(call.equals(currentProcedureName)){
                found=true;
                count++;
            }
        }
    }
}
