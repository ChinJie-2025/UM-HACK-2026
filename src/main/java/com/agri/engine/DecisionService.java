package com.agri.engine;

import com.agri.model.AnalysisResult;
import com.agri.model.CropData;
import com.agri.model.FarmerProfile;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import com.agri.ledger.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DecisionService {

    private final PromptBuilder promptBuilder;
    private final GlmClient glmClient;
    private final ZaiRationaleGenerator rationaleGenerator;
    private final MultiStrategyGenerator strategyGenerator;
    private final DecisionLogger decisionLogger; 

    @Autowired
    public DecisionService(@Value("${ZAI_API_KEY}") String glmApiKey, 
                           DecisionLogger decisionLogger) { 
        this.promptBuilder = new PromptBuilder();
        this.glmClient = new GlmClient(glmApiKey);
        this.rationaleGenerator = new ZaiRationaleGenerator();
        this.strategyGenerator = new MultiStrategyGenerator();
        this.decisionLogger = decisionLogger; // Correctly initialize the injected logger
    }
    

    /**
     * Runs the full decision-core pipeline and returns a complete AnalysisResult.
     *
     * @param profile        The farmer's profile (location, budget, risk tolerance).
     * @param marketData     Crop market data from MarketDataClient (Part 3).
     * @param weatherContext Unstructured field intelligence from WeatherNewsClient (Part 3).
     * @return               A complete AnalysisResult with rationale + 3 strategies.
     * @throws IOException   If the GLM API call fails (network error or non-2xx response).
     */
    // Inside DecisionService.java
public AnalysisResult analyze(FarmerProfile profile, List<CropData> marketData, String weatherContext) throws IOException {
    System.out.println("[DecisionService] Step 1: Building Prompt...");
    String prompt = promptBuilder.build(profile, marketData, weatherContext);
    
    System.out.println("[DecisionService] Step 2: Calling GLM API (This might take a while)...");
    String rawResponse = glmClient.call(prompt); // <--- IF STUCK, IT STOPS HERE
    
    System.out.println("[DecisionService] Step 3: Parsing Response...");
    AnalysisResult result = rationaleGenerator.parse(rawResponse);
    
    System.out.println("[DecisionService] Step 4: Generating Strategies...");
    Map<String, String> strategies = strategyGenerator.generate(result, profile, marketData);
    
    result.setStrategyBreakdown(strategies);
    // NEW: Auto-log every recommendation to the JSON ledger
    String recId = decisionLogger.log(profile.getFarmerName(), result);
    result.setRecommendationId(recId);
    return result;
}

    private void log(String level, String msg) {
        System.out.println("[" + level + "][DecisionService] " + msg);
    }
    public Map<String, Object> getAnalysisWithGraph(FarmerProfile profile, List<CropData> market, String weather) throws IOException {
    // 1. Run the existing Z.AI pipeline
    AnalysisResult result = analyze(profile, market, weather);

    // 2. Prepare the response packet
    Map<String, Object> response = new HashMap<>();
    response.put("textReasoning", result.getReasoning());
    response.put("recommendedCrop", result.getRecommendedCrop());
    response.put("graphData", result.getPlotData()); // The numeric values for the Y-axis
    response.put("labels", result.getPlotData().keySet()); // The strategy names for the X-axis

    return response;
}
    public String processNaturalLanguage(String userMessage) {
    try {
        // Create a simple chat-focused prompt
        String chatPrompt = "You are a helpful Malaysian agricultural assistant. " +
                            "Answer the following farmer's question concisely: " + userMessage;
        
        // Reuse the GlmClient logic
        return glmClient.call(chatPrompt);
    } catch (IOException e) {
        log("ERROR", "Chat processing failed: " + e.getMessage());
        return "I'm sorry, I'm having trouble connecting to my brain right now. Please try again.";
    }
}
}
