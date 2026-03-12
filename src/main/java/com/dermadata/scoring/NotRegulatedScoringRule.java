package com.dermadata.scoring;

import com.dermadata.dto.IngredientResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotRegulatedScoringRule implements ScoringRule {

    @Value("${scoring.penalty.not-regulated:3}")
    private int penaltyPoints;

    @Override
    public int calculatePenalty(IngredientResult result) {
        if ("NOT_REGULATED".equalsIgnoreCase(result.getStatus())) {
            return penaltyPoints;
        }
        return 0;
    }
}
