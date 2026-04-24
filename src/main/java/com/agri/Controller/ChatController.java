package com.agri.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.agri.DTO.AnalysisResponseDTO; // Import your DTO class
import com.agri.engine.DecisionService; // Import your Service class
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private DecisionService decisionService;

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendMessage(@RequestBody Map<String, String> payload) {
        String userMessage = payload.get("message");
        
        // Call your DecisionService -> GlmClient
        String aiResponse = decisionService.processNaturalLanguage(userMessage);
        
        return ResponseEntity.ok(Map.of("reply", aiResponse));
    }
}
