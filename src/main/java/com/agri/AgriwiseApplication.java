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
    
    @PostMapping("/chat")
public ResponseEntity<Map<String, String>> handleChat(@RequestBody Map<String, String> payload) {
    String userMsg = payload.get("message").toLowerCase();
    String response = "I'm analyzing your request...";

    // FAQ LOGIC
    if (userMsg.contains("weather")) {
        response = "Z.AI Alert: Monsoon patterns detected near Universiti Malaya. Check your drainage.";
    } else if (userMsg.contains("yield")) {
        // Z.AI refers to the file
        String plotData = getPlotDataSummary();
        response = "Based on your ledger: " + plotData + ". Overall yield is stable.";
    } else {
        response = "I've reviewed your plot info. We should apply a balanced nutrient strategy today.";
    }

    Map<String, String> res = new HashMap<>();
    res.put("reply", response);
    return ResponseEntity.ok(res);
}

// Z.AI refers to the file
private String getPlotDataSummary() {
    try (BufferedReader br = new BufferedReader(new FileReader("crop_database.csv"))) {
        String line = br.readLine(); // Just get the first/latest plot info
        if (line != null) return "Active Plot: " + line.split(",")[1];
    } catch (Exception e) { return "No active plots found"; }
    return "No data";
}

    // 1. Updated Register: Returns the AI values needed for the card
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerPlot(@RequestBody Map<String, Object> payload) {
        saveToCsv(payload);
        Map<String, Object> ai = new HashMap<>();
        ai.put("expectedYield", "4.2 MT/Acre"); // Static demo data
        ai.put("waterReq", "Moderate");
        return ResponseEntity.ok(ai);
    }

    // 2. Updated Loader: Strictly reads the 4 columns you enter in the UI
    @GetMapping("/all")
    public ResponseEntity<List<Map<String, String>>> getAllPlots() {
        List<Map<String, String>> plots = new ArrayList<>();
        File file = new File("crop_database.csv");
        
        if (!file.exists()) return ResponseEntity.ok(plots);

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] v = line.split(",");
                // We match the 4 columns: PlotID, CropName, LandSize, Date
                if (v.length >= 4) {
                    Map<String, String> p = new HashMap<>();
                    p.put("plotId", v[0].trim());
                    p.put("cropName", v[1].trim());
                    p.put("landSize", v[2].trim());
                    p.put("plantingDate", v[3].trim());
                    // Static demo values for the AI analytics
                    p.put("expectedYield", "4.2 MT/Acre"); 
                    p.put("waterReq", "Moderate");
                    plots.add(p);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok(plots);
    }

    // 3. Updated CSV Writer: Ensures clean formatting
    private void saveToCsv(Map<String, Object> plot) {
        try (FileWriter fw = new FileWriter("crop_database.csv", true); 
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(plot.get("plotId") + "," + 
                       plot.get("cropName") + "," + 
                       plot.get("landSize") + "," + 
                       plot.get("plantingDate"));
        } catch (IOException e) { e.printStackTrace(); }
    }
}