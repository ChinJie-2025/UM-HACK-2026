package com.agri.sandbox;
import org.springframework.stereotype.Service;
import com.agri.engine.DecisionService;
import com.agri.model.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@Service
public class ScenarioSolver {
    private final DecisionService decisionService;

    public ScenarioSolver(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    /**
     * Accepts variable modifiers (e.g., budget +20%) and re-runs the engine.
     */
    // Inside ScenarioSolver.java
public AnalysisResult solve(FarmerProfile realProfile, 
                            List<CropData> marketData, 
                            String weatherContext, 
                            Map<String, Double> modifiers) throws IOException {
    
    // 1. Create the temporary profile and deep copy plots
    FarmerProfile tempProfile = new FarmerProfile(realProfile.getFarmerName(), realProfile.getRiskTolerance());
    for (CropPlot plot : realProfile.getMyPlots()) {
        tempProfile.addPlot(new CropPlot(
            plot.getPlotId(), plot.getCropName(), plot.getAddress(), 
            plot.getLandSize(), plot.getPlantingDate(), plot.getPlotBudget(),
            plot.getLatitude(), plot.getLongitude()
        ));
    }

    // 2. APPLY THE ACTUAL VARIABLES
    if (modifiers != null) {
        // Handle Fertilizer Cost Impact
        if (modifiers.containsKey("fertilizerCost")) {
            double multiplier = modifiers.get("fertilizerCost");
            // Logic: High fertilizer cost reduces the effective budget for other operations
            for (CropPlot plot : tempProfile.getMyPlots()) {
                plot.setPlotBudget(plot.getPlotBudget() / multiplier); 
            }
            System.out.println("[Sandbox] Applied Fertilizer Modifier: x" + multiplier);
        }

        // Handle Labor Availability
        if (modifiers.containsKey("laborAvailability")) {
            double laborFactor = modifiers.get("laborAvailability");
            // Logic: Low labor might restrict the land size that can be managed
            for (CropPlot plot : tempProfile.getMyPlots()) {
                plot.setLandSize(plot.getLandSize() * laborFactor);
            }
            System.out.println("[Sandbox] Applied Labor Modifier: x" + laborFactor);
        }
    }

    // 3. Re-trigger the engine with the "altered" profile
    return decisionService.analyze(tempProfile, marketData, weatherContext);
}
}