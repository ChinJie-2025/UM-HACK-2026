public class FarmerProfile {
    private String location;
    private double landSize;      // in Hectares or Acres
    private double budget;        // Available capital
    private String riskTolerance; // e.g., "Conservative", "Balanced", "Aggressive"

    public FarmerProfile() {}

    public FarmerProfile(String location, double landSize, double budget, String riskTolerance) {
        this.location = location;
        this.landSize = landSize;
        this.budget = budget;
        this.riskTolerance = riskTolerance;
    }

    // Getters and Setters
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getBudget() { return budget; }
    public void setBudget(double budget) { this.budget = budget; }
    public String getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(String riskTolerance) { this.riskTolerance = riskTolerance; }
}