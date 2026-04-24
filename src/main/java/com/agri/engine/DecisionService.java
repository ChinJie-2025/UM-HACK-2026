package com.agri.engine;

import com.agri.model.AnalysisResult;
import com.agri.model.CropData;
import com.agri.model.FarmerProfile;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
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
 *   Step 2 – GlmClient           : prompt → raw AI content string (JSON)
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
 *   String apiKey = AppConfig.getGlmApiKey();      // Part 8
 *   DecisionService service = new DecisionService(apiKey);
 *
 *   List<CropData>  market  = new MarketDataClient().fetchCurrentMarketPrices();
 *   String          weather = new WeatherNewsClient().fetchUnstructuredContext(profile.getLocation());
 *
 *   AnalysisResult result = service.analyze(profile, market, weather);
 */
@Service
public class DecisionService {

    private final PromptBuilder          promptBuilder;
    private final GlmClient              glmClient;
    private final ZaiRationaleGenerator  rationaleGenerator;
    private final MultiStrategyGenerator strategyGenerator;

    /**
     * Constructs a DecisionService wired with all engine components.
     *
     * @param glmApiKey Your Z.AI API key. Retrieve from AppConfig – never hardcode.
     */
    public DecisionService(@Value("${zai.api.key:mock_key_for_hackathon}")String glmApiKey) {
        this.promptBuilder      = new PromptBuilder();
        this.glmClient          = new GlmClient(glmApiKey);
        this.rationaleGenerator = new ZaiRationaleGenerator();
        this.strategyGenerator  = new MultiStrategyGenerator();
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
    public AnalysisResult analyze(FarmerProfile profile,
                                   List<CropData> marketData,
                                   String weatherContext) throws IOException {

        log("INFO", "=== Decision pipeline started for: " + profile.getLocation() + " ===");

        // ── Step 1: Build the context-aware prompt ────────────────────────────────
        log("INFO", "Step 1/4 – Building prompt...");
        String prompt = promptBuilder.build(profile, marketData, weatherContext);

        // ── Step 2: Call the GLM API ──────────────────────────────────────────────
        log("INFO", "Step 2/4 – Calling GLM API...");
        String rawAiResponse = glmClient.call(prompt);

        // ── Step 3: Parse rationale into AnalysisResult ───────────────────────────
        log("INFO", "Step 3/4 – Parsing AI rationale...");
        AnalysisResult result = rationaleGenerator.parse(rawAiResponse);
        log("INFO", "Recommended crop: " + result.getRecommendedCrop()
                + " | Risk: " + result.getRiskScore()
                + "/10 | Impact: RM" + String.format("%.2f", result.getEconomicImpact()));

        // ── Step 4: Generate the three strategies ─────────────────────────────────
        log("INFO", "Step 4/4 – Generating Conservative / Balanced / Aggressive strategies...");
        Map<String, String> strategies = strategyGenerator.generate(result, profile, marketData);

        // Attach the strategy map to the result.
        // REQUIRES setStrategyBreakdown() to be added to AnalysisResult – see class Javadoc above.
        result.setStrategyBreakdown(strategies);

        log("INFO", "=== Decision pipeline complete ===");
        return result;
    }

    private void log(String level, String msg) {
        System.out.println("[" + level + "][DecisionService] " + msg);
    }
    
    public String processNaturalLanguage(String userMessage) {
        // 1. You might call PromptBuilder here
        // 2. Call GlmClient to get AI response
        // For now, let's return a test string to stop the error:
        return "AI is processing your message: " + userMessage;
    }
}
