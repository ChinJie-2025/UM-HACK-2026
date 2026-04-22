import java.util.Map;

public class AnalysisResult {
    private String recommendedCrop;
    private String reasoning;       // For "Explanation of decisions"
    private int riskScore;          // 1-10 scale
    private double economicImpact;  // Quantifiable impact (e.g., Projected ROI)
    
    // For Multi-Strategy (Conservative, Balanced, Aggressive)
    private Map<String, String> strategyBreakdown; 

    public AnalysisResult() {}

    public AnalysisResult(String recommendedCrop, String reasoning, int riskScore, double economicImpact) {
        this.recommendedCrop = recommendedCrop;
        this.reasoning = reasoning;
        this.riskScore = riskScore;
        this.economicImpact = economicImpact;
    }

    // Getters and Setters
    public String getRecommendedCrop() { return recommendedCrop; }
    public String getReasoning() { return reasoning; }
    public int getRiskScore() { return riskScore; }
    public double getEconomicImpact() { return economicImpact; }
}