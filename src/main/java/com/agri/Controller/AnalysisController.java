package com.agri.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import com.agri.DTO.AnalysisResponseDTO; // Import your DTO class
import com.agri.engine.DecisionService; // Import your Service class
import java.util.List;
// @RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    // @Autowired
    // private DecisionService decisionService;

    @GetMapping("/latest")
    public ResponseEntity<List<AnalysisResponseDTO>> getLatestAnalysis() {
        // This calls your Engine to get the 3 strategies
        // For now, returning a mock list for your UI testing
        return ResponseEntity.ok(List.of(
            new AnalysisResponseDTO("Corn", "High market demand", 0.2, "RM 5,000", "Conservative"),
            new AnalysisResponseDTO("Durian", "Export potential", 0.7, "RM 15,000", "Aggressive")
        ));
    }
}