package com.dermadata.scoring;

import com.dermadata.dto.IngredientResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ExceededConcentrationRule implements ScoringRule {

    @Value("${scoring.penalty.exceeded:10}")
    private int penaltyPoints;

    @Override
    public int calculatePenalty(IngredientResult result) {
        if ("EXCEEDED".equalsIgnoreCase(result.getStatus())) {
            return penaltyPoints;
        }
        return 0;
    }
}
