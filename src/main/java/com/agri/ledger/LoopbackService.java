package com.agri.ledger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Part 6 – Decision Ledger
 *
 * LoopbackService closes the AI feedback loop by comparing what the GLM predicted
 * against what the farmer actually experienced at harvest time.
 *
 * It provides two views:
 *
 *   1. {@link #generateReport(String)}        – Single entry: detailed predicted vs actual breakdown.
 *   2. {@link #generateSummaryReport()}        – All completed entries: aggregate accuracy metrics.
 *
 * Metrics computed:
 *   • Economic Accuracy (%) = (actualProfit / projectedProfit) × 100
 *     100% = perfect prediction, >100% = AI underestimated, <100% = AI overestimated.
 *   • Average variance (RM) across all completed entries.
 *   • Overestimation / Underestimation count.
 *   • Mean risk score of chosen plans vs mean actual profit.
 *
 * Only LedgerEntry records with {@code outcomeLogged = true} are included in
 * summary metrics. Entries without an outcome are reported separately as pending.
 *
 * Usage:
 * <pre>
 *   LoopbackService loopback = new LoopbackService(new DecisionLogger());
 *   loopback.generateReport("uuid-of-specific-entry");
 *   loopback.generateSummaryReport();
 * </pre>
 */
public class LoopbackService {

    private final DecisionLogger decisionLogger;

    /**
     * @param decisionLogger The shared DecisionLogger instance for reading all entries.
     */
    public LoopbackService(DecisionLogger decisionLogger) {
        this.decisionLogger = decisionLogger;
    }

    // ── Public API ────────────────────────────────────────────────────────────────

    /**
     * Generates a detailed report comparing predicted vs actual for a single recommendation.
     *
     * @param recommendationId The UUID of the entry to report on.
     */
    public void generateReport(String recommendationId) {

        LedgerEntry entry = decisionLogger.findById(recommendationId);

        if (entry == null) {
            System.out.println("[LoopbackService] No entry found for ID: " + recommendationId);
            return;
        }

        String divider = "─".repeat(60);
        System.out.println("\n" + "═".repeat(60));
        System.out.println("  LOOPBACK ACCURACY REPORT");
        System.out.println("═".repeat(60));
        System.out.println("Recommendation ID  : " + entry.getRecommendationId());
        System.out.println("Farmer             : " + entry.getFarmerName());
        System.out.println("Generated At       : " + entry.getTimestamp());
        System.out.println(divider);

        System.out.println("Recommended Crop   : " + entry.getRecommendedCrop());
        System.out.println("AI Risk Score      : " + entry.getRiskScore() + "/10");
        System.out.println("AI Reasoning       : " + truncate(entry.getReasoning(), 120));
        System.out.println(divider);

        System.out.println("AI Suggested Plan  : " + nullSafe(entry.getRecommendedStrategy()));
        System.out.println("Farmer Chose       : " + nullSafe(entry.getChosenPlan(), "Not recorded"));
        System.out.println("Divergence         : " + planDivergence(entry));
        System.out.println(divider);

        System.out.println("Projected Return   : RM " + String.format("%.2f", entry.getProjectedEconomicImpact()));

        if (!entry.isOutcomeLogged()) {
            System.out.println("Actual Return      : (harvest not yet recorded — outcome pending)");
            System.out.println("═".repeat(60) + "\n");
            return;
        }

        double projected = entry.getProjectedEconomicImpact();
        double actual    = entry.getActualProfit();
        double delta     = actual - projected;
        double accuracy  = (projected != 0) ? (actual / projected) * 100.0 : 0.0;

        System.out.println("Actual Return      : RM " + String.format("%.2f", actual));
        System.out.println("Variance (Δ)       : " + formatDelta(delta));
        System.out.printf("Economic Accuracy  : %.1f%%%n", accuracy);
        System.out.println("Assessment         : " + assessAccuracy(accuracy));
        System.out.println(divider);

        System.out.println("Farmer's Notes     : " + nullSafe(entry.getActualOutcome()));
        System.out.println("Outcome Logged At  : " + nullSafe(entry.getOutcomeTimestamp()));
        System.out.println("═".repeat(60) + "\n");
    }

    /**
     * Generates an aggregate summary report across ALL completed ledger entries.
     * Entries without outcome data are listed separately as pending.
     */
    public void generateSummaryReport() {

        List<LedgerEntry> all;
        try {
            all = decisionLogger.loadAll();
        } catch (IOException e) {
            System.out.println("[LoopbackService] ERROR: Could not read ledger: " + e.getMessage());
            return;
        }

        if (all.isEmpty()) {
            System.out.println("[LoopbackService] Ledger is empty — no entries to report.");
            return;
        }

        List<LedgerEntry> completed = new ArrayList<>();
        List<LedgerEntry> pending   = new ArrayList<>();

        for (LedgerEntry entry : all) {
            if (entry.isOutcomeLogged()) {
                completed.add(entry);
            } else {
                pending.add(entry);
            }
        }

        String thick   = "═".repeat(70);
        String divider = "─".repeat(70);

        System.out.println("\n" + thick);
        System.out.println("  DECISION LEDGER — AGGREGATE LOOPBACK SUMMARY");
        System.out.printf("  Total Entries: %d  |  Completed: %d  |  Pending: %d%n",
                all.size(), completed.size(), pending.size());
        System.out.println(thick);

        if (!completed.isEmpty()) {

            // ── Aggregate metrics ──────────────────────────────────────────────────
            double totalProjected     = 0;
            double totalActual        = 0;
            double totalVariance      = 0;
            int    overestimatedCount = 0;
            int    underestimatedCount= 0;
            int    perfectCount       = 0;   // within ±5%

            for (LedgerEntry e : completed) {
                totalProjected += e.getProjectedEconomicImpact();
                totalActual    += e.getActualProfit();
                double variance = e.getActualProfit() - e.getProjectedEconomicImpact();
                totalVariance  += variance;

                double acc = (e.getProjectedEconomicImpact() != 0)
                        ? (e.getActualProfit() / e.getProjectedEconomicImpact()) * 100.0 : 0;

                if      (acc >= 95 && acc <= 105) perfectCount++;
                else if (acc < 95)                overestimatedCount++;
                else                              underestimatedCount++;
            }

            double avgAccuracy = (totalProjected != 0)
                    ? (totalActual / totalProjected) * 100.0 : 0;
            double avgVariance = totalVariance / completed.size();

            System.out.println("COMPLETED ENTRIES SUMMARY:");
            System.out.println(divider);
            System.out.printf("  Total Projected Return  : RM %.2f%n", totalProjected);
            System.out.printf("  Total Actual Return     : RM %.2f%n", totalActual);
            System.out.printf("  Total Variance (Δ)      : %s%n", formatDelta(totalVariance));
            System.out.printf("  Average Variance/Entry  : %s%n", formatDelta(avgVariance));
            System.out.printf("  Overall Economic Accuracy: %.1f%%%n", avgAccuracy);
            System.out.println(divider);
            System.out.printf("  AI Overestimated (>5%%)  : %d entries%n", overestimatedCount);
            System.out.printf("  AI Underestimated (<5%%): %d entries%n", underestimatedCount);
            System.out.printf("  AI Near-Perfect (±5%%)  : %d entries%n", perfectCount);
            System.out.println(divider);

            // ── Per-entry table ────────────────────────────────────────────────────
            System.out.printf("  %-8s %-12s %-12s %-10s %-10s %-8s%n",
                    "ID(short)", "Farmer", "Crop", "Projected", "Actual", "Δ");
            System.out.println(divider);

            for (LedgerEntry e : completed) {
                double delta = e.getActualProfit() - e.getProjectedEconomicImpact();
                System.out.printf("  %-8s %-12s %-12s RM%-8.0f RM%-8.0f %s%n",
                        shortId(e.getRecommendationId()),
                        truncate(e.getFarmerName(), 12),
                        truncate(e.getRecommendedCrop(), 12),
                        e.getProjectedEconomicImpact(),
                        e.getActualProfit(),
                        formatDelta(delta));
            }
            System.out.println(divider);
        }

        // ── Pending entries ────────────────────────────────────────────────────────
        if (!pending.isEmpty()) {
            System.out.println("\nPENDING ENTRIES (harvest not yet recorded):");
            System.out.println(divider);
            for (LedgerEntry e : pending) {
                System.out.printf("  [%s] Farmer: %-12s Crop: %-12s Plan Chosen: %s%n",
                        shortId(e.getRecommendationId()),
                        truncate(e.getFarmerName(), 12),
                        truncate(e.getRecommendedCrop(), 12),
                        nullSafe(e.getChosenPlan(), "Not yet chosen"));
            }
            System.out.println(divider);
        }

        System.out.println(thick + "\n");
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private String planDivergence(LedgerEntry entry) {
        String ai     = entry.getRecommendedStrategy();
        String chosen = entry.getChosenPlan();

        if (chosen == null)  return "No plan recorded";
        if (ai == null)      return "Farmer chose: " + chosen + " (AI suggestion unavailable)";
        if (ai.equals(chosen)) return "None — farmer followed AI suggestion (" + chosen + ")";
        return "AI suggested " + ai + ", farmer chose " + chosen;
    }

    /**
     * Maps accuracy percentage to a human-readable assessment.
     * <75%: significant overestimate | 75–90%: mild overestimate |
     * 90–110%: on target | 110–125%: mild underestimate | >125%: significant underestimate
     */
    private String assessAccuracy(double accuracy) {
        if      (accuracy < 50)  return "⚠ Large overestimate — projected significantly exceeded actual.";
        else if (accuracy < 75)  return "↓ Moderate overestimate — AI was overly optimistic.";
        else if (accuracy < 90)  return "↙ Slight overestimate — close, but AI leaned optimistic.";
        else if (accuracy <= 110) return "✓ On target — AI prediction closely matched reality.";
        else if (accuracy <= 125) return "↗ Slight underestimate — actual exceeded projection.";
        else                     return "↑ Significant underestimate — farmer outperformed the forecast.";
    }

    private String formatDelta(double delta) {
        return (delta >= 0 ? "+" : "") + String.format("RM %.2f", delta);
    }

    private String shortId(String uuid) {
        if (uuid == null) return "—";
        return uuid.length() > 8 ? uuid.substring(0, 8) : uuid;
    }

    private String truncate(String s, int max) {
        if (s == null) return "—";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }

    private String nullSafe(String value) {
        return value != null ? value : "—";
    }

    private String nullSafe(String value, String fallback) {
        return value != null ? value : fallback;
    }
}
