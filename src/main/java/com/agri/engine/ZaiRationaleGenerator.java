package com.agri.engine;

import com.agri.model.AnalysisResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Part 4 - Decision Core
 *
 * ZaiRationaleGenerator extracts structured fields from the raw text string
 * returned by GlmClient. It uses resilient parsing to handle LLM "chatter".
 */
public class ZaiRationaleGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Parses {@code rawAiResponse} and returns a fully-populated AnalysisResult.
     * Never returns null – falls back to a safe default on any parse failure.
     */
    public AnalysisResult parse(String rawAiResponse) {
        try {
            String cleaned = stripMarkdownFences(rawAiResponse);
            JsonNode node  = mapper.readTree(cleaned);

            String recommendedCrop = node.path("recommendedCrop").asText("Unknown");
            String reasoning       = node.path("reasoning").asText("No reasoning provided.");
            int    riskScore       = clampRiskScore(node.path("riskScore").asInt(5));
            double economicImpact  = node.path("economicImpact").asDouble(0.0);

            log("INFO", "Parsed: crop=" + recommendedCrop + ", risk=" + riskScore + ", impact=RM" + String.format("%.2f", economicImpact));

            return new AnalysisResult(recommendedCrop, reasoning, riskScore, economicImpact);

        } catch (IOException e) {
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

    /**
     * Resiliently extracts JSON from raw LLM output.
     * Handles markdown fences (```json) and leading/trailing chatter.
     */
    private String stripMarkdownFences(String raw) {
    if (raw == null || raw.isBlank()) return "{}";
    String cleaned = raw.trim();

    // Remove markdown fences like ```json if present
    if (cleaned.startsWith("```")) {
        int firstNewline = cleaned.indexOf('\n');
        int lastFence    = cleaned.lastIndexOf("```");
        if (firstNewline != -1 && lastFence > firstNewline) {
            cleaned = cleaned.substring(firstNewline + 1, lastFence).trim();
        }
    }

    // Resilience: Find the actual JSON object boundaries
    int firstBrace = cleaned.indexOf('{');
    int lastBrace  = cleaned.lastIndexOf('}');
    
    if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
        return cleaned.substring(firstBrace, lastBrace + 1);
    }
    return cleaned;
}

    private int clampRiskScore(int score) {
        return Math.max(1, Math.min(10, score));
    }

    private void log(String level, String msg) {
        System.out.println("[" + level + "][ZaiRationaleGenerator] " + msg);
    }
}