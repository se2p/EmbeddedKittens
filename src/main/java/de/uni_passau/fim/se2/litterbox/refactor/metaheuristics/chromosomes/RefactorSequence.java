package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes;

import de.uni_passau.fim.se2.litterbox.analytics.RefactoringFinder;
import de.uni_passau.fim.se2.litterbox.ast.model.Program;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Crossover;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.search_operators.Mutation;
import de.uni_passau.fim.se2.litterbox.refactor.refactorings.Refactoring;

import java.util.*;
import java.util.function.Supplier;

public class RefactorSequence extends Solution<RefactorSequence> {

    private final List<Integer> productions;

    private final List<RefactoringFinder> refactoringFinders;

    private final Program originalProgram;

    private List<Refactoring> executedRefactorings;

    public List<Integer> getProductions() {
        return productions;
    }

    public List<Refactoring> getExecutedRefactorings() {
        return executedRefactorings;
    }

    public Program getOriginalProgram() {
        return originalProgram;
    }

    /**
     * Lazily build the refactored program, only if it was not previously already build.
     */
    private Supplier<Program> refactoredProgram = () -> {
        var program = buildRefactoredProgram();
        refactoredProgram = () -> program;
        return program;
    };

    public Program getRefactoredProgram() {
        return refactoredProgram.get();
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param originalProgram      the original program without any refactorings applied
     * @param mutation             a strategy that tells how to perform mutation, not {@code null}
     * @param crossover            a strategy that tells how to perform crossover, not {@code null}
     * @param productions          a list of executed refactorings within the sequence, not {@code null}
     * @param refactoringFinders   used refactoringFinders in the run, not {@code null}
     * @param fitnessMap           A map of fitness functions and their value stored inside the solution, not {@code null}
     * @param executedRefactorings A list of the concrete refactorings produced by the given list of productions, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Program originalProgram, Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<Integer> productions, List<RefactoringFinder> refactoringFinders,
                            Map<FitnessFunction<RefactorSequence>, Double> fitnessMap,
                            List<Refactoring> executedRefactorings) throws NullPointerException {
        super(mutation, crossover, fitnessMap);
        this.originalProgram = originalProgram;
        this.productions = Objects.requireNonNull(productions);
        this.refactoringFinders = Objects.requireNonNull(refactoringFinders);
        this.executedRefactorings = Objects.requireNonNull(executedRefactorings);
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param originalProgram    the original program without any refactorings applied
     * @param mutation           a strategy that tells how to perform mutation, not {@code null}
     * @param crossover          a strategy that tells how to perform crossover, not {@code null}
     * @param productions        a list of executed refactorings within the sequence, not {@code null}
     * @param refactoringFinders used refactoringFinders in the run, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Program originalProgram, Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<Integer> productions, List<RefactoringFinder> refactoringFinders) throws NullPointerException {
        super(mutation, crossover);
        this.originalProgram = originalProgram;
        this.productions = Objects.requireNonNull(productions);
        this.refactoringFinders = Objects.requireNonNull(refactoringFinders);
        this.executedRefactorings = new LinkedList<>();
    }

    /**
     * Constructs a new chromosome, using the given mutation and crossover operators for offspring
     * creation.
     *
     * @param originalProgram    the original program without any refactorings applied
     * @param mutation           a strategy that tells how to perform mutation, not {@code null}
     * @param crossover          a strategy that tells how to perform crossover, not {@code null}
     * @param refactoringFinders used refactoringFinders in the run, not {@code null}
     * @throws NullPointerException if an argument is {@code null}
     */
    public RefactorSequence(Program originalProgram, Mutation<RefactorSequence> mutation, Crossover<RefactorSequence> crossover,
                            List<RefactoringFinder> refactoringFinders) throws NullPointerException {
        super(mutation, crossover);
        this.originalProgram = originalProgram;
        this.productions = new LinkedList<>();
        this.refactoringFinders = refactoringFinders;
        this.executedRefactorings = new LinkedList<>();
    }

    /**
     * Apply the refactoring sequence to a given program, without modifying the original program.
     *
     * @return A deep copy of the original program after the refactorings were applied.
     */
    public Program buildRefactoredProgram() {
        executedRefactorings = new LinkedList<>();
        var current = originalProgram.deepCopy();

        for (Integer nthProduction : productions) {

            var executedRefactoring = getExecutedRefactoring(current, nthProduction);
            if (executedRefactoring == null) {
                break;
            }
            executedRefactorings.add(executedRefactoring);
            current = executedRefactoring.apply(current);
        }
        return current.deepCopy();
    }

    private Refactoring getExecutedRefactoring(Program program, Integer nthProduction) {
        List<Refactoring> possibleProductions = new LinkedList<>();
        for (RefactoringFinder refactoringFinder : refactoringFinders) {
            possibleProductions.addAll(refactoringFinder.check(program));
        }
        if (possibleProductions.isEmpty()) {
            return null;
        }

        int executedProduction = nthProduction % possibleProductions.size();
        return possibleProductions.get(executedProduction);
    }

    @Override
    public RefactorSequence copy() {
        return new RefactorSequence(originalProgram, getMutation(), getCrossover(),
                new ArrayList<>(productions), refactoringFinders);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof RefactorSequence)) {
            return false;
        }
        if (executedRefactorings.isEmpty()) {
            if (((RefactorSequence) other).getProductions().equals(getProductions())) {
                return true;
            }
            // calculate the executed refactorings for both objects for comparison
            ((RefactorSequence) other).buildRefactoredProgram();
            buildRefactoredProgram();
        }
        return ((RefactorSequence) other).getExecutedRefactorings().equals(getExecutedRefactorings());
    }

    @Override
    public int hashCode() {
        if (executedRefactorings.isEmpty()) {
            // internally calculates refactored program and sets executedRefactoring list
            getRefactoredProgram();
        }
        return getExecutedRefactorings().hashCode();
    }

    @Override
    public RefactorSequence self() {
        return this;
    }
}
