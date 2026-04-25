package com.agri.Controller;

import com.agri.engine.DecisionService;
import com.agri.model.AnalysisResult;
import com.agri.model.FarmerProfile;
import com.agri.model.CropData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*") // Allows your UI to connect without CORS errors
public class AnalysisController {

    @Autowired
    private DecisionService decisionService;

    /**
     * This endpoint handles the actual AI request from the chat.
     */
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestAnalysis(@RequestBody FarmerProfile profile) {
        try {
            // Retrieve market and weather data (you may need to inject a MarketService here)
            // For now, using placeholders to ensure the pipeline runs
            List<CropData> marketData = List.of(); 
            String weatherContext = "Stable conditions reported for current plot.";

            // 1. Execute Z.AI Pipeline
            AnalysisResult result = decisionService.analyze(profile, marketData, weatherContext);

            // 2. Build the Chat Bubble Response
            Map<String, Object> response = new HashMap<>();
            response.put("type", "graph_recommendation");
            response.put("sender", "Z.AI Assistant");
            response.put("message", "Analysis complete for " + result.getRecommendedCrop() + ".");
            response.put("crop", result.getRecommendedCrop());
            response.put("reasoning", result.getReasoning());
            
            // 3. Graph Data Extraction (Matches your AnalysisResult.java)
            response.put("graphData", result.getPlotData()); 

            return ResponseEntity.ok(response);

        } catch (Exception e) {
    // THIS WILL PRINT THE ACTUAL ERROR TO YOUR INTELLIJ CONSOLE
    e.printStackTrace(); 
    
    Map<String, Object> errorResponse = new HashMap<>();
    // Send the actual error message back to curl for debugging
    errorResponse.put("error", e.getMessage()); 
    return ResponseEntity.internalServerError().body(errorResponse);
}
    }
}