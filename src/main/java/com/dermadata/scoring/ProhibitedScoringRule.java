package com.dermadata.scoring;

import com.dermadata.dto.IngredientResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProhibitedScoringRule implements ScoringRule {

    @Value("${scoring.penalty.prohibited:20}")
    private int penaltyPoints;

    @Override
    public int calculatePenalty(IngredientResult result) {
        if ("PROHIBITED".equalsIgnoreCase(result.getStatus())) {
            return penaltyPoints;
        }
        return 0;
    }
}
