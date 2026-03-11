package com.dermadata.dto;

public class IngredientResult {
    private String inciName;
    private Double detectedConcentration;
    private Double euMaxConcentration;
    private Boolean prohibited;
    private Boolean restricted;
    private String status;
    private String regulationRef;
    private String conditions;
    private Integer penaltyPoints;

    public IngredientResult() {}

    public IngredientResult(String inciName, Double detectedConcentration, Double euMaxConcentration,
                            Boolean prohibited, Boolean restricted, String status,
                            String regulationRef, String conditions, Integer penaltyPoints) {
        this.inciName = inciName;
        this.detectedConcentration = detectedConcentration;
        this.euMaxConcentration = euMaxConcentration;
        this.prohibited = prohibited;
        this.restricted = restricted;
        this.status = status;
        this.regulationRef = regulationRef;
        this.conditions = conditions;
        this.penaltyPoints = penaltyPoints;
    }

    public String getInciName() { return inciName; }
    public void setInciName(String inciName) { this.inciName = inciName; }
    public Double getDetectedConcentration() { return detectedConcentration; }
    public void setDetectedConcentration(Double detectedConcentration) { this.detectedConcentration = detectedConcentration; }
    public Double getEuMaxConcentration() { return euMaxConcentration; }
    public void setEuMaxConcentration(Double euMaxConcentration) { this.euMaxConcentration = euMaxConcentration; }
    public Boolean getProhibited() { return prohibited; }
    public void setProhibited(Boolean prohibited) { this.prohibited = prohibited; }
    public Boolean getRestricted() { return restricted; }
    public void setRestricted(Boolean restricted) { this.restricted = restricted; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRegulationRef() { return regulationRef; }
    public void setRegulationRef(String regulationRef) { this.regulationRef = regulationRef; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public Integer getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(Integer penaltyPoints) { this.penaltyPoints = penaltyPoints; }
}
