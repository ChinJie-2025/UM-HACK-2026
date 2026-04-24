package com.agri.ledger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Part 6 – Decision Ledger
 *
 * OutcomeInput is the harvest-time data-entry point.
 * After the farming season ends, the farmer (or an operator on their behalf) uses
 * this class to log what actually happened, closing the feedback loop.
 *
 * Once an outcome is recorded, LoopbackService can compare projected vs actual
 * results and generate an accuracy/impact report for that recommendation.
 *
 * Usage (at harvest time):
 * <pre>
 *   OutcomeInput outcomeInput = new OutcomeInput(new DecisionLogger());
 *   outcomeInput.recordOutcome(
 *       "uuid-from-decision-logger",
 *       "Good yield. Pest damage in week 8 reduced profit slightly.",
 *       14750.00   // actual RM profit
 *   );
 * </pre>
 *
 * Validation:
 *   - Outcome text must not be null or blank.
 *   - actualProfit may be negative (loss is a valid real-world outcome).
 *   - An entry cannot be re-logged once marked outcomeLogged=true without explicit override.
 */
public class OutcomeInput {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DecisionLogger decisionLogger;

    /**
     * @param decisionLogger The shared DecisionLogger instance for reading and updating entries.
     */
    public OutcomeInput(DecisionLogger decisionLogger) {
        this.decisionLogger = decisionLogger;
    }

    // ── Public API ────────────────────────────────────────────────────────────────

    /**
     * Records the actual harvest result for a previously logged recommendation.
     *
     * @param recommendationId The UUID returned by DecisionLogger.log().
     * @param actualOutcome    A text description of what happened during the season.
     *                         Must not be null or blank.
     * @param actualProfit     The actual net return the farmer made in RM.
     *                         May be negative (loss).
     * @return                 true if the outcome was successfully recorded; false otherwise.
     */
    public boolean recordOutcome(String recommendationId, String actualOutcome, double actualProfit) {
        return recordOutcome(recommendationId, actualOutcome, actualProfit, false);
    }

    /**
     * Records or overwrites the actual harvest result.
     *
     * @param forceOverwrite If true, allows re-recording an already-logged outcome.
     *                       Use with caution – this replaces the previous outcome data.
     */
    public boolean recordOutcome(String recommendationId,
                                  String actualOutcome,
                                  double actualProfit,
                                  boolean forceOverwrite) {

        // ── Validate inputs ────────────────────────────────────────────────────────
        if (recommendationId == null || recommendationId.isBlank()) {
            log("ERROR", "Cannot record outcome: recommendationId is null or blank.");
            return false;
        }

        if (actualOutcome == null || actualOutcome.isBlank()) {
            log("ERROR", "Cannot record outcome: actualOutcome text is null or blank.");
            return false;
        }

        // ── Load the existing entry ────────────────────────────────────────────────
        LedgerEntry entry = decisionLogger.findById(recommendationId);

        if (entry == null) {
            log("ERROR", "No ledger entry found for ID: " + recommendationId);
            return false;
        }

        // ── Guard: prevent accidental overwrite ────────────────────────────────────
        if (entry.isOutcomeLogged() && !forceOverwrite) {
            log("WARN", "Outcome already recorded for ID: " + recommendationId
                    + ". Use forceOverwrite=true to replace it.");
            return false;
        }

        if (entry.isOutcomeLogged()) {
            log("WARN", "Overwriting existing outcome for ID: " + recommendationId);
        }

        // ── Record the outcome ─────────────────────────────────────────────────────
        entry.setActualOutcome(actualOutcome);
        entry.setActualProfit(actualProfit);
        entry.setOutcomeTimestamp(LocalDateTime.now().format(ISO));
        entry.setOutcomeLogged(true);

        decisionLogger.updateEntry(entry);

        double projected = entry.getProjectedEconomicImpact();
        double delta     = actualProfit - projected;
        String deltaStr  = (delta >= 0 ? "+" : "") + String.format("RM %.2f", delta);

        log("INFO", "Outcome logged. ID: " + recommendationId
                + " | Farmer: " + entry.getFarmerName()
                + " | Crop: " + entry.getRecommendedCrop()
                + " | Projected: RM " + String.format("%.2f", projected)
                + " | Actual: RM " + String.format("%.2f", actualProfit)
                + " | Delta: " + deltaStr);

        return true;
    }

    /**
     * Prints a full outcome summary to the console for verification.
     *
     * @param recommendationId The UUID of the recommendation to look up.
     */
    public void printOutcomeSummary(String recommendationId) {

        LedgerEntry entry = decisionLogger.findById(recommendationId);

        if (entry == null) {
            System.out.println("[OutcomeInput] No entry found for ID: " + recommendationId);
            return;
        }

        System.out.println("\n=== HARVEST OUTCOME SUMMARY ===");
        System.out.println("Recommendation ID  : " + entry.getRecommendationId());
        System.out.println("Farmer             : " + entry.getFarmerName());
        System.out.println("Crop               : " + entry.getRecommendedCrop());
        System.out.println("Chosen Plan        : " + nullSafe(entry.getChosenPlan(), "Not recorded"));
        System.out.println("Projected Return   : RM " + String.format("%.2f", entry.getProjectedEconomicImpact()));

        if (entry.isOutcomeLogged()) {
            double delta = entry.getActualProfit() - entry.getProjectedEconomicImpact();
            System.out.println("Actual Profit      : RM " + String.format("%.2f", entry.getActualProfit()));
            System.out.println("Variance (Δ)       : " + (delta >= 0 ? "+" : "") + String.format("RM %.2f", delta));
            System.out.println("Outcome Notes      : " + entry.getActualOutcome());
            System.out.println("Logged At          : " + entry.getOutcomeTimestamp());
        } else {
            System.out.println("Actual Profit      : (harvest not yet recorded)");
        }

        System.out.println("================================\n");
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private String nullSafe(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private void log(String level, String msg) {
        System.out.println("[" + level + "][OutcomeInput] " + msg);
    }
}
