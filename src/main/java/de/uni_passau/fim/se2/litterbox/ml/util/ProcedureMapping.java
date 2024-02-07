package de.uni_passau.fim.se2.litterbox.ml.util;

import de.uni_passau.fim.se2.litterbox.ast.model.ActorDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.ast.model.procedure.ProcedureDefinition;
import de.uni_passau.fim.se2.litterbox.ast.model.statement.CallStmt;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureDefinitionNameMapping;
import de.uni_passau.fim.se2.litterbox.ast.parser.symboltable.ProcedureInfo;
import de.uni_passau.fim.se2.litterbox.ast.util.AstNodeUtil;
import de.uni_passau.fim.se2.litterbox.ast.visitor.ScratchVisitor;

import java.util.Optional;

public final class ProcedureMapping {
    public ProcedureMapping(final Program program) {
        throw new IllegalCallerException("utility class");
    }



    public static Optional<ProcedureDefinition> findCalledProcedure(
        final ProcedureDefinitionNameMapping procedureMapping, final CallStmt callStmt
    ) {
        String procedureName = callStmt.getIdent().getName();
        String sprite = AstNodeUtil.findActor(callStmt).orElseThrow().getIdent().getName();

        return procedureMapping.getProceduresForName(sprite, procedureName)
            .stream()
            .filter(procedure -> hasMatchingParameterCount(callStmt, procedure.getRight()))
            .map(org.apache.commons.lang3.tuple.Pair::getKey)
            .map(procedures::get)
            .findFirst();
    }

    private static boolean hasMatchingParameterCount(final CallStmt callStmt, final ProcedureInfo procedure) {
        int passedArgumentCount = callStmt.getExpressions().getExpressions().size();
        int acceptingArgumentCount = procedure.getArguments().length;
        return passedArgumentCount == acceptingArgumentCount;
    }

    private class ProcedureDefinitionVisitor implements ScratchVisitor {
    }
}
