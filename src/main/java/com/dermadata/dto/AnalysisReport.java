package com.dermadata.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AnalysisReport {
    private Integer safetyScore;
    private String productType;
    private String scoreCategory;
    private List<IngredientResult> ingredientResults;
    private List<CombinationWarning> combinationWarnings;
    private List<String> llmInsights;
    private LocalDateTime analyzedAt;
    private Integer totalIngredients;
    private Integer flaggedIngredients;
    private Integer prohibitedCount;
    private Integer exceededCount;
    private Integer combinationViolations;

    public AnalysisReport() {}

    public AnalysisReport(Integer safetyScore, String productType, String scoreCategory,
                          List<IngredientResult> ingredientResults, List<CombinationWarning> combinationWarnings,
                          List<String> llmInsights, LocalDateTime analyzedAt, Integer totalIngredients,
                          Integer flaggedIngredients, Integer prohibitedCount, Integer exceededCount,
                          Integer combinationViolations) {
        this.safetyScore = safetyScore;
        this.productType = productType;
        this.scoreCategory = scoreCategory;
        this.ingredientResults = ingredientResults;
        this.combinationWarnings = combinationWarnings;
        this.llmInsights = llmInsights;
        this.analyzedAt = analyzedAt;
        this.totalIngredients = totalIngredients;
        this.flaggedIngredients = flaggedIngredients;
        this.prohibitedCount = prohibitedCount;
        this.exceededCount = exceededCount;
        this.combinationViolations = combinationViolations;
    }

    public Integer getSafetyScore() { return safetyScore; }
    public void setSafetyScore(Integer safetyScore) { this.safetyScore = safetyScore; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getScoreCategory() { return scoreCategory; }
    public void setScoreCategory(String scoreCategory) { this.scoreCategory = scoreCategory; }
    public List<IngredientResult> getIngredientResults() { return ingredientResults; }
    public void setIngredientResults(List<IngredientResult> ingredientResults) { this.ingredientResults = ingredientResults; }
    public List<CombinationWarning> getCombinationWarnings() { return combinationWarnings; }
    public void setCombinationWarnings(List<CombinationWarning> combinationWarnings) { this.combinationWarnings = combinationWarnings; }
    public List<String> getLlmInsights() { return llmInsights; }
    public void setLlmInsights(List<String> llmInsights) { this.llmInsights = llmInsights; }
    public LocalDateTime getAnalyzedAt() { return analyzedAt; }
    public void setAnalyzedAt(LocalDateTime analyzedAt) { this.analyzedAt = analyzedAt; }
    public Integer getTotalIngredients() { return totalIngredients; }
    public void setTotalIngredients(Integer totalIngredients) { this.totalIngredients = totalIngredients; }
    public Integer getFlaggedIngredients() { return flaggedIngredients; }
    public void setFlaggedIngredients(Integer flaggedIngredients) { this.flaggedIngredients = flaggedIngredients; }
    public Integer getProhibitedCount() { return prohibitedCount; }
    public void setProhibitedCount(Integer prohibitedCount) { this.prohibitedCount = prohibitedCount; }
    public Integer getExceededCount() { return exceededCount; }
    public void setExceededCount(Integer exceededCount) { this.exceededCount = exceededCount; }
    public Integer getCombinationViolations() { return combinationViolations; }
    public void setCombinationViolations(Integer combinationViolations) { this.combinationViolations = combinationViolations; }
}
