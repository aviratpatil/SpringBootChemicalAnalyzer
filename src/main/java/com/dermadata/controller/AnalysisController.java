package com.dermadata.controller;

import com.dermadata.dto.*;
import com.dermadata.service.ExtractionService;
import com.dermadata.service.LlmService;
import com.dermadata.service.SafetyScoreEngineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final SafetyScoreEngineService scoreEngine;
    private final ExtractionService extractionService;
    private final LlmService llmService;

    public AnalysisController(SafetyScoreEngineService scoreEngine,
                              ExtractionService extractionService,
                              LlmService llmService) {
        this.scoreEngine = scoreEngine;
        this.extractionService = extractionService;
        this.llmService = llmService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisReport> analyzeIngredients(@RequestBody AnalysisRequest request) {
        log.info("Received analysis request: productType={}, ingredients={}",
                request.getProductType(),
                request.getIngredients() != null ? request.getIngredients().size() : 0);

        List<IngredientInput> ingredients = request.getIngredients();

        if (ingredients == null || ingredients.isEmpty()) {
            if (request.getRawText() != null && !request.getRawText().isBlank()) {
                ingredients = extractionService.extractFromText(request.getRawText());
            } else if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
                ingredients = extractionService.extractFromImage(request.getImageBase64());
            } else {
                return ResponseEntity.badRequest().build();
            }
        }

        AnalysisReport report = scoreEngine.analyze(ingredients, request.getProductType());
        List<String> llmInsights = llmService.getLlmCombinationAnalysis(ingredients, request.getProductType());
        report.setLlmInsights(llmInsights);

        log.info("Analysis complete: score={}, category={}", report.getSafetyScore(), report.getScoreCategory());
        return ResponseEntity.ok(report);
    }

    @PostMapping("/extract")
    public ResponseEntity<List<IngredientInput>> extractIngredients(@RequestBody AnalysisRequest request) {
        List<IngredientInput> extracted;

        if (request.getRawText() != null && !request.getRawText().isBlank()) {
            extracted = extractionService.extractFromText(request.getRawText());
        } else if (request.getImageBase64() != null && !request.getImageBase64().isBlank()) {
            extracted = extractionService.extractFromImage(request.getImageBase64());
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(extracted);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("DermaData API is running ✓");
    }
}
