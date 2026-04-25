package com.agri.sandbox;

import com.agri.model.AnalysisResult;
import com.agri.model.SimulationRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * SimulationRunner: The "UI" layer for SimulationControls.
 * This class captures the "What-If" inputs from the user and 
 * triggers the SimulationController logic.
 */
public class SimulationRunner {

    private final SimulationController simulationController;

    public SimulationRunner(SimulationController controller) {
        this.simulationController = controller;
    }

    /**
     * Simulates the UI action of moving sliders or changing dropdowns.
     */
    public void startInteractiveSession() {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== AGRI-DECISION SANDBOX CONTROL PANEL ===");
        
        // 1. Capture Variable Modifiers (Mimicking UI Sliders)
        Map<String, Double> modifiers = new HashMap<>();
        
        System.out.print("Enter Fertilizer Cost Modifier (e.g., 1.2 for +20%, 1.0 for no change): ");
        modifiers.put("fertilizerCost", scanner.nextDouble());
        
        System.out.print("Enter Labor Availability Modifier (e.g., 0.8 for -20%): ");
        modifiers.put("laborAvailability", scanner.nextDouble());
        
        scanner.nextLine(); // Consume newline

        // 2. Capture Environmental Context (Mimicking a Dropdown)
        System.out.print("Enter Environmental Scenario (e.g., 'Severe Drought', 'Flash Floods'): ");
        String envContext = scanner.nextLine();

        // 3. Package into SimulationRequest (Parses Inputs)
        SimulationRequest request = new SimulationRequest();
        request.setModifiers(modifiers);
        request.setEnvironmentalContext(envContext);

        try {
            // 4. Delegate Work & Format Response
            // This triggers the side-by-side Comparative View in the console
            AnalysisResult result = simulationController.handleSimulationRequest(request);
            
            System.out.println("Simulation Complete. Results mapped to Comparative View.");
            
        } catch (Exception e) {
            System.err.println("Error during simulation: " + e.getMessage());
        }
    }

    /**
     * Automated "What-If" trigger for quick testing.
     */
    public void runQuickScenario(double fertMod, double laborMod, String weather) {
        SimulationRequest request = new SimulationRequest();
        Map<String, Double> mods = new HashMap<>();
        mods.put("fertilizerCost", fertMod);
        mods.put("laborAvailability", laborMod);
        
        request.setModifiers(mods);
        request.setEnvironmentalContext(weather);

        try {
            simulationController.handleSimulationRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}