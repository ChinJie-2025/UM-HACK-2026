package com.agri.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.beans.factory.annotation.Value;

import java.util.*;

@Service
public class ZAIService {

    private final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";

    @Value("${ZAI_API_KEY}")
    private String API_KEY;

    public String getAIResponse(String userMsg, String crop, String marketData, String newsData) {

        RestTemplate restTemplate = new RestTemplate();

        String prompt = "You are Z.AI, an agricultural expert.\n\n"
                + "User Crop: " + crop + "\n"
                + "User Question: " + userMsg + "\n\n"
                + "Market Data:\n" + marketData + "\n\n"
                + "Field News:\n" + newsData + "\n\n"
                + "Give clear, practical farming advice.";

        Map<String, Object> body = new HashMap<>();
        body.put("model", "glm-4-flash");

        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> msg = new HashMap<>();
        msg.put("role", "user");
        msg.put("content", prompt);
        messages.add(msg);

        body.put("messages", messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        // 🔥 DEBUG
        System.out.println("=== DEBUG ===");
        System.out.println("API_KEY: " + API_KEY);
        System.out.println("HEADERS: " + headers);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(API_URL, request, Map.class);

            Map choice = (Map)((List)response.getBody().get("choices")).get(0);
            Map message = (Map)choice.get("message");

            return message.get("content").toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "Z.AI Error: " + e.getMessage();
        }
    }
}