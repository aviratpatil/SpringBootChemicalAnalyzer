package com.dermadata.scoring;

import com.dermadata.dto.IngredientResult;

public interface ScoringRule {
    /**
     * Calculates the penalty points for a given ingredient result.
     * @param result the evaluation result of an ingredient
     * @return penalty points (0 if the rule does not apply)
     */
    int calculatePenalty(IngredientResult result);
    
    /**
     * Optional method to determine if the rule applies to a combination warning.
     * Overloaded to handle combination logic.
     */
    default int calculateCombinationPenalty() {
        return 0; // Default implementation for single-ingredient rules
    }
}
