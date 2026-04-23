import java.util.Map;
import java.util.HashMap;

public class AnalysisResult {
    // 1. Fields
    private String recommendedCrop;
    private String recommendedStrategy;
    private String reasoning;
    private int riskScore;
    private double economicImpact;
    private Map<String, String> strategyBreakdown;

    // 2. Default Constructor
    public AnalysisResult() {
        // Initialize the map to prevent NullPointerExceptions later
        this.strategyBreakdown = new HashMap<>();
    }

    // 3. Fully-Loaded Constructor
    // It's best practice to include all core fields here
    public AnalysisResult(String recommendedCrop, String recommendedStrategy, String reasoning, 
                          int riskScore, double economicImpact, Map<String, String> strategyBreakdown) {
        this.recommendedCrop = recommendedCrop;
        this.recommendedStrategy = recommendedStrategy;
        this.reasoning = reasoning;
        this.riskScore = riskScore;
        this.economicImpact = economicImpact;
        this.strategyBreakdown = strategyBreakdown;
    }

    // 4. Getters and Setters
    // Every private field should generally have a way to be accessed/modified
    
    public String getRecommendedCrop() { return recommendedCrop; }
    public void setRecommendedCrop(String recommendedCrop) { this.recommendedCrop = recommendedCrop; }

    public String getRecommendedStrategy() { return recommendedStrategy; }
    public void setRecommendedStrategy(String recommendedStrategy) { this.recommendedStrategy = recommendedStrategy; }

    public String getReasoning() { return reasoning; }
    public void setReasoning(String reasoning) { this.reasoning = reasoning; }

    public int getRiskScore() { return riskScore; }
    public void setRiskScore(int riskScore) { this.riskScore = riskScore; }

    public double getEconomicImpact() { return economicImpact; }
    public void setEconomicImpact(double economicImpact) { this.economicImpact = economicImpact; }

    public Map<String, String> getStrategyBreakdown() { return strategyBreakdown; }
    public void setStrategyBreakdown(Map<String, String> strategyBreakdown) { this.strategyBreakdown = strategyBreakdown; }

    // 5. Helper Method (Optional but useful)
    @Override
    public String toString() {
        return "Analysis for " + recommendedCrop + " (Risk: " + riskScore + "/10)";
    }
}