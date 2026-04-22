package engine;

import com.agri.model.CropData;
import com.agri.model.FarmerProfile;

import java.util.List;

/**
 * Part 4 - Decision Core
 *
 * PromptBuilder assembles a single, context-aware prompt string from three data sources:
 *   1. FarmerProfile  – who is the farmer, what is their budget and risk posture
 *   2. List<CropData> – current market prices from MarketDataClient (Part 3)
 *   3. weatherContext  – unstructured field intelligence from WeatherNewsClient (Part 3)
 *
 * The output prompt is formatted so that the GLM will always return a strict JSON object,
 * which ZaiRationaleGenerator can parse without ambiguity.
 */
public class PromptBuilder {

    /**
     * Builds and returns the complete prompt string ready to be sent to the GLM.
     *
     * @param profile        The farmer's profile (from FarmerProfile model).
     * @param marketData     The current crop market data (from MarketDataClient).
     * @param weatherContext The unstructured news/weather block (from WeatherNewsClient).
     * @return               A fully-formed prompt string for the GLM.
     */
    public String build(FarmerProfile profile, List<CropData> marketData, String weatherContext) {

        StringBuilder prompt = new StringBuilder();

        // ── System persona ────────────────────────────────────────────────────────
        prompt.append("You are an expert agricultural investment advisor specialising in Malaysian\n");
        prompt.append("smallholder farming. Your recommendations must be grounded in real market\n");
        prompt.append("economics, risk management, and the farmer's specific constraints.\n\n");

        // ── Farmer Profile ────────────────────────────────────────────────────────
        prompt.append("=== FARMER PROFILE ===\n");
        prompt.append("Location        : ").append(profile.getLocation()).append("\n");
        prompt.append("Budget Available: RM ").append(String.format("%.2f", profile.getBudget())).append("\n");
        prompt.append("Risk Tolerance  : ").append(profile.getRiskTolerance()).append("\n\n");

        // ── Current Market Data ───────────────────────────────────────────────────
        // NOTE: Only getName() and getMarketPrice() are exposed on CropData (zip1).
        //       If getExpectedYield() and getWaterReq() are later added to CropData,
        //       include them here for richer prompts.
        prompt.append("=== CURRENT MARKET PRICES ===\n");
        for (CropData crop : marketData) {
            prompt.append("- ").append(crop.getName())
                  .append(" | Market Price: RM ")
                  .append(String.format("%.2f", crop.getMarketPrice()))
                  .append(" per ton\n");
        }
        prompt.append("\n");

        // ── Field Intelligence ────────────────────────────────────────────────────
        prompt.append("=== FIELD INTELLIGENCE (Weather & News) ===\n");
        prompt.append(weatherContext).append("\n\n");

        // ── Output Contract ───────────────────────────────────────────────────────
        prompt.append("=== TASK ===\n");
        prompt.append("Analyse all the information above and identify the single best crop\n");
        prompt.append("for this farmer to invest in this season. Account for weather risk,\n");
        prompt.append("market prices, and the farmer's budget and risk tolerance.\n\n");

        prompt.append("Respond ONLY with a valid JSON object in this EXACT format.\n");
        prompt.append("Do NOT include any text, explanation, or markdown outside the JSON object:\n\n");
        prompt.append("{\n");
        prompt.append("  \"recommendedCrop\": \"<name of the single best crop>\",\n");
        prompt.append("  \"reasoning\": \"<concise explanation of why this crop was chosen>\",\n");
        prompt.append("  \"riskScore\": <integer 1–10, where 1 = lowest risk, 10 = highest risk>,\n");
        prompt.append("  \"economicImpact\": <projected net return in RM as a decimal, e.g. 12500.00>\n");
        prompt.append("}\n");

        return prompt.toString();
    }
}
