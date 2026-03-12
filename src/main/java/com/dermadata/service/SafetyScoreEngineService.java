package com.dermadata.service;

import com.dermadata.dto.*;
import com.dermadata.entity.CombinationRule;
import com.dermadata.entity.IngredientRegulation;
import com.dermadata.repository.CombinationRuleRepository;
import com.dermadata.repository.IngredientRegulationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SafetyScoreEngineService {

    private static final Logger log = LoggerFactory.getLogger(SafetyScoreEngineService.class);

    private final IngredientRegulationRepository regulationRepo;
    private final CombinationRuleRepository combinationRepo;

    private final com.dermadata.scoring.ScoringRuleRegistry scoringRegistry;

    private static final int BASELINE_SCORE = 100;
    private static final int PROHIBITED_PENALTY = 20;
    private static final int EXCEEDED_CONCENTRATION_PENALTY = 10;
    private static final int COMBINATION_VIOLATION_PENALTY = 5;

    public SafetyScoreEngineService(IngredientRegulationRepository regulationRepo,
                                     CombinationRuleRepository combinationRepo,
                                     com.dermadata.scoring.ScoringRuleRegistry scoringRegistry) {
        this.regulationRepo = regulationRepo;
        this.combinationRepo = combinationRepo;
        this.scoringRegistry = scoringRegistry;
    }

    /**
     * Analyze a list of ingredients against EU regulations and return a full report.
     */
    public AnalysisReport analyze(List<IngredientInput> ingredients, String productType) {
        List<IngredientResult> results = new ArrayList<>();
        List<CombinationWarning> warnings = new ArrayList<>();

        int score = BASELINE_SCORE;
        int prohibitedCount = 0;
        int exceededCount = 0;
        int flaggedCount = 0;
        int notRegulatedCount = 0;

        // ── Step 1: Check each ingredient against regulations ──
        for (IngredientInput input : ingredients) {
            Optional<IngredientRegulation> regOpt =
                    regulationRepo.findByInciNameIgnoreCase(input.getInciName().trim());

            if (regOpt.isPresent()) {
                IngredientRegulation reg = regOpt.get();
                IngredientResult result = evaluateIngredient(input, reg, productType);
                
                // Apply scoring rules (Strategy Pattern)
                int penalty = applyScoringRules(result);
                result.setPenaltyPoints(penalty);
                result.setAliases(input.getAliases());
                score -= penalty;
                
                results.add(result);

                if ("PROHIBITED".equals(result.getStatus())) {
                    prohibitedCount++;
                    flaggedCount++;
                } else if ("EXCEEDED".equals(result.getStatus())) {
                    exceededCount++;
                    flaggedCount++;
                } else if ("RESTRICTED".equals(result.getStatus())) {
                    flaggedCount++;
                }
            } else {
                // Ingredient not found in regulation DB → mark as not regulated
                notRegulatedCount++;
                flaggedCount++; // since unknown is not strictly safe
                
                IngredientResult nr = new IngredientResult();
                nr.setInciName(input.getInciName());
                nr.setDetectedConcentration(input.getConcentration());
                nr.setEuMaxConcentration(null);
                nr.setProhibited(false);
                nr.setRestricted(false);
                nr.setStatus("NOT_REGULATED");
                nr.setRegulationRef(null);
                nr.setConditions(null);
                
                // Apply scoring rules (Strategy Pattern)
                int penalty = applyScoringRules(nr);
                nr.setPenaltyPoints(penalty);
                nr.setAliases(input.getAliases());
                score -= penalty;
                
                results.add(nr);
            }
        }

        // ── Step 2: Check combination rules ──
        Set<String> normalizedNames = ingredients.stream()
                .map(i -> i.getInciName().trim().toLowerCase())
                .collect(Collectors.toSet());

        List<CombinationRule> matchedRules = combinationRepo.findRulesForIngredients(new ArrayList<>(normalizedNames));

        for (CombinationRule rule : matchedRules) {
            String ruleA = rule.getIngredientA().trim().toLowerCase();
            String ruleB = rule.getIngredientB().trim().toLowerCase();

            if (normalizedNames.contains(ruleA) && normalizedNames.contains(ruleB)) {
                
                Optional<IngredientInput> inputA = ingredients.stream()
                        .filter(i -> i.getInciName().trim().toLowerCase().equals(ruleA))
                        .findFirst();
                Optional<IngredientInput> inputB = ingredients.stream()
                        .filter(i -> i.getInciName().trim().toLowerCase().equals(ruleB))
                        .findFirst();

                if (inputA.isPresent() && inputB.isPresent()) {
                    boolean violated = checkCombinationViolation(rule, inputA.get(), inputB.get());
                    if (violated) {
                        CombinationWarning w = new CombinationWarning();
                        w.setIngredientA(rule.getIngredientA());
                        w.setIngredientB(rule.getIngredientB());
                        w.setCondition(rule.getCondition());
                        w.setExplanation(rule.getExplanation());
                        w.setSource(rule.getSource());
                        
                        // We can't access scoringRegistry.getCombinationPenalty() easily if it's not a getter,
                        int combinationPenalty = scoringRegistry.getCombinationPenalty();
                        w.setPenaltyPoints(combinationPenalty);
                        warnings.add(w);
                        score -= combinationPenalty;
                    }
                }
            }
        }

        // ── Clamp score to [0, 100] ──
        score = Math.max(0, Math.min(100, score));

        boolean hardCapApplied = false;
        String overrideReason = null;

        if (prohibitedCount >= 1) {
            if (score > 40) {
                score = 40; // Hard cap
                hardCapApplied = true;
                overrideReason = "Score overridden: " + prohibitedCount + " EU-prohibited substance(s) detected";
            }
        }

        String category;
        if (score >= 71) category = "SAFE";
        else if (score >= 41) category = "CAUTION";
        else category = "DANGER";

        
        List<String> insights = new ArrayList<>();
        if (notRegulatedCount > 0) {
            insights.add(notRegulatedCount + " ingredient(s) not found in EU CosIng database — safety unknown.");
        }

        AnalysisReport report = new AnalysisReport();
        report.setSafetyScore(score);
        report.setProductType(productType);
        report.setScoreCategory(category);
        report.setIngredientResults(results);
        report.setCombinationWarnings(warnings);
        report.setLlmInsights(insights);
        report.setAnalyzedAt(LocalDateTime.now());
        report.setTotalIngredients(ingredients.size());
        report.setFlaggedIngredients(flaggedCount);
        report.setProhibitedCount(prohibitedCount);
        report.setExceededCount(exceededCount);
        report.setCombinationViolations(warnings.size());
        report.setNotRegulatedCount(notRegulatedCount);
        report.setHardCapApplied(hardCapApplied);
        report.setOverrideReason(overrideReason);
        return report;
    }
    
    private int applyScoringRules(IngredientResult result) {
        int totalPenalty = 0;
        for (com.dermadata.scoring.ScoringRule rule : scoringRegistry.getRules()) {
            totalPenalty += rule.calculatePenalty(result);
        }
        return totalPenalty;
    }

    private IngredientResult evaluateIngredient(IngredientInput input,
                                                 IngredientRegulation reg,
                                                 String productType) {
        String status;

        if (Boolean.TRUE.equals(reg.getProhibited())) {
            status = "PROHIBITED";
        } else if (reg.getMaxConcentration() != null
                && input.getConcentration() != null
                && input.getConcentration() > reg.getMaxConcentration()) {
            status = "EXCEEDED";
        } else if (Boolean.TRUE.equals(reg.getRestricted())) {
            status = "RESTRICTED";
        } else {
            status = "SAFE";
        }

        IngredientResult r = new IngredientResult();
        r.setInciName(reg.getInciName());
        r.setDetectedConcentration(input.getConcentration());
        r.setEuMaxConcentration(reg.getMaxConcentration());
        r.setProhibited(reg.getProhibited());
        r.setRestricted(reg.getRestricted());
        r.setStatus(status);
        r.setRegulationRef(reg.getRegulationRef());
        r.setConditions(reg.getConditions());
        // Penalty is set later by the strategy rules
        r.setPenaltyPoints(0);
        return r;
    }

    private boolean checkCombinationViolation(CombinationRule rule,
                                               IngredientInput inputA,
                                               IngredientInput inputB) {
        // Case 1: Concentration of ingredient A is known and exceeds the safe limit
        if (rule.getSafeConcentrationA() != null
                && inputA.getConcentration() != null
                && inputA.getConcentration() > rule.getSafeConcentrationA()) {
            return true;
        }

        // Case 2: Concentration of ingredient B is known and exceeds the required limit
        if (rule.getRequiredConcentrationB() != null
                && inputB.getConcentration() != null
                && inputB.getConcentration() > rule.getRequiredConcentrationB()) {
            return true;
        }

        // Case 3: No concentration thresholds defined — co-presence alone is the concern
        // (e.g., double allergens, reactive pairs, or banned ingredient combos).
        // Only flag if the rule explicitly has no concentration guards (both null),
        // meaning the rule applies purely based on both ingredients being present.
        if (rule.getSafeConcentrationA() == null && rule.getRequiredConcentrationB() == null) {
            return true;
        }

        // Case 4: Rule has a safe limit but ingredient concentration is unknown (null) — skip
        return false;
    }
}
