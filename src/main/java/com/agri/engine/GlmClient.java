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

/**
 * Part 4 - Decision Core
 *
 * GlmClient manages a single HTTP POST to Z.AI's GLM-4 chat-completions endpoint.
 * It is intentionally minimal: its only job is to send a prompt and return
 * the raw content string from the GLM's first response choice.
 *
 * All JSON building / parsing of the outer GLM envelope is done here with Jackson.
 * Parsing the *content* (the agri recommendation) is delegated to ZaiRationaleGenerator.
 *
 * Dependencies: Jackson Databind (already referenced in Part 8 / AppConfig).
 */
public class GlmClient {

    // Low temperature → more deterministic JSON output from the GLM
    
    

    // ── Z.AI GLM endpoint & model ────────────────────────────────────────────────
  private static final String API_URL = "https://api.ilmu.ai/anthropic";
    private static final String MODEL   = "ilmu-glm-5.1";

    // Low temperature → more deterministic JSON output from the GLM
    private static final double TEMPERATURE     = 0.3;
    // ── Increase the patience for high-quality responses ──────────────────────────
private static final int CONNECT_TIMEOUT = 30_000;  // 30 seconds to connect
private static final int READ_TIMEOUT    = 180_000; // 180 seconds (3 minutes) to wait for the full reply
    private static final int    MAX_TOKENS      = 1024; // <-- Add this constant

    private final String       apiKey;
    private final ObjectMapper mapper;

    /**
     * @param apiKey Your Z.AI API key. Retrieve this from AppConfig – never hardcode it.
     */
    public GlmClient(String apiKey) {
        this.apiKey  = apiKey;
        this.mapper  = new ObjectMapper();
    }

    /**
     * Sends {@code prompt} to the GLM and returns the plain-text content of
     * the first choice in the API response.
     *
     * @param prompt The full prompt string produced by PromptBuilder.
     * @return       The raw content string from the AI (expected to be JSON).
     * @throws IOException If the network call fails or the API returns an error status.
     */
    public String call(String prompt) throws IOException {

        // ── 1. Build the request JSON body ────────────────────────────────────────
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("temperature", TEMPERATURE);
        requestBody.put("max_tokens", MAX_TOKENS);

        ArrayNode messages   = mapper.createArrayNode();
        ObjectNode userMsg   = mapper.createObjectNode();
        userMsg.put("role",    "user");
        userMsg.put("content", prompt);
        messages.add(userMsg);
        requestBody.set("messages", messages);

        String jsonBody = mapper.writeValueAsString(requestBody);

        // ── 2. Open HTTP connection ───────────────────────────────────────────────
        HttpURLConnection conn = (HttpURLConnection) new URL(API_URL).openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type",  "application/json");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
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
                "[GlmClient] GLM API returned HTTP " + statusCode + ": " + rawResponse
            );
        }

        // ── 6. Extract content from GLM envelope ──────────────────────────────────
        // GLM response shape: { "choices": [ { "message": { "content": "..." } } ] }
        JsonNode root    = mapper.readTree(rawResponse.toString());
        JsonNode choices = root.path("choices");

        if (!choices.isArray() || choices.size() == 0) {
            throw new IOException("[GlmClient] No choices returned in GLM response: " + rawResponse);
        }

        JsonNode content = choices.get(0).path("message").path("content");

        if (content.isMissingNode() || content.isNull()) {
            throw new IOException("[GlmClient] 'content' field missing in GLM response: " + rawResponse);
        }

        log("INFO", "GLM call successful (HTTP " + statusCode + ").");
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
