package com.dermadata.dto;

public class CombinationWarning {
    private String ingredientA;
    private String ingredientB;
    private String condition;
    private String explanation;
    private String source;
    private Integer penaltyPoints;

    public CombinationWarning() {}

    public CombinationWarning(String ingredientA, String ingredientB, String condition,
                              String explanation, String source, Integer penaltyPoints) {
        this.ingredientA = ingredientA;
        this.ingredientB = ingredientB;
        this.condition = condition;
        this.explanation = explanation;
        this.source = source;
        this.penaltyPoints = penaltyPoints;
    }

    public String getIngredientA() { return ingredientA; }
    public void setIngredientA(String ingredientA) { this.ingredientA = ingredientA; }
    public String getIngredientB() { return ingredientB; }
    public void setIngredientB(String ingredientB) { this.ingredientB = ingredientB; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getPenaltyPoints() { return penaltyPoints; }
    public void setPenaltyPoints(Integer penaltyPoints) { this.penaltyPoints = penaltyPoints; }
}
