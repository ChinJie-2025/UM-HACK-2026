import java.util.ArrayList;
import java.util.List;

public class FarmerProfile {
    private String farmerName;
    private String riskTolerance;
    private List<CropPlot> myPlots;

    public FarmerProfile(String farmerName, String riskTolerance) {
        this.farmerName = farmerName;
        this.riskTolerance = riskTolerance;
        this.myPlots = new ArrayList<>();
    }

    public void addPlot(CropPlot plot) {
        this.myPlots.add(plot);
    }

    /** * Calculates the total budget by summing all plots.
     */
    public double getTotalExpenses() {
        double total = 0;
        for (CropPlot plot : myPlots) {
            total += plot.getPlotBudget();
        }
        return total;
    }

    public void displayDashboard() {
        System.out.println("=== Farmer: " + farmerName + " ===");
        System.out.println("Strategy: " + riskTolerance);
        System.out.println("----------------------------");
        if (myPlots.isEmpty()) {
            System.out.println("No plots registered.");
        } else {
            for (CropPlot plot : myPlots) {
                System.out.println(plot.toString());
            }
        }
        System.out.println("----------------------------");
        // We call the logic method here to show the sum
        System.out.printf("TOTAL BUDGET SPENT: $%.2f%n", getTotalExpenses());
    }

    // --- Cleaned Getters and Setters ---

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String name) { this.farmerName = name; }

    public String getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(String risk) { this.riskTolerance = risk; }

    public List<CropPlot> getMyPlots() { return myPlots; }
    
    // Note: totalBudget field was removed because we now calculate it 
    // dynamically via getTotalExpenses().
}