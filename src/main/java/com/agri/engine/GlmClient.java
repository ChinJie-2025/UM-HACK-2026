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

public class GlmClient {

    // ── Z.AI GLM endpoint & model ────────────────────────────────────────────────
  private static final String API_URL = "https://api.ilmu.ai/anthropic/v1/messages";
    private static final String MODEL   = "ilmu-glm-5.1";

    private static final double TEMPERATURE     = 0.3;
    // ── Increase the patience for high-quality responses ──────────────────────────
private static final int CONNECT_TIMEOUT = 30_000;  // 30 seconds to connect
private static final int READ_TIMEOUT    = 180_000; // 180 seconds (3 minutes) to wait for the full reply
    private static final int    MAX_TOKENS      = 1024; // <-- Add this constant

    private final String       apiKey;
    private final ObjectMapper mapper;

    public GlmClient(String apiKey) {
        this.apiKey  = apiKey;
        this.mapper  = new ObjectMapper();
    }

    /**
     * Sends prompt to the GLM. Currently set to MOCK mode for Hackathon stability.
     */
   public String call(String prompt) throws IOException {
    // 1. Prepare the connection object to use the timeout variables
    URL url = new URL(API_URL);
    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    
    // Applying your declared variables
    conn.setConnectTimeout(CONNECT_TIMEOUT); 
    conn.setReadTimeout(READ_TIMEOUT);
    
    // 2. Log that we are using these settings
    log("INFO", String.format("Initialized connection to %s (Timeout: %ds)", 
        MODEL, (READ_TIMEOUT / 1000)));

    // --- DYNAMIC MOCK BYPASS ---
    // Returns immediately so the 504 Gateway Timeout doesn't happen
    return "{" +
           "\"recommendedCrop\": \"Musang King (Simulated)\"," +
           "\"riskScore\": 6," +
           "\"economicImpact\": 8200.00," +
           "\"reasoning\": \"Simulation active with " + CONNECT_TIMEOUT + "ms connect safety. " +
           "Under current modifiers, Musang King requires a 20% increase in irrigation budget.\"" +
           "}";
}

    private void log(String level, String msg) {
        System.out.println("[" + level + "][GlmClient] " + msg);
    }
}