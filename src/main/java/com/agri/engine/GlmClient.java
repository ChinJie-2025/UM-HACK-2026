package com.agri.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import com.agri.config.GeminiApiKeyResolver;

/**
 * Part 4 - Decision Core
 *
 * GlmClient manages a single HTTP POST to Google Gemini API endpoint.
 * It is intentionally minimal: its only job is to send a prompt and return
 * the raw content string from the Gemini's first response choice.
 *
 * All JSON building / parsing of the outer Gemini envelope is done here with Jackson.
 * Parsing the *content* (the agri recommendation) is delegated to ZaiRationaleGenerator.
 *
 * Dependencies: Jackson Databind (already referenced in Part 8 / AppConfig).
 */
public class GlmClient {

    // Low temperature → more deterministic JSON output from Gemini
    

    // ── Google Gemini API endpoint & model ────────────────────────────────────────────────
    private static final String API_URL_TEMPLATE = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent";
    private static final String MODEL   = "gemma-3-27b-it";

    // Low temperature → more deterministic JSON output from Gemini
    private static final double TEMPERATURE     = 0.3;
    // ── Increase the patience for high-quality responses ──────────────────────────
    private static final int CONNECT_TIMEOUT = 30_000;  // 30 seconds to connect
    private static final int READ_TIMEOUT    = 180_000; // 180 seconds (3 minutes) to wait for the full reply
    private static final int    MAX_TOKENS      = 1024; // <-- Add this constant

    private final String       apiKey;
    private final ObjectMapper mapper;

    /**
     * @param apiKey Your Google Gemini API key. Retrieve this from AppConfig – never hardcode it.
     */
    public GlmClient(String apiKey) {
        this.apiKey  = GeminiApiKeyResolver.sanitize(apiKey);
        this.mapper  = new ObjectMapper();
        
        // Validate API key at initialization
        if (!GeminiApiKeyResolver.isUsable(this.apiKey)) {
            System.err.println("[ERROR] GlmClient initialized without a valid Gemini API key.");
        }else {
            System.out.println("[INFO] GlmClient initialized with API key: " + this.apiKey.substring(0, Math.min(10, this.apiKey.length())) + "...");
        }
    }

    /**
     * Sends {@code prompt} to Gemini and returns the plain-text content of
     * the first choice in the API response.
     *
     * @param prompt The full prompt string produced by PromptBuilder.
     * @return       The raw content string from the AI (expected to be JSON).
     * @throws IOException If the network call fails or the API returns an error status.
     */
    public String call(String prompt) throws IOException {
        // Validate API key before making request
        if (!GeminiApiKeyResolver.isUsable(apiKey)) {
            throw new IOException("[GlmClient] Cannot call Gemini API: API key is not set or is invalid. Please set GEMINI_API_KEY (or GOOGLE_API_KEY/API_KEY) in environment or .env.");
        }
        
        // ── 1. Build the request JSON body for Gemini API ────────────────────────────────────────
        ObjectNode requestBody = mapper.createObjectNode();
        
        // Gemini uses "contents" instead of "messages"
        ArrayNode contents = mapper.createArrayNode();
        ObjectNode contentObj = mapper.createObjectNode();
        contentObj.put("role", "user");
        
        ArrayNode parts = mapper.createArrayNode();
        ObjectNode part = mapper.createObjectNode();
        part.put("text", prompt);
        parts.add(part);
        
        contentObj.set("parts", parts);
        contents.add(contentObj);
        requestBody.set("contents", contents);

        // Add generation config
        ObjectNode generationConfig = mapper.createObjectNode();
        generationConfig.put("temperature", TEMPERATURE);
        generationConfig.put("maxOutputTokens", MAX_TOKENS);
        requestBody.set("generationConfig", generationConfig);

        String jsonBody = mapper.writeValueAsString(requestBody);

        // ── 2. Open HTTP connection ───────────────────────────────────────────────
        String apiUrl = String.format(API_URL_TEMPLATE, MODEL) + "?key=" + apiKey;
        HttpURLConnection conn = (HttpURLConnection) new URL(apiUrl).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",  "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(CONNECT_TIMEOUT);
        conn.setReadTimeout(READ_TIMEOUT);

        // ── 3. Write request body ─────────────────────────────────────────────────
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        // ── 4. Read response ──────────────────────────────────────────────────────
        int statusCode = conn.getResponseCode();
        InputStream responseStream = (statusCode >= 200 && statusCode < 300)
                ? conn.getInputStream()
                : conn.getErrorStream();

        StringBuilder rawResponse = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(responseStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                rawResponse.append(line);
            }
        }

        // ── 5. Handle non-2xx ─────────────────────────────────────────────────────
        if (statusCode < 200 || statusCode >= 300) {
            throw new IOException(
                "[GlmClient] Gemini API returned HTTP " + statusCode + ": " + rawResponse
            );
        }

        // ── 6. Extract content from Gemini envelope ──────────────────────────────
        // Gemini response shape: { "candidates": [ { "content": { "parts": [ { "text": "..." } ] } } ] }
        JsonNode root    = mapper.readTree(rawResponse.toString());
        JsonNode candidates = root.path("candidates");

        if (!candidates.isArray() || candidates.size() == 0) {
            throw new IOException("[GlmClient] No candidates returned in Gemini response: " + rawResponse);
        }

        JsonNode responseParts = candidates.get(0).path("content").path("parts");

        if (!responseParts.isArray() || responseParts.size() == 0) {
            throw new IOException("[GlmClient] 'parts' array missing in Gemini response: " + rawResponse);
        }

        JsonNode content = responseParts.get(0).path("text");

        if (content.isMissingNode() || content.isNull()) {
            throw new IOException("[GlmClient] 'text' field missing in Gemini response: " + rawResponse);
        }

        log("INFO", "Gemini call successful (HTTP " + statusCode + ").");
        String text = content.asText().trim();

        if (text.startsWith("```")) {
            text = text.replaceAll("```json", "")
                    .replaceAll("```", "")
                    .trim();
        }

        return text;
    }

    private void log(String level, String msg) {
        System.out.println("[" + level + "][GlmClient] " + msg);
    }
}