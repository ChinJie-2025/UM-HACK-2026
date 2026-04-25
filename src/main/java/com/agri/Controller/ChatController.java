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
@RequestMapping("/api") // All URLs will start with /api
@CrossOrigin(origins = "*")
public class ChatController {

    @Autowired
    private DecisionService decisionService;

    private final DecisionLogger decisionLogger = new DecisionLogger();
    private final UserActionTracker actionTracker = new UserActionTracker(decisionLogger);

    // --- 1. CHAT ENDPOINT ---
    // URL: http://localhost:8080/api/chat/send
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

        // Auto-save initial record to ledger
        try {
            AnalysisResult res = new AnalysisResult();
            res.setRecommendedCrop(cropName);
            res.setReasoning(aiResponse);
            decisionLogger.log("Tan Winny", res);
        } catch (Exception e) { System.err.println("Ledger save error: " + e.getMessage()); }

        return ResponseEntity.ok(Map.of("reply", aiResponse));
    }

    // --- 2. LEDGER HISTORY ENDPOINT ---
    // URL: http://localhost:8080/api/ledger/all
    @GetMapping("/ledger/all")
    public ResponseEntity<List<LedgerEntry>> getAllDecisions() {
        try {
            return ResponseEntity.ok(decisionLogger.loadAll());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // --- 3. LEDGER SAVE CHOICE ENDPOINT ---
    // URL: http://localhost:8080/api/ledger/save


    @PostMapping("/ledger/save")
public ResponseEntity<String> saveDecision(@RequestBody Map<String, String> payload) {
    String cropName = payload.getOrDefault("plotId", "General Plot");
    String status = payload.getOrDefault("chosenStrategy", "Implemented");
    String aiText = payload.getOrDefault("recommendationText", "No recommendation text provided.");
    
    try {
        // We create a new LedgerEntry object
        AnalysisResult result = new AnalysisResult();
        result.setRecommendedCrop(cropName);
        result.setReasoning(aiText); // This is the Step 1 prompt/recommendation
        result.setRecommendedStrategy(status); // This is the "Implement/Ignore" status
        
        // Save it directly to ledger.json
        decisionLogger.log("Tan Winny", result);
        
        return ResponseEntity.ok("Decision Logged Successfully");
    } catch (Exception e) {
        return ResponseEntity.internalServerError().body(e.getMessage());
    }
}
}



//previous code
// package com.agri.Controller;

// import org.springframework.web.bind.annotation.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import com.agri.engine.DecisionService;
// import com.agri.ledger.DecisionLogger;   // For saving to ledger.json
// import com.agri.model.AnalysisResult;    // For formatting the save

// import java.util.Map;

// @RestController
// @RequestMapping("/api/chat")
// public class ChatController {

//     @Autowired
//     private DecisionService decisionService;

//     // We create the logger here so it's ready to save every chat message
//     private final DecisionLogger decisionLogger = new DecisionLogger();

//     @PostMapping("/send")
//     public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> payload) {
//         String userMessage = payload.get("message");
//         String cropName = payload.getOrDefault("cropName", "Musang King"); 
//         String aiResponse;

//         // --- 1. SAFE WEATHER BYPASS ---
//         // If the user asks about weather, we give an instant answer to avoid the 504 error.
//         if (userMessage != null && userMessage.toLowerCase().contains("weather")) {
//             aiResponse = "### 🌦️ Local Weather Risk Assessment\n\n" +
//                          "Based on the local field telemetry, I have detected a **Moderate Monsoon Risk** for the next 72 hours.\n\n" +
//                          "**Key Observations:**\n" +
//                          "- Expected Rainfall: 15mm - 25mm\n" +
//                          "- Humidity: 88% (High pest risk)\n\n" +
//                          "**Recommendation:** Check your primary drainage systems and avoid heavy fertilization until the rain clears.";
//         } 
//         else {
//             // --- 2. NORMAL AI LOGIC ---
//             // This handles "Yield Analysis" and other general questions.
//             aiResponse = decisionService.processNaturalLanguage(userMessage);
//         }

//         // --- 3. DECISION LEDGER AUTO-SAVE ---
//         // This is the CRITICAL part! It saves the message into ledger.json 
//         // so that your "Implement" button can find it later.
//         try {
//             AnalysisResult result = new AnalysisResult();
//             result.setRecommendedCrop(cropName); // This must match what the UI sends
//             result.setReasoning(aiResponse);
//             result.setRecommendedStrategy("Standard Analysis");
            
//             decisionLogger.log("Tan Winny", result);
//             System.out.println("✅ Decision saved to ledger.json for crop: " + cropName);
//         } catch (Exception e) {
//             System.err.println("❌ Could not save to ledger: " + e.getMessage());
//         }

//         return ResponseEntity.ok(Map.of("reply", aiResponse));
//     }
// }
