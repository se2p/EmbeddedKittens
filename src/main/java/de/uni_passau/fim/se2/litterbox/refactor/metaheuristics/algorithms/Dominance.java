package de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.algorithms;

import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.chromosomes.Solution;
import de.uni_passau.fim.se2.litterbox.refactor.metaheuristics.fitness_functions.FitnessFunction;

import java.util.Collection;
import java.util.List;
import java.util.function.BiPredicate;

public class Dominance<C extends Solution<C>> implements BiPredicate<C, C> {

    private final List<FitnessFunction<C>> fitnessFunctions;

    public Dominance(Collection<FitnessFunction<C>> fitnessFunctions) {
        this.fitnessFunctions = List.copyOf(fitnessFunctions);
    }

    /**
     * solution1 is said to dominate the other solution2 if both condition 1 and 2 below are true:
     * Condition 1: solution1 is no worse than solution2 for all objectives
     * Condition 2: solution1 is strictly better than solution2 in at least one objective
     * <p>
     *
     * @param solution1 Chromosome 1, which is checked, whether it dominates the other chromosome
     * @param solution2 Chromosome 2, which is checked, whether it is dominates by the other chromosome
     * @return {@code true} if solution1 dominates solution2, {@code false} otherwise
     */
    @Override
    public boolean test(C solution1, C solution2) {
        var dominatesAtLeastOne = false;

        for (FitnessFunction<C> fitnessFunction : fitnessFunctions) {
            if (solution2.getFitness(fitnessFunction) > solution1.getFitness(fitnessFunction)) {
                if (fitnessFunction.isMinimizing()) {
                    dominatesAtLeastOne = true;
                } else {
                    return false;
                }
            } else if (solution1.getFitness(fitnessFunction) > solution2.getFitness(fitnessFunction)) {
                if (fitnessFunction.isMinimizing()) {
                    return false;
                } else {
                    dominatesAtLeastOne = true;
                }
            }
        }

        return dominatesAtLeastOne;
    }
}
