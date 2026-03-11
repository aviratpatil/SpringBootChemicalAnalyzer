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

    private static final int BASELINE_SCORE = 100;
    private static final int PROHIBITED_PENALTY = 20;
    private static final int EXCEEDED_CONCENTRATION_PENALTY = 10;
    private static final int COMBINATION_VIOLATION_PENALTY = 5;

    public SafetyScoreEngineService(IngredientRegulationRepository regulationRepo,
                                     CombinationRuleRepository combinationRepo) {
        this.regulationRepo = regulationRepo;
        this.combinationRepo = combinationRepo;
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

        // ── Step 1: Check each ingredient against regulations ──
        for (IngredientInput input : ingredients) {
            Optional<IngredientRegulation> regOpt =
                    regulationRepo.findByInciNameIgnoreCase(input.getInciName().trim());

            if (regOpt.isPresent()) {
                IngredientRegulation reg = regOpt.get();
                IngredientResult result = evaluateIngredient(input, reg, productType);
                results.add(result);

                score -= result.getPenaltyPoints();
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
                IngredientResult nr = new IngredientResult();
                nr.setInciName(input.getInciName());
                nr.setDetectedConcentration(input.getConcentration());
                nr.setEuMaxConcentration(null);
                nr.setProhibited(false);
                nr.setRestricted(false);
                nr.setStatus("NOT_REGULATED");
                nr.setRegulationRef(null);
                nr.setConditions(null);
                nr.setPenaltyPoints(0);
                results.add(nr);
            }
        }

        // ── Step 2: Check combination rules ──
        List<String> inciNames = ingredients.stream()
                .map(i -> i.getInciName().trim().toLowerCase())
                .collect(Collectors.toList());

        List<CombinationRule> matchedRules = combinationRepo.findRulesForIngredients(inciNames);

        for (CombinationRule rule : matchedRules) {
            Optional<IngredientInput> inputA = ingredients.stream()
                    .filter(i -> i.getInciName().equalsIgnoreCase(rule.getIngredientA()))
                    .findFirst();
            Optional<IngredientInput> inputB = ingredients.stream()
                    .filter(i -> i.getInciName().equalsIgnoreCase(rule.getIngredientB()))
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
                    w.setPenaltyPoints(COMBINATION_VIOLATION_PENALTY);
                    warnings.add(w);
                    score -= COMBINATION_VIOLATION_PENALTY;
                }
            }
        }

        // ── Clamp score to [0, 100] ──
        score = Math.max(0, Math.min(100, score));

        String category;
        if (score >= 71) category = "SAFE";
        else if (score >= 41) category = "CAUTION";
        else category = "DANGER";

        AnalysisReport report = new AnalysisReport();
        report.setSafetyScore(score);
        report.setProductType(productType);
        report.setScoreCategory(category);
        report.setIngredientResults(results);
        report.setCombinationWarnings(warnings);
        report.setLlmInsights(new ArrayList<>());
        report.setAnalyzedAt(LocalDateTime.now());
        report.setTotalIngredients(ingredients.size());
        report.setFlaggedIngredients(flaggedCount);
        report.setProhibitedCount(prohibitedCount);
        report.setExceededCount(exceededCount);
        report.setCombinationViolations(warnings.size());
        return report;
    }

    private IngredientResult evaluateIngredient(IngredientInput input,
                                                 IngredientRegulation reg,
                                                 String productType) {
        int penalty = 0;
        String status;

        if (Boolean.TRUE.equals(reg.getProhibited())) {
            status = "PROHIBITED";
            penalty = PROHIBITED_PENALTY;
        } else if (reg.getMaxConcentration() != null
                && input.getConcentration() != null
                && input.getConcentration() > reg.getMaxConcentration()) {
            status = "EXCEEDED";
            penalty = EXCEEDED_CONCENTRATION_PENALTY;
        } else if (Boolean.TRUE.equals(reg.getRestricted())) {
            status = "RESTRICTED";
            penalty = 0;
        } else {
            status = "SAFE";
            penalty = 0;
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
        r.setPenaltyPoints(penalty);
        return r;
    }

    private boolean checkCombinationViolation(CombinationRule rule,
                                               IngredientInput inputA,
                                               IngredientInput inputB) {
        if (rule.getSafeConcentrationA() != null
                && inputA.getConcentration() != null
                && inputA.getConcentration() > rule.getSafeConcentrationA()) {
            return true;
        }

        if (rule.getSafeConcentrationA() == null && rule.getRequiredConcentrationB() == null) {
            return true;
        }

        return false;
    }
}
