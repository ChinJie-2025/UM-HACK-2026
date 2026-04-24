package com.agri.sandbox;

import com.agri.engine.DecisionService;
import com.agri.model.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ScenarioSolver {
    private final DecisionService decisionService;

    public ScenarioSolver(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /**
     * Accepts variable modifiers (e.g., budget +20%) and re-runs the engine.
     */
    public AnalysisResult solve(FarmerProfile realProfile, 
                                List<CropData> marketData, 
                                String weatherContext, 
                                Map<String, Double> modifiers) throws IOException {
        
        // 1. CONTEXT RECONSTRUCTION: Create a temporary profile
        FarmerProfile tempProfile = new FarmerProfile(
            realProfile.getFarmerName(), 
            realProfile.getRiskTolerance()
        );
        
        // 2. DEEP COPY: Clone each plot into the temp profile
        // This ensures modifications in the sandbox don't ruin the real data
        for (CropPlot plot : realProfile.getMyPlots()) {
            CropPlot clonedPlot = new CropPlot(
                plot.getPlotId(), 
                plot.getCropName(), 
                plot.getAddress(), 
                plot.getLandSize(), 
                plot.getPlantingDate(), 
                plot.getPlotBudget(),
                plot.getLatitude(),
                plot.getLongitude()
            );
            tempProfile.addPlot(clonedPlot);
        }

        // 3. APPLY VARIABLE MODIFIERS
        // This adjusts the simulated reality based on user input
        if (modifiers.containsKey("budget_multiplier")) {
            double multiplier = modifiers.get("budget_multiplier");
            for (CropPlot plot : tempProfile.getMyPlots()) {
                plot.setPlotBudget(plot.getPlotBudget() * multiplier);
            }
        }

        // 4. RE-TRIGGER THE ENGINE
        System.out.println("[Sandbox] Running simulation with modified parameters...");
        return decisionService.analyze(tempProfile, marketData, weatherContext);
    }
}