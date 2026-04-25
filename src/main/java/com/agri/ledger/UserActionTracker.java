package com.agri.ledger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * Part 6 – Decision Ledger
 *
 * UserActionTracker records which investment strategy the farmer actually chose
 * after receiving the AI recommendation. This is the bridge between the AI's output
 * and real-world farmer behaviour, enabling LoopbackService to measure decision quality.
 *
 * Valid plan values (matching keys produced by MultiStrategyGenerator):
 *   "Conservative", "Balanced", "Aggressive"
 *
 * Usage (after the farmer confirms their choice via UI):
 * <pre>
 *   UserActionTracker tracker = new UserActionTracker(new DecisionLogger());
 *   tracker.recordChoice("uuid-from-decision-logger", "Balanced");
 * </pre>
 *
 * Integration note:
 *   The recommendationId comes from DecisionLogger.log().
 *   Store it in session state (or pass it back to the UI) immediately after
 *   DecisionService.analyze() returns so it's available when the farmer chooses a plan.
 */
@Component
public class UserActionTracker {

    private static final Set<String> VALID_PLANS = Set.of("Conservative", "Balanced", "Aggressive");
    private static final DateTimeFormatter ISO    = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final DecisionLogger decisionLogger;

    /**
     * @param decisionLogger The shared DecisionLogger instance for reading and updating entries.
     */
    public UserActionTracker(DecisionLogger decisionLogger) {
        this.decisionLogger = decisionLogger;
    }

    // ── Public API ────────────────────────────────────────────────────────────────

    /**
     * Records the farmer's chosen investment strategy against a logged recommendation.
     *
     * @param recommendationId The UUID returned by DecisionLogger.log().
     * @param chosenPlan       Must be "Conservative", "Balanced", or "Aggressive".
     * @return                 true if the choice was successfully recorded; false otherwise.
     */
    public boolean recordChoice(String recommendationId, String chosenPlan) {

        // ── Validate inputs ────────────────────────────────────────────────────────
        if (recommendationId == null || recommendationId.isBlank()) {
            log("ERROR", "Cannot record choice: recommendationId is null or blank.");
            return false;
        }

        if (chosenPlan == null || !VALID_PLANS.contains(chosenPlan)) {
            log("ERROR", "Invalid plan '" + chosenPlan + "'. Must be one of: " + VALID_PLANS);
            return false;
        }

        // ── Load the existing entry ────────────────────────────────────────────────
        LedgerEntry entry = decisionLogger.findById(recommendationId);

        if (entry == null) {
            log("ERROR", "No ledger entry found for ID: " + recommendationId);
            return false;
        }

        // ── Guard: warn if overwriting an existing choice ─────────────────────────
        if (entry.getChosenPlan() != null) {
            log("WARN", "Overwriting previous choice '" + entry.getChosenPlan()
                    + "' with '" + chosenPlan + "' for ID: " + recommendationId);
        }

        // ── Record the choice ──────────────────────────────────────────────────────
        entry.setChosenPlan(chosenPlan);
        entry.setChoiceTimestamp(LocalDateTime.now().format(ISO));

        decisionLogger.updateEntry(entry);

        log("INFO", "Choice recorded: " + chosenPlan
                + " | Farmer: " + entry.getFarmerName()
                + " | Crop: " + entry.getRecommendedCrop()
                + " | ID: " + recommendationId);

        return true;
    }

    /**
     * Prints a summary of the farmer's choice for confirmation (e.g., in a chat UI).
     *
     * @param recommendationId The UUID of the recommendation to look up.
     */
    public void printChoiceSummary(String recommendationId) {

        LedgerEntry entry = decisionLogger.findById(recommendationId);

        if (entry == null) {
            System.out.println("[UserActionTracker] No entry found for ID: " + recommendationId);
            return;
        }

        System.out.println("\n=== FARMER DECISION RECORD ===");
        System.out.println("Recommendation ID : " + entry.getRecommendationId());
        System.out.println("Farmer            : " + entry.getFarmerName());
        System.out.println("Recommended Crop  : " + entry.getRecommendedCrop());
        System.out.println("AI Suggested Plan : " + nullSafe(entry.getRecommendedStrategy()));
        System.out.println("Farmer Chose      : " + nullSafe(entry.getChosenPlan(), "Not yet chosen"));
        System.out.println("Choice Recorded At: " + nullSafe(entry.getChoiceTimestamp(), "—"));
        System.out.println("Outcome Logged    : " + (entry.isOutcomeLogged() ? "Yes" : "No (pending harvest)"));
        System.out.println("==============================\n");
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private String nullSafe(String value) {
        return value != null ? value : "—";
    }

    private String nullSafe(String value, String fallback) {
        return value != null ? value : fallback;
    }

    private void log(String level, String msg) {
        System.out.println("[" + level + "][UserActionTracker] " + msg);
    }
}
