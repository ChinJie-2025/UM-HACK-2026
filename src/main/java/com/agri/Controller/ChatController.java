package com.agri.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.agri.engine.DecisionService;
import com.agri.ledger.DecisionLogger;
import com.agri.ledger.LedgerEntry;
import com.agri.ledger.UserActionTracker;
import com.agri.model.AnalysisResult;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private DecisionService decisionService;

    private final DecisionLogger decisionLogger = new DecisionLogger();
    @PostMapping("/chat/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        String cropName = payload.getOrDefault("cropName", "Musang King");
        String aiResponse;

        if (userMessage != null && userMessage.toLowerCase().contains("weather")) {
            aiResponse = "### 🌦️ Weather Risk: Moderate\n\nRainfall expected. Check drainage.";
        } else {
            aiResponse = decisionService.processNaturalLanguage(userMessage);
        }

        try {
            AnalysisResult res = new AnalysisResult();
            res.setRecommendedCrop(cropName);
            res.setReasoning(aiResponse);
                String recommendationId = decisionLogger.log("Tan Winny", res);
                return ResponseEntity.ok(Map.of(
                        "reply", aiResponse,
                        "recommendationId", recommendationId
                ));
            } catch (Exception e) {
                System.err.println("Ledger save error: " + e.getMessage());
                return ResponseEntity.ok(Map.of("reply", aiResponse));
            }
    }

    @GetMapping("/ledger/all")
    public ResponseEntity<List<LedgerEntry>> getAllDecisions() {
        try {
            return ResponseEntity.ok(decisionLogger.loadAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

        @PostMapping("/ledger/save")
        public ResponseEntity<String> saveDecision(@RequestBody Map<String, String> payload) {
            String recommendationId = payload.get("recommendationId");
            String chosenAction = payload.getOrDefault("chosenStrategy", "ignored").trim().toLowerCase();

            if (recommendationId == null || recommendationId.isBlank()) {
                return ResponseEntity.badRequest().body("Missing recommendationId");
            }

            if (!"implement".equals(chosenAction) && !"ignored".equals(chosenAction)) {
                return ResponseEntity.badRequest().body("chosenStrategy must be 'implement' or 'ignored'");
            }

            LedgerEntry entry = decisionLogger.findById(recommendationId);
            if (entry == null) {
                return ResponseEntity.badRequest().body("Recommendation not found: " + recommendationId);
            }

            try {
                entry.setChosenPlan(chosenAction);
                decisionLogger.updateEntry(entry);
                return ResponseEntity.ok("Decision Logged Successfully");
            } catch (Exception e) {
                return ResponseEntity.internalServerError().body(e.getMessage());
            }
    }
}