package com.dermadata.dto;

public class IngredientInput {
    private String rawName;
    private String inciName;
    private Double concentration;
    private Integer position;
    private Double confidenceScore;

    public IngredientInput() {}

    public IngredientInput(String rawName, String inciName, Double concentration, Integer position, Double confidenceScore) {
        this.rawName = rawName;
        this.inciName = inciName;
        this.concentration = concentration;
        this.position = position;
        this.confidenceScore = confidenceScore;
    }

    public String getRawName() { return rawName; }
    public void setRawName(String rawName) { this.rawName = rawName; }
    public String getInciName() { return inciName; }
    public void setInciName(String inciName) { this.inciName = inciName; }
    public Double getConcentration() { return concentration; }
    public void setConcentration(Double concentration) { this.concentration = concentration; }
    public Integer getPosition() { return position; }
    public void setPosition(Integer position) { this.position = position; }
    public Double getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(Double confidenceScore) { this.confidenceScore = confidenceScore; }
}
