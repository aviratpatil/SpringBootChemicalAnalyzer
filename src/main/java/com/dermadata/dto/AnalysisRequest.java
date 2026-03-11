package com.dermadata.dto;

import java.util.List;

public class AnalysisRequest {
    private List<IngredientInput> ingredients;
    private String productType;
    private String imageBase64;
    private String rawText;

    public AnalysisRequest() {}

    public AnalysisRequest(List<IngredientInput> ingredients, String productType, String imageBase64, String rawText) {
        this.ingredients = ingredients;
        this.productType = productType;
        this.imageBase64 = imageBase64;
        this.rawText = rawText;
    }

    public List<IngredientInput> getIngredients() { return ingredients; }
    public void setIngredients(List<IngredientInput> ingredients) { this.ingredients = ingredients; }
    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }
    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
    public String getRawText() { return rawText; }
    public void setRawText(String rawText) { this.rawText = rawText; }
}
