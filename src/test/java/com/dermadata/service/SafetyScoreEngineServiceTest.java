package com.dermadata.service;

import com.dermadata.dto.AnalysisReport;
import com.dermadata.dto.IngredientInput;
import com.dermadata.entity.CombinationRule;
import com.dermadata.entity.IngredientRegulation;
import com.dermadata.repository.CombinationRuleRepository;
import com.dermadata.repository.IngredientRegulationRepository;
import com.dermadata.scoring.CombinationViolationRule;
import com.dermadata.scoring.ExceededConcentrationRule;
import com.dermadata.scoring.ProhibitedScoringRule;
import com.dermadata.scoring.ScoringRuleRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SafetyScoreEngineServiceTest {

    @Mock
    private IngredientRegulationRepository regulationRepo;

    @Mock
    private CombinationRuleRepository combinationRepo;

    private SafetyScoreEngineService engine;

    @BeforeEach
    void setUp() {
        // Setup registry with default rules
        ProhibitedScoringRule pRule = new ProhibitedScoringRule();
        ReflectionTestUtils.setField(pRule, "penaltyPoints", 20);
        
        ExceededConcentrationRule eRule = new ExceededConcentrationRule();
        ReflectionTestUtils.setField(eRule, "penaltyPoints", 10);
        
        CombinationViolationRule cRule = new CombinationViolationRule();
        ReflectionTestUtils.setField(cRule, "penaltyPoints", 5);

        ScoringRuleRegistry registry = new ScoringRuleRegistry(List.of(pRule, eRule), cRule);
        engine = new SafetyScoreEngineService(regulationRepo, combinationRepo, registry);
    }

    @Test
    void whenFormaldehydeIsPresent_thenHardCapOf40IsApplied() {
        // Arrange
        IngredientInput badInput = new IngredientInput();
        badInput.setInciName("FORMALDEHYDE");
        badInput.setConcentration(0.5);

        IngredientRegulation bannedReg = new IngredientRegulation();
        bannedReg.setInciName("FORMALDEHYDE");
        bannedReg.setProhibited(true);

        when(regulationRepo.findByInciNameIgnoreCase("FORMALDEHYDE"))
                .thenReturn(Optional.of(bannedReg));
        
        when(combinationRepo.findRulesForIngredients(Collections.singletonList("formaldehyde")))
                .thenReturn(Collections.emptyList());

        // Act
        AnalysisReport report = engine.analyze(Collections.singletonList(badInput), "Shampoo");

        // Assert
        assertTrue(report.getSafetyScore() <= 40, "Score should be hard capped at 40 or below. Got: " + report.getSafetyScore());
        assertEquals("DANGER", report.getScoreCategory());
        assertTrue(report.getHardCapApplied(), "Hard cap flag should be true");
        assertNotNull(report.getOverrideReason(), "Override reason should be provided");
        assertTrue(report.getOverrideReason().contains("EU-prohibited"), "Reason should mention prohibited substance");
    }
    @Test
    void whenCombinationIsPresentInAnyOrder_thenPenaltyIsApplied() {
        // Arrange
        IngredientInput sls = new IngredientInput();
        sls.setInciName("SLS");
        sls.setConcentration(2.0);

        IngredientInput mit = new IngredientInput();
        mit.setInciName("MIT");
        mit.setConcentration(0.01);

        IngredientRegulation slsReg = new IngredientRegulation();
        slsReg.setInciName("SLS");
        when(regulationRepo.findByInciNameIgnoreCase("SLS")).thenReturn(Optional.of(slsReg));

        IngredientRegulation mitReg = new IngredientRegulation();
        mitReg.setInciName("MIT");
        when(regulationRepo.findByInciNameIgnoreCase("MIT")).thenReturn(Optional.of(mitReg));

        CombinationRule rule = new CombinationRule();
        rule.setIngredientA("SLS");
        rule.setIngredientB("MIT");
        // No concentrations specified implies co-presence violation
        
        when(combinationRepo.findRulesForIngredients(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Collections.singletonList(rule));

        // Act 1: SLS then MIT
        AnalysisReport report1 = engine.analyze(List.of(sls, mit), "Face Wash");
        
        // Act 2: MIT then SLS
        AnalysisReport report2 = engine.analyze(List.of(mit, sls), "Face Wash");

        // Assert
        assertEquals(1, report1.getCombinationViolations(), "Should detect 1 combination violation regardless of order");
        assertEquals(95, report1.getSafetyScore(), "Score should be 100 - 5 = 95");
        
        assertEquals(1, report2.getCombinationViolations(), "Should detect 1 combination violation in reverse order");
        assertEquals(95, report2.getSafetyScore(), "Score should be 100 - 5 = 95 in reverse order");
    }
}
