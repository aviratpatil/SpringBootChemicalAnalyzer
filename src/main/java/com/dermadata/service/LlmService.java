package com.dermadata.service;

import com.dermadata.dto.IngredientInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Mock LLM Service — returns hardcoded insights until real API keys are configured.
 */
@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    public List<String> getLlmCombinationAnalysis(List<IngredientInput> ingredients, String productType) {
        log.info("Mock LLM analysis for {} ingredients (product type: {})", ingredients.size(), productType);

        List<String> insights = new ArrayList<>();
        Set<String> names = new HashSet<>();
        for (IngredientInput i : ingredients) {
            names.add(i.getInciName().toUpperCase().trim());
        }

        if (names.contains("SODIUM LAURYL SULFATE") && names.contains("COCAMIDOPROPYL BETAINE")) {
            insights.add("SLS + Cocamidopropyl Betaine: Common combination in shampoos. Betaine helps mitigate SLS irritation. Generally safe at standard concentrations.");
        }

        if (names.contains("METHYLPARABEN") && names.contains("PROPYLPARABEN")) {
            insights.add("Methylparaben + Propylparaben: Total paraben concentration should not exceed 0.8%. Combined estrogenic activity is a concern under EU Regulation Annex V/12.");
        }

        if (names.contains("SODIUM HYDROXIDE") && names.contains("CITRIC ACID")) {
            insights.add("Sodium Hydroxide + Citric Acid: pH neutralization pair. Ensure final product pH is between 3.5-9.0 for leave-on products.");
        }

        if (names.contains("PHENOXYETHANOL") && names.contains("ETHYLHEXYLGLYCERIN")) {
            insights.add("Phenoxyethanol + Ethylhexylglycerin: Synergistic preservative system. Ethylhexylglycerin boosts antimicrobial activity, allowing lower Phenoxyethanol levels.");
        }

        if (names.contains("RETINOL") || names.contains("RETINYL PALMITATE")) {
            insights.add("Retinoid detected: Should not be combined with AHA/BHA acids in the same product. Maximum 0.3% Retinol equivalent in leave-on body products per EU guidelines.");
        }

        if (names.contains("SALICYLIC ACID") && names.contains("GLYCOLIC ACID")) {
            insights.add("Salicylic Acid + Glycolic Acid: Dual acid combination increases skin sensitization risk. Total acid concentration should be carefully managed. pH must be ≥ 3.5.");
        }

        if (names.contains("FORMALDEHYDE")) {
            insights.add("⚠ Formaldehyde is classified as a CMR substance (Carcinogenic Category 1B). Prohibited in EU cosmetics under Regulation (EC) No 1223/2009, Annex II/1577.");
        }

        if (insights.isEmpty()) {
            insights.add("No significant interaction concerns detected by AI analysis for this combination.");
        }

        return insights;
    }
}
