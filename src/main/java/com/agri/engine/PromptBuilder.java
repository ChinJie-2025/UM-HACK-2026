package com.agri.engine;

import com.agri.model.CropData;
import com.agri.model.FarmerProfile;

import java.util.List;

/**
 * Part 4 - Decision Core
 *
 * PromptBuilder assembles a structured, high-precision prompt for the GLM model.
 * The goal is to FORCE deterministic JSON output for downstream parsing.
 */
public class PromptBuilder {

    public String build(FarmerProfile profile,
                        List<CropData> marketData,
                        String weatherContext) {

        if (profile == null || marketData == null) {
            throw new IllegalArgumentException("Profile and marketData cannot be null");
        }

        if (weatherContext == null) {
            weatherContext = "No weather or news data available.";
        }

        StringBuilder prompt = new StringBuilder();

        // ── SYSTEM ROLE ──────────────────────────────────────────────────────────
        prompt.append("You are an expert agricultural investment advisor for Malaysian smallholder farmers.\n");
        prompt.append("You make data-driven, risk-aware, economically rational farming recommendations.\n\n");

        // ── FARMER PROFILE ───────────────────────────────────────────────────────
        prompt.append("=== FARMER PROFILE ===\n");
        prompt.append("Location       : ").append(profile.getLocation()).append("\n");
        prompt.append("Budget (RM)    : ").append(String.format("%.2f", profile.getBudget())).append("\n");
        prompt.append("Risk Profile   : ").append(profile.getRiskTolerance()).append("\n\n");

        // ── MARKET DATA ──────────────────────────────────────────────────────────
        prompt.append("=== MARKET DATA ===\n");
        for (CropData crop : marketData) {
            if (crop == null || crop.getName() == null) continue;

            prompt.append("- ")
                    .append(crop.getName())
                    .append(" | Price: RM ")
                    .append(String.format("%.2f", crop.getMarketPrice()))
                    .append(" per ton\n");
        }
        prompt.append("\n");

        // ── FIELD INTELLIGENCE ───────────────────────────────────────────────────
        prompt.append("=== FIELD INTELLIGENCE ===\n");
        prompt.append(weatherContext).append("\n\n");

        // ── DECISION RULES (IMPORTANT FOR MODEL QUALITY) ────────────────────────
        prompt.append("=== DECISION RULES ===\n");
        prompt.append("- Prioritise high economic return relative to risk\n");
        prompt.append("- Penalise crops sensitive to adverse weather conditions\n");
        prompt.append("- Respect farmer budget constraints strictly\n");
        prompt.append("- Prefer stable yield crops for low-risk profiles\n\n");

        // ── TASK ────────────────────────────────────────────────────────────────
        prompt.append("=== TASK ===\n");
        prompt.append("Select the SINGLE best crop for this farmer based on all inputs.\n\n");

        // ── STRICT OUTPUT CONTRACT ───────────────────────────────────────────────
        prompt.append("CRITICAL: Output MUST be valid JSON ONLY.\n");
        prompt.append("NO explanations, NO markdown, NO backticks, NO extra text.\n");
        prompt.append("Return EXACTLY this format:\n\n");

        prompt.append("{\n");
        prompt.append("  \"recommendedCrop\": \"string\",\n");
        prompt.append("  \"reasoning\": \"string\",\n");
        prompt.append("  \"riskScore\": integer (1-10),\n");
        prompt.append("  \"economicImpact\": number\n");
        prompt.append("}\n");

        return prompt.toString();
    }
}