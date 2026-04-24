package com.agri.sandbox;

import com.agri.model.*;
import java.util.List;
import java.io.IOException;

public class SimulationController {
    private ScenarioSolver solver;
    private FarmerProfile currentSessionProfile;
    // We need these to re-run the AI engine
    private List<CropData> marketData; 
    private String weatherContext;

    public SimulationController(ScenarioSolver solver, FarmerProfile profile, List<CropData> market, String weather) {
        this.solver = solver;
        this.currentSessionProfile = profile;
        this.marketData = market;
        this.weatherContext = weather;
    }

    public void handleSimulationRequest(SimulationRequest request) {
    System.out.println("--- Starting Simulation Session ---");
    
    try {
        // Merge original weather with the simulation's environmental context
        String simulatedWeather = weatherContext + " | SIMULATED OVERLAY: " + request.getEnvironmentalContext();

        AnalysisResult result = solver.solve(
            currentSessionProfile, 
            marketData, 
            simulatedWeather, // Use the merged string here
            request.getModifiers()
        );
        
        displaySideBySide(result);
    } catch (IOException e) {
        System.err.println("Simulation failed: " + e.getMessage());
    }
}

    private void displaySideBySide(AnalysisResult hypothetical) {
        System.out.println("\n=== COMPARATIVE VIEW ===");
        System.out.println("Original Plan    : [Standard ROI Calculation]");
        System.out.println("Simulated Crop   : " + hypothetical.getRecommendedCrop());
        System.out.println("Simulated Impact : RM " + String.format("%.2f", hypothetical.getEconomicImpact()));
        System.out.println("Risk Score       : " + hypothetical.getRiskScore() + "/10");
        System.out.println("AI Rationale     : " + hypothetical.getReasoning());
        
        if (hypothetical.getStrategyBreakdown() != null) {
             System.out.println("Conservative Alternative: " + hypothetical.getStrategyBreakdown().get("Conservative"));
        }
    }
}