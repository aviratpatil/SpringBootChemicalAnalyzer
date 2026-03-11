package com.dermadata.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "combination_rule")
public class CombinationRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_a", nullable = false)
    private String ingredientA;

    @Column(name = "ingredient_b", nullable = false)
    private String ingredientB;

    @Column(name = "condition_desc", columnDefinition = "TEXT")
    private String condition;

    @Column(name = "safe_concentration_a")
    private Double safeConcentrationA;

    @Column(name = "required_concentration_b")
    private Double requiredConcentrationB;

    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "source")
    private String source;

    public CombinationRule() {}

    public CombinationRule(Long id, String ingredientA, String ingredientB, String condition,
                           Double safeConcentrationA, Double requiredConcentrationB,
                           String explanation, String source) {
        this.id = id;
        this.ingredientA = ingredientA;
        this.ingredientB = ingredientB;
        this.condition = condition;
        this.safeConcentrationA = safeConcentrationA;
        this.requiredConcentrationB = requiredConcentrationB;
        this.explanation = explanation;
        this.source = source;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getIngredientA() { return ingredientA; }
    public void setIngredientA(String ingredientA) { this.ingredientA = ingredientA; }
    public String getIngredientB() { return ingredientB; }
    public void setIngredientB(String ingredientB) { this.ingredientB = ingredientB; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Double getSafeConcentrationA() { return safeConcentrationA; }
    public void setSafeConcentrationA(Double safeConcentrationA) { this.safeConcentrationA = safeConcentrationA; }
    public Double getRequiredConcentrationB() { return requiredConcentrationB; }
    public void setRequiredConcentrationB(Double requiredConcentrationB) { this.requiredConcentrationB = requiredConcentrationB; }
    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
