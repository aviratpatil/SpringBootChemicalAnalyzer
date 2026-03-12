package com.dermadata.scoring;

import com.dermadata.dto.IngredientResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CombinationViolationRule implements ScoringRule {

    @Value("${scoring.penalty.combination:5}")
    private int penaltyPoints;

    @Override
    public int calculatePenalty(IngredientResult result) {
        // Combination violations refer to pairs, not single ingredients.
        // This method isn't used for combinations, so return 0.
        return 0;
    }

    @Override
    public int calculateCombinationPenalty() {
        return penaltyPoints;
    }
}
