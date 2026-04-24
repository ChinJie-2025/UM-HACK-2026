package com.agri.model;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher; // Required for regex processing
import java.util.HashMap;
import java.util.LinkedHashMap;

public class AnalysisResult {
    private String recommendedCrop;
    private String recommendedStrategy;
    private String reasoning;
    private int riskScore;
    private double economicImpact;
    private Map<String, String> strategyBreakdown;

    public AnalysisResult() {
        this.strategyBreakdown = new HashMap<>();
    }

    public AnalysisResult(String recommendedCrop, String reasoning, int riskScore, double economicImpact) {
        this.recommendedCrop = recommendedCrop;
        this.reasoning = reasoning;
        this.riskScore = riskScore;
        this.economicImpact = economicImpact;
        this.recommendedStrategy = "Balanced"; 
        this.strategyBreakdown = new HashMap<>();
    }

    /**
     * Extracts numeric values for graph plotting.
     * This matches the format: "Projected Net Return: RM 123.45"
     */
   // Inside AnalysisResult.java
// AnalysisResult.java
public Map<String, Double> getPlotData() {
    Map<String, Double> plotData = new LinkedHashMap<>(); //
    if (this.strategyBreakdown == null) return plotData; //

    // This regex looks for digits and a decimal point after the RM prefix
    Pattern p = Pattern.compile("Projected Net Return: RM ([\\d,]+\\.?\\d*)");

    this.strategyBreakdown.forEach((name, description) -> {
        Matcher m = p.matcher(description);
        if (m.find()) {
            plotData.put(name, Double.parseDouble(m.group(1))); //
        }
    });
    return plotData;
}

    // Standard Getters and Setters
    public String getRecommendedCrop() { return recommendedCrop; }
    public void setRecommendedCrop(String recommendedCrop) { this.recommendedCrop = recommendedCrop; }
    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }
    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }
    public double getEconomicImpact() { return economicImpact; }
    public void setEconomicImpact(double economicImpact) { this.economicImpact = economicImpact; }
    public Map<String, String> getStrategyBreakdown() { return strategyBreakdown; }
    public void setStrategyBreakdown(Map<String, String> strategyBreakdown) { this.strategyBreakdown = strategyBreakdown; }
    public String getRecommendedStrategy() { return recommendedStrategy; }
    public void setRecommendedStrategy(String recommendedStrategy) { 
     this.recommendedStrategy = recommendedStrategy; 
}}