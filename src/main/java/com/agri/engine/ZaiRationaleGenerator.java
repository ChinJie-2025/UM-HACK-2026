package com.agri.engine;

import src.main.java.com.agri.model.AnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Part 4 - Decision Core
 *
 * ZaiRationaleGenerator extracts the structured fields from the raw text string
 * that GlmClient returned, and constructs a populated AnalysisResult.
 *
 * The GLM was instructed (via PromptBuilder) to return a strict JSON object with
 * four fields: recommendedCrop, reasoning, riskScore, economicImpact.
 *
 * This class handles:
 *   • Stripping accidental markdown fences (```json ... ```) that LLMs sometimes add.
 *   • Clamping riskScore to the valid 1–10 range.
 *   • Returning a safe fallback AnalysisResult if JSON parsing fails entirely,
 *     so that the rest of the pipeline never receives a null.
 *
 * Uses the 4-arg AnalysisResult constructor defined in model/AnalysisResult.java (zip1):
 *   new AnalysisResult(recommendedCrop, reasoning, riskScore, economicImpact)
 */
public class ZaiRationaleGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses {@code rawAiResponse} and returns a fully-populated AnalysisResult.
     * Never returns null – falls back to a safe default on any parse failure.
     *
     * @param rawAiResponse The plain-text content string returned by GlmClient.
     * @return              A populated AnalysisResult.
     */
    public AnalysisResult parse(String rawAiResponse) {

        try {
            String cleaned = stripMarkdownFences(rawAiResponse);
            JsonNode node  = mapper.readTree(cleaned);

            // ── Extract fields, with sensible defaults if a field is absent ─────
            String recommendedCrop = node.path("recommendedCrop").asText("Unknown");
            String reasoning       = node.path("reasoning").asText("No reasoning provided.");
            int    riskScore       = clampRiskScore(node.path("riskScore").asInt(5));
            double economicImpact  = node.path("economicImpact").asDouble(0.0);

            log("INFO", "Parsed: crop=" + recommendedCrop
                    + ", risk=" + riskScore
                    + ", impact=RM" + String.format("%.2f", economicImpact));

            // Uses the 4-arg constructor from AnalysisResult (zip1)
            return new AnalysisResult(recommendedCrop, reasoning, riskScore, economicImpact);

        } catch (IOException e) {
            // Parsing failed – log the raw response so developers can debug, then
            // return a safe fallback so the pipeline doesn't crash entirely.
            log("WARN", "JSON parse failed: " + e.getMessage());
            log("WARN", "Raw AI response was:\n" + rawAiResponse);
            return new AnalysisResult(
                    "Unavailable",
                    "AI response could not be parsed. Please retry or check GLM output.",
                    5,
                    0.0
            );
        }
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    /**
     * Removes markdown code fences that the GLM may wrap around JSON output.
     * Handles both ``` and ```json variants.
     *
     * Examples cleaned:
     *   ```json           →  (stripped)
     *   { "recommendedCrop": ... }
     *   ```               →  (stripped)
     */
    private String stripMarkdownFences(String raw) {
        if (raw == null) return "{}";

        String trimmed = raw.trim();

        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            int lastFence    = trimmed.lastIndexOf("```");

            // Only strip if there is a closing fence after the opening line
            if (firstNewline != -1 && lastFence > firstNewline) {
                return trimmed.substring(firstNewline + 1, lastFence).trim();
            }
        }

        return trimmed;
    }

    /**
     * Ensures riskScore stays within the documented 1–10 range from AnalysisResult.
     */
    private int clampRiskScore(int score) {
        return Math.max(1, Math.min(10, score));
    }

    private void log(String level, String msg) {
        System.out.println("[" + level + "][ZaiRationaleGenerator] " + msg);
    }
}
