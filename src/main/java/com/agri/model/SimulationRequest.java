package com.agri.model;

import java.util.Map;

public class SimulationRequest {
    // Maps variable names (e.g., "fertilizerCost") to their modified values (e.g., 1.20 for +20%)
    private Map<String, Double> modifiers;
    private String environmentalContext; // e.g., "Drought", "Heavy Rain"

    public SimulationRequest() {}

    public Map<String, Double> getModifiers() { return modifiers; }
    public void setModifiers(Map<String, Double> modifiers) { this.modifiers = modifiers; }

    public String getEnvironmentalContext() { return environmentalContext; }
    public void setEnvironmentalContext(String context) { this.environmentalContext = context; }
}
