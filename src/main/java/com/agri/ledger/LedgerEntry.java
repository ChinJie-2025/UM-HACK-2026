package com.agri.ledger;

import java.util.Map;

/**
 * Part 6 – Decision Ledger
 *
 * LedgerEntry is the single unit of data persisted by the Decision Ledger.
 * One entry is created per recommendation, then updated in two additional stages:
 *
 *   Stage 1 – Created by DecisionLogger when a recommendation is generated.
 *   Stage 2 – Updated by UserActionTracker when the farmer chooses a strategy.
 *   Stage 3 – Updated by OutcomeInput at harvest time with actual results.
 *
 * The complete lifecycle enables LoopbackService to compare predicted vs actual
 * and compute accuracy/impact metrics across all historical decisions.
 *
 * Serialization:
 *   Jackson ObjectMapper (already in pom.xml) handles JSON read/write.
 *   All fields have public getters + setters so Jackson can (de)serialize without
 *   any additional annotation configuration.
 */
public class LedgerEntry {

    // ── Stage 1: Set by DecisionLogger ───────────────────────────────────────────

    /** UUID-based unique identifier for this recommendation. */
    private String recommendationId;

    /** ISO-8601 timestamp of when the recommendation was generated. */
    private String timestamp;

    /** Farmer's name from FarmerProfile.getFarmerName(). */
    private String farmerName;

    /** The GLM-recommended crop for this season. */
    private String recommendedCrop;

    /** The default recommended strategy from AnalysisResult. */
    private String recommendedStrategy;

    /** Projected net return (RM) from the GLM recommendation. */
    private double projectedEconomicImpact;

    /** Risk score (1–10) assigned by the GLM. */
    private int riskScore;

    /** The GLM's reasoning behind the recommendation. */
    private String reasoning;

    /**
     * All three strategy descriptions from MultiStrategyGenerator.
     * Keys: "Conservative", "Balanced", "Aggressive".
     * Values: pipe-delimited metric strings (from strategyBreakdown).
     */
    private Map<String, String> strategyBreakdown;

    // ── Stage 2: Set by UserActionTracker ────────────────────────────────────────

    /**
     * Which strategy the farmer actually chose.
     * One of: "Conservative", "Balanced", "Aggressive", or null if not yet chosen.
     */
    private String chosenPlan;

    /** ISO-8601 timestamp of when the farmer made their choice. */
    private String choiceTimestamp;

    // ── Stage 3: Set by OutcomeInput ─────────────────────────────────────────────

    /**
     * The farmer's text description of their actual harvest result.
     * Example: "Good yield but pest damage reduced profit."
     */
    private String actualOutcome;

    /**
     * The actual net profit or loss the farmer made (RM).
     * Positive = profit, negative = loss.
     */
    private double actualProfit;

    /** ISO-8601 timestamp of when the harvest result was logged. */
    private String outcomeTimestamp;

    /**
     * True once Stage 3 data has been recorded.
     * LoopbackService only processes entries where this is true.
     */
    private boolean outcomeLogged;

    // ── Constructors ─────────────────────────────────────────────────────────────

    /** No-arg constructor required by Jackson for deserialization. */
    public LedgerEntry() {
        this.outcomeLogged = false;
    }

    // ── Getters and Setters ───────────────────────────────────────────────────────

    public String getRecommendationId() { return recommendationId; }
    public void setRecommendationId(String recommendationId) { this.recommendationId = recommendationId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String farmerName) { this.farmerName = farmerName; }

    public String getRecommendedCrop() { return recommendedCrop; }
    public void setRecommendedCrop(String recommendedCrop) { this.recommendedCrop = recommendedCrop; }

    public String getRecommendedStrategy() { return recommendedStrategy; }
    public void setRecommendedStrategy(String recommendedStrategy) { this.recommendedStrategy = recommendedStrategy; }

    public double getProjectedEconomicImpact() { return projectedEconomicImpact; }
    public void setProjectedEconomicImpact(double projectedEconomicImpact) { this.projectedEconomicImpact = projectedEconomicImpact; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public Map<String, String> getStrategyBreakdown() { return strategyBreakdown; }
    public void setStrategyBreakdown(Map<String, String> strategyBreakdown) { this.strategyBreakdown = strategyBreakdown; }

    public String getChosenPlan() { return chosenPlan; }
    public void setChosenPlan(String chosenPlan) { this.chosenPlan = chosenPlan; }

    public String getChoiceTimestamp() { return choiceTimestamp; }
    public void setChoiceTimestamp(String choiceTimestamp) { this.choiceTimestamp = choiceTimestamp; }

    public String getActualOutcome() { return actualOutcome; }
    public void setActualOutcome(String actualOutcome) { this.actualOutcome = actualOutcome; }

    public double getActualProfit() { return actualProfit; }
    public void setActualProfit(double actualProfit) { this.actualProfit = actualProfit; }

    public String getOutcomeTimestamp() { return outcomeTimestamp; }
    public void setOutcomeTimestamp(String outcomeTimestamp) { this.outcomeTimestamp = outcomeTimestamp; }

    public boolean isOutcomeLogged() { return outcomeLogged; }
    public void setOutcomeLogged(boolean outcomeLogged) { this.outcomeLogged = outcomeLogged; }

    @Override
    public String toString() {
        return String.format("LedgerEntry[id=%s, farmer=%s, crop=%s, projected=RM%.2f, actual=RM%.2f, outcomeLogged=%b]",
                recommendationId, farmerName, recommendedCrop, projectedEconomicImpact, actualProfit, outcomeLogged);
    }
}
