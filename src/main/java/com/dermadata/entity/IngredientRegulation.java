package com.dermadata.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "ingredient_regulation")
public class IngredientRegulation {

    @Id
    @Column(name = "inci_name", nullable = false, unique = true)
    private String inciName;

    @Column(name = "max_concentration")
    private Double maxConcentration;

    @Column(name = "restricted")
    private Boolean restricted;

    @Column(name = "prohibited")
    private Boolean prohibited;

    @Column(name = "conditions", columnDefinition = "TEXT")
    private String conditions;

    @Column(name = "product_types")
    private String productTypes;

    @Column(name = "regulation_ref")
    private String regulationRef;

    public IngredientRegulation() {}

    public IngredientRegulation(String inciName, Double maxConcentration, Boolean restricted,
                                 Boolean prohibited, String conditions, String productTypes, String regulationRef) {
        this.inciName = inciName;
        this.maxConcentration = maxConcentration;
        this.restricted = restricted;
        this.prohibited = prohibited;
        this.conditions = conditions;
        this.productTypes = productTypes;
        this.regulationRef = regulationRef;
    }

    public String getInciName() { return inciName; }
    public void setInciName(String inciName) { this.inciName = inciName; }
    public Double getMaxConcentration() { return maxConcentration; }
    public void setMaxConcentration(Double maxConcentration) { this.maxConcentration = maxConcentration; }
    public Boolean getRestricted() { return restricted; }
    public void setRestricted(Boolean restricted) { this.restricted = restricted; }
    public Boolean getProhibited() { return prohibited; }
    public void setProhibited(Boolean prohibited) { this.prohibited = prohibited; }
    public String getConditions() { return conditions; }
    public void setConditions(String conditions) { this.conditions = conditions; }
    public String getProductTypes() { return productTypes; }
    public void setProductTypes(String productTypes) { this.productTypes = productTypes; }
    public String getRegulationRef() { return regulationRef; }
    public void setRegulationRef(String regulationRef) { this.regulationRef = regulationRef; }
}
