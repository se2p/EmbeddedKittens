package de.uni_passau.fim.se2.litterbox.analytics.ml_preprocessing.code2vec;

import de.uni_passau.fim.se2.litterbox.ast.model.ASTNode;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProgramPathGenerator extends PathGenerator{

    public ProgramPathGenerator(int maxPathLength, boolean includeStage,  Program program) {
        super(maxPathLength, includeStage,  program);
    }

    @Override
    public void printLeafs() {
        //
    }

    @Override
    public List<ProgramFeatures> generatePaths() {
        return generatePathsWholeProgram().stream().collect(Collectors.toList());
    }

    private Optional<ProgramFeatures> generatePathsWholeProgram() {
        final List<ASTNode> leafs = leafsMap.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        final ProgramFeatures programFeatures = getProgramFeatures("program", leafs);
        return Optional.of(programFeatures).filter(features -> !features.isEmpty());
    }


}
