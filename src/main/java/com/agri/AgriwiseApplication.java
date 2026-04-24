package com.agri;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.*;
import java.util.*;

@SpringBootApplication
@RestController
@RequestMapping("/api/crops")
public class AgriwiseApplication {

    private static final String CSV_FILE_PATH = "crop_database.csv";

    public static void main(String[] args) {
        SpringApplication.run(AgriwiseApplication.class, args);
        System.out.println("🚀 AGRI-ANALYST BACKEND IS RUNNING!");
    }

    // 1. REGISTER NEW PLOT
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerPlot(@RequestBody Map<String, Object> payload) {
        saveToCsv(payload);
        
        Map<String, Object> aiResponse = new HashMap<>();
        aiResponse.put("expectedYield", payload.get("cropName").toString().contains("Durian") ? "8.5 MT" : "4.2 MT");
        aiResponse.put("waterReq", "Moderate (Optimized)");
        return ResponseEntity.ok(aiResponse);
    }

    // 2. FETCH ALL PLOTS (Persistence Logic)
    // This allows the website to "ask" for all the data in the CSV
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, String>>> getAllPlots() {
        List<Map<String, String>> plots = new ArrayList<>();
        File file = new File(CSV_FILE_PATH);
        
        if (!file.exists()) return ResponseEntity.ok(plots);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                // Basic check to ensure we have PlotID, Crop, Size, and Date
                if (values.length >= 4) {
                    Map<String, String> plot = new HashMap<>();
                    plot.put("plotId", values[0]);
                    plot.put("cropName", values[1]);
                    plot.put("landSize", values[2]);
                    plot.put("plantingDate", values[3]);
                    // We give these default values because CSV only has basic data
                    plot.put("expectedYield", "Synced"); 
                    plot.put("waterReq", "Optimized");
                    plots.add(plot);
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading CSV: " + e.getMessage());
        }
        return ResponseEntity.ok(plots);
    }

    // 3. AI CHAT ENDPOINT
    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> handleChat(@RequestBody Map<String, String> payload) {
        String msg = payload.get("message").toLowerCase();
        String reply = msg.contains("aggressive") ? "<b>Aggressive Plan:</b> Increase NPK by 15%." : "<b>Analysis:</b> Soil moisture is optimal.";
        
        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        return ResponseEntity.ok(response);
    }

    private void saveToCsv(Map<String, Object> plot) {
        try (FileWriter fw = new FileWriter(CSV_FILE_PATH, true); PrintWriter pw = new PrintWriter(fw)) {
            pw.printf("%s,%s,%s,%s%n", plot.get("plotId"), plot.get("cropName"), plot.get("landSize"), plot.get("plantingDate"));
        } catch (IOException e) { e.printStackTrace(); }
    }
}