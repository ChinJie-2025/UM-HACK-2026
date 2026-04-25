package com.agri.engine;

import com.agri.model.AnalysisResult;
import com.agri.model.CropData;
import com.agri.model.FarmerProfile;
import com.agri.config.AppConfig;
import com.agri.config.GeminiApiKeyResolver;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.agri.ledger.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Part 4 - Decision Core
 *
 * DecisionService is the single public entry point for the entire engine package.
 * It owns and wires together all four engine components and exposes one method:
 *
 *   {@link #analyze(FarmerProfile, List, String)}
 *
 * Pipeline (as specified in UM_HACK.md Part 4):
 *   Step 1 – PromptBuilder       : profile + market + weather → prompt string
 *   Step 2 – GlmClient           : prompt → raw Gemini content string (JSON)
 *   Step 3 – ZaiRationaleGenerator : raw content → AnalysisResult (4 core fields)
 *   Step 4 – MultiStrategyGenerator : AnalysisResult → strategyBreakdown map
 *   Step 5 – Assemble final AnalysisResult with strategies and return.
 *
 * ── IMPORTANT: Required patch to AnalysisResult (zip1) before this compiles ──────────
 *
 *   The AnalysisResult model (zip1) declares:
 *       private Map<String, String> strategyBreakdown;
 *   but does NOT expose a setter or getter for it.
 *
 *   Add these two methods to AnalysisResult.java before integrating Part 4:
 *
 *       public void setStrategyBreakdown(Map<String, String> strategyBreakdown) {
 *           this.strategyBreakdown = strategyBreakdown;
 *       }
 *
 *       public Map<String, String> getStrategyBreakdown() {
 *           return strategyBreakdown;
 *       }
 *
 * ── IMPORTANT: Required patch to CropData (zip1) before MarketDataClient compiles ────
 *
 *   MarketDataClient (zip2) calls: new CropData(name, price, yield, water)
 *   But CropData only has a no-arg constructor. Add:
 *
 *       public CropData(String name, double marketPrice, double expectedYield, double waterReq) {
 *           this.name          = name;
 *           this.marketPrice   = marketPrice;
 *           this.expectedYield = expectedYield;
 *           this.waterReq      = waterReq;
 *       }
 *
 * ──────────────────────────────────────────────────────────Adjusted Risk Score──────────────────────────
 *
 * Usage example (from a controller or test):
 *
 *   String apiKey = AppConfig.getGlmApiKey();      // Retrieve from .env GEMINI_API_KEY
 *   DecisionService service = new DecisionService(apiKey);
 *
 *   List<CropData>  market  = new MarketDataClient().fetchCurrentMarketPrices();
 *   String          weather = new WeatherNewsClient().fetchUnstructuredContext(profile.getLocation());
 *
 *   AnalysisResult result = service.analyze(profile, market, weather);
 */
@Service
public class DecisionService {

    private final PromptBuilder promptBuilder;
    private final GlmClient glmClient;
    private final ZaiRationaleGenerator rationaleGenerator;
    private final MultiStrategyGenerator strategyGenerator;
    private final DecisionLogger decisionLogger; 

    /**
     * Constructs a DecisionService wired with all engine components.
     *
     * @param glmApiKey Your Gemini API key. Retrieve from AppConfig – never hardcode.
     */
    public DecisionService(@Value("${GEMINI_API_KEY:}") String glmApiKey) {
        this.promptBuilder      = new PromptBuilder();
        
        String apiKey = GeminiApiKeyResolver.resolve(glmApiKey);
        if (!GeminiApiKeyResolver.isUsable(apiKey)) {
            try {
                apiKey = GeminiApiKeyResolver.resolve(AppConfig.getGlmApiKey());
            } catch (Exception e) {
                apiKey = "";
                System.err.println("[WARN] Could not load API key from AppConfig: " + e.getMessage());
            }
        }
        
        if (GeminiApiKeyResolver.isUsable(apiKey)) {
            System.setProperty("GEMINI_API_KEY", apiKey);
        }

        System.out.println("[INFO] DecisionService initialized with API key: "
                + (GeminiApiKeyResolver.isUsable(apiKey)
                ? apiKey.substring(0, Math.min(10, apiKey.length())) + "..."
                : "NOT SET"));
        this.glmClient          = new GlmClient(apiKey);
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
