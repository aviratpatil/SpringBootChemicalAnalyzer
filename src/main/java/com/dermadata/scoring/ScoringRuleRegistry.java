package com.dermadata.scoring;

import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class ScoringRuleRegistry {

    private final List<ScoringRule> rules;
    private final CombinationViolationRule combinationRule;

    public ScoringRuleRegistry(List<ScoringRule> rules, CombinationViolationRule combinationRule) {
        this.rules = rules;
        this.combinationRule = combinationRule;
    }

    public List<ScoringRule> getRules() {
        return rules;
    }

    public int getCombinationPenalty() {
        return combinationRule.calculateCombinationPenalty();
    }
}
