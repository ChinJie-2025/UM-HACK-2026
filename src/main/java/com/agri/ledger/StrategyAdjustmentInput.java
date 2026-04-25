package com.agri.ledger;

import java.util.Map;

/**
 * Part 6 – Decision Ledger (Extension)
 *
 * StrategyAdjustmentInput allows an operator or farmer to manually override 
 * the numeric data points in the investment strategies after they have been logged.
 * * This is useful if the farmer realizes their actual costs are different from 
 * the AI's defaults, or if they want to "fine-tune" the graph data.
 */
public class StrategyAdjustmentInput {

    private final DecisionLogger decisionLogger;

    public StrategyAdjustmentInput(DecisionLogger decisionLogger) {
        this.decisionLogger = decisionLogger;
    }

    /**
     * Updates the specific strategy breakdown for a recommendation.
     * * @param recommendationId The ID of the existing record.
     * @param strategyName "Conservative", "Balanced", or "Aggressive".
     * @param newDescription The full formatted string (must maintain the pattern for graph extraction).
     * @return true if updated successfully.
     */
    public boolean updateStrategyData(String recommendationId, String strategyName, String newDescription) {
        
        LedgerEntry entry = decisionLogger.findById(recommendationId);
        
        if (entry == null) {
            log("ERROR", "Entry not found: " + recommendationId);
            return false;
        }

        Map<String, String> breakdown = entry.getStrategyBreakdown();
        
        if (!breakdown.containsKey(strategyName)) {
            log("WARN", "Strategy '" + strategyName + "' not found. Adding as new.");
        }

        // Update the map
        breakdown.put(strategyName, newDescription);
        entry.setStrategyBreakdown(breakdown);

        // If the user edited the currently "chosen" plan, we might want to update 
        // the top-level projected impact for the LoopbackService as well.
        if (strategyName.equals(entry.getChosenPlan())) {
            extractAndSetImpact(entry, newDescription);
        }

        decisionLogger.updateEntry(entry);
        log("INFO", "Strategy '" + strategyName + "' updated for ID: " + recommendationId);
        return true;
    }

    /**
     * Helper to sync the main economic impact field if the edited strategy
     * is the one the farmer actually chose.
     */
    // Inside StrategyAdjustmentInput.java
// In StrategyAdjustmentInput.java
private void extractAndSetImpact(LedgerEntry entry, String description) {
    try {
        String marker = "Projected Net Return: RM ";
        int start = description.indexOf(marker);
        if (start != -1) {
            // Find the end of the number by looking for the next "|" or the end of the string
            int end = description.indexOf("|", start + marker.length());
            if (end == -1) end = description.length();
            
            // Strip commas and spaces to ensure Double.parseDouble works
            String value = description.substring(start + marker.length(), end).replace(",", "").trim();
            
            entry.setProjectedEconomicImpact(Double.parseDouble(value));
            // This is the "Sync" that the LoopbackService uses!
        }
    } catch (Exception e) {
        log("ERROR", "Sync failed. Ensure description contains 'Projected Net Return: RM X.XX'");
    }
}

    private void log(String level, String msg) {
        System.out.println("[" + level + "][StrategyAdjustmentInput] " + msg);
    }
}