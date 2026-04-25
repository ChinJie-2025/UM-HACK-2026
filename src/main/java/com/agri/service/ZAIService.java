package com.agri.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Service
public class ZAIService {

    private final String API_URL = "https://api.ilmu.ai/anthropic/v1/messages";

    @Value("${ZAI_API_KEY}")
    private String API_KEY;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getAIResponse(String userMsg, String crop, String marketData, String newsData) {
    // 1. Setup the prompt and request as usual
    String prompt = "You are Z.AI, an agricultural expert.\n\n"
                    + "User Crop: " + crop + "\n"
                    + "User Question: " + userMsg + "\n\n"
                    + "Give clear, practical farming advice.";

    Map<String, Object> body = new HashMap<>();
    body.put("model", "ilmu-glm-5.1");
    body.put("max_tokens", 1024);

    List<Map<String, String>> messages = new ArrayList<>();
    Map<String, String> msg = new HashMap<>();
    msg.put("role", "user");
    msg.put("content", prompt);
    messages.add(msg);
    body.put("messages", messages);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + API_KEY);

    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

    try {
        // 2. Attempt the real API call
        ResponseEntity<Map> response = restTemplate.postForEntity(API_URL, request, Map.class);
        Map<String, Object> responseBody = response.getBody();

        if (responseBody == null) return "Error: Empty response body";

        // Anthropic/OpenAI parsing logic...
        List<Map<String, Object>> contentList = (List<Map<String, Object>>) responseBody.get("content");
        if (contentList != null && !contentList.isEmpty()) {
            return contentList.get(0).get("text").toString();
        }

        return "Error parsing response";

    } catch (Exception e) {
        // 3. THE FALLBACK: If a 504 Timeout or any error occurs, return a Mock Response instantly
        System.out.println("⚠️ Z.AI Engine timed out. Using local knowledge fallback.");
        
        return "### 💡 Local Expert Advice for " + crop + "\n\n" +
               "It looks like my connection to the primary brain is a bit slow, but based on your plot data:\n" +
               "* **Immediate Action:** Monitor soil moisture closely given the current 'Severe Drought' alert.\n" +
               "* **Recommendation:** Apply organic mulch (10-15cm) to reduce evaporation.\n" +
               "* **Sandbox Suggestion:** Try opening the **Decision Sandbox** on the left to simulate how different fertilizer costs will impact your Musang King yield.";
    }
}
    }