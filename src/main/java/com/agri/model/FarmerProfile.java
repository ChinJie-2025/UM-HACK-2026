package com.agri.model;
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

    /** * Satisfies the requirement for the Decision Sandbox.
     * Treats the sum of all individual plot budgets as the total profile budget.
     */
    public double getBudget() {
        return getTotalExpenses();
    }

    /**
     * Used by ScenarioSolver to 'overlay' a hypothetical budget.
     * Note: Since our budget is plot-based, this setter is a simplified 
     * version for the simulation engine.
     */
    private double simulatedBudget = -1;

    public void setSimulatedBudget(double budget) {
        this.simulatedBudget = budget;
    }

    // You might update getBudget to check if a simulation is active
    public double getActiveBudget() {
        return (simulatedBudget != -1) ? simulatedBudget : getTotalExpenses();
    }
    // --- Cleaned Getters and Setters ---

    // Helper to get the location string for the AI prompt
public String getLocation() {
    if (myPlots.isEmpty()) return "Unknown Location";
    // Get GPS from the first plot for the general context
    CropPlot first = myPlots.get(0); 
    return String.format("GPS(%.6f, %.6f) near %s", 
                         first.getLatitude(), first.getLongitude(), first.getAddress());
}

    public String getFarmerName() { return farmerName; }
    public void setFarmerName(String name) { this.farmerName = name; }

      
    public String getRiskTolerance() { return riskTolerance; }
    public void setRiskTolerance(String risk) { this.riskTolerance = risk; }

    public List<CropPlot> getMyPlots() { return myPlots; }
    
    // Note: totalBudget field was removed because we now calculate it 
    // dynamically via getTotalExpenses().
}