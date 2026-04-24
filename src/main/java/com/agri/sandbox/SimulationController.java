package com.agri.sandbox;

import com.agri.model.AnalysisResult;
import com.agri.model.CropData;
import com.agri.model.FarmerProfile;
import com.agri.model.SimulationRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Part 5 – Decision Sandbox: SimulationController + Comparative View
 *
 * SimulationController is the entry point for all "What-If" simulations.
 * It:
 *   1. Accepts a SimulationRequest (variable modifiers + environmental context).
 *   2. Delegates the heavy computation to ScenarioSolver.
 *   3. Renders a full Comparative View via displayComparativeView().
 *
 * ── Comparative View ─────────────────────────────────────────────────────────────────
 * displayComparativeView() shows TWO result sets side-by-side in the console:
 *
 *   • LEFT  column  → Original baseline (the real recommendation)
 *   • RIGHT column  → Simulated scenario (the what-if result)
 *
 * Within each column, all three strategies (Conservative / Balanced / Aggressive)
 * are shown with their key economic metrics extracted from strategyBreakdown.
 * A delta row shows the profit/loss difference between original and simulated.
 *
 * ── Constructor Notes ─────────────────────────────────────────────────────────────────
 * Two constructors are provided:
 *
 *   • 4-arg  (backward-compatible): originalResult defaults to null; comparison
 *     column shows "No baseline available" message if null.
 *   • 5-arg  (preferred): pass originalResult explicitly for a full comparison.
 *
 * ── Integration with Part 6 (Ledger) ─────────────────────────────────────────────────
 * handleSimulationRequest() now returns AnalysisResult (previously void).
 * Callers can pass this result to DecisionLogger for persistence.
 */
public class SimulationController {

    // ── Column widths for the side-by-side display ───────────────────────────────
    private static final int COL_WIDTH  = 44;
    private static final int LABEL_WIDTH = 22;
    private static final String SEP = " │ ";

    // ── Dependencies ─────────────────────────────────────────────────────────────
    private final ScenarioSolver   solver;
    private final FarmerProfile    currentSessionProfile;
    private final List<CropData>   marketData;
    private final String           weatherContext;

    /**
     * The real baseline result (from DecisionService, before any simulation).
     * May be null if the caller uses the 4-arg constructor.
     */
    private AnalysisResult originalResult;

    // ── Constructors ─────────────────────────────────────────────────────────────

    /**
     * Backward-compatible constructor (originalResult = null).
     * The comparative view will still render the simulated side fully, but
     * the baseline column will display "No baseline available".
     */
    public SimulationController(ScenarioSolver solver,
                                 FarmerProfile profile,
                                 List<CropData> market,
                                 String weather) {
        this(solver, profile, market, weather, null);
    }

    /**
     * Full constructor. Pass the original AnalysisResult from DecisionService
     * to enable a proper baseline-vs-simulation comparison.
     *
     * @param originalResult The real recommendation before any what-if modifiers.
     *                       Obtain this from: decisionService.analyze(profile, market, weather)
     */
    public SimulationController(ScenarioSolver solver,
                                 FarmerProfile profile,
                                 List<CropData> market,
                                 String weather,
                                 AnalysisResult originalResult) {
        this.solver                = solver;
        this.currentSessionProfile = profile;
        this.marketData            = market;
        this.weatherContext        = weather;
        this.originalResult        = originalResult;
    }

    /** Allows setting or updating the baseline after construction. */
    public void setOriginalResult(AnalysisResult originalResult) {
        this.originalResult = originalResult;
    }

    // ── Public API ────────────────────────────────────────────────────────────────

    /**
     * Handles a "What-If" simulation request end-to-end.
     *
     * Flow:
     *   1. Merge base weather with the simulation's environmental overlay.
     *   2. Delegate to ScenarioSolver (which deep-copies the profile and re-runs the engine).
     *   3. Render the full Comparative View.
     *   4. Return the hypothetical AnalysisResult (for ledger integration in Part 6).
     *
     * @param request SimulationRequest containing modifiers and environmental context.
     * @return        The hypothetical AnalysisResult (can be passed to DecisionLogger).
     * @throws IOException If the underlying GLM API call fails.
     */
    public AnalysisResult handleSimulationRequest(SimulationRequest request) throws IOException {

        System.out.println("\n[SimulationController] Starting simulation session...");

        // 1. Merge real weather with the simulated environmental overlay
        String simulatedWeather = weatherContext;
        if (request.getEnvironmentalContext() != null && !request.getEnvironmentalContext().isBlank()) {
            simulatedWeather = weatherContext + "\n| SIMULATED OVERLAY: " + request.getEnvironmentalContext();
        }

        // 2. Run the what-if scenario through ScenarioSolver
        AnalysisResult hypothetical = solver.solve(
                currentSessionProfile,
                marketData,
                simulatedWeather,
                request.getModifiers()
        );

        // 3. Render the full Comparative View
        displayComparativeView(originalResult, hypothetical);

        // 4. Return for ledger / UI use
        return hypothetical;
    }

    /**
     * Comparative View – renders a full side-by-side table of:
     *   • Summary metrics  (crop, risk, economic impact)
     *   • All 3 strategies (Conservative / Balanced / Aggressive) for each scenario
     *   • Delta row        (simulated return − original return)
     *
     * If {@code original} is null, the left column shows "No baseline available."
     *
     * @param original    The real/baseline AnalysisResult. May be null.
     * @param hypothetical The what-if AnalysisResult from ScenarioSolver.
     */
    public void displayComparativeView(AnalysisResult original, AnalysisResult hypothetical) {

        int totalWidth = LABEL_WIDTH + COL_WIDTH + SEP.length() + COL_WIDTH;
        String divider = "─".repeat(totalWidth);
        String thick   = "═".repeat(totalWidth);

        System.out.println("\n" + thick);
        System.out.println(centre("COMPARATIVE VIEW — WHAT-IF SIMULATION RESULT", totalWidth));
        System.out.println(thick);

        // ── Header row ───────────────────────────────────────────────────────────
        System.out.println(
            padRight("", LABEL_WIDTH) + SEP +
            centre("ORIGINAL (Baseline)", COL_WIDTH) + SEP +
            centre("SIMULATED (What-If)", COL_WIDTH)
        );
        System.out.println(divider);

        // ── Summary metrics ───────────────────────────────────────────────────────
        String origCrop   = original != null ? original.getRecommendedCrop()                         : "—";
        String origRisk   = original != null ? original.getRiskScore() + "/10"                       : "—";
        String origImpact = original != null ? String.format("RM %.2f", original.getEconomicImpact()): "—";

        String simCrop    = hypothetical.getRecommendedCrop();
        String simRisk    = hypothetical.getRiskScore() + "/10";
        String simImpact  = String.format("RM %.2f", hypothetical.getEconomicImpact());

        printRow("Recommended Crop",  origCrop,   simCrop);
        printRow("Risk Score",        origRisk,   simRisk);
        printRow("Projected Return",  origImpact, simImpact);
        System.out.println(divider);

        // ── Delta row ─────────────────────────────────────────────────────────────
        if (original != null) {
            double delta = hypothetical.getEconomicImpact() - original.getEconomicImpact();
            String deltaStr = (delta >= 0 ? "+" : "") + String.format("RM %.2f", delta);
            String deltaLabel = delta >= 0 ? "▲ GAIN vs Baseline" : "▼ LOSS vs Baseline";
            printRow(deltaLabel, "—", deltaStr);
            System.out.println(divider);
        }

        // ── Strategy breakdown ────────────────────────────────────────────────────
        System.out.println(centre("── STRATEGY BREAKDOWN ──", totalWidth));
        System.out.println(divider);

        String[] strategyKeys = {"Conservative", "Balanced", "Aggressive"};

        for (String key : strategyKeys) {
            // Extract metrics from the pipe-delimited strategy string
            StrategyMetrics origMetrics = original != null
                    ? parseStrategy(original.getStrategyBreakdown(), key)
                    : StrategyMetrics.empty();
            StrategyMetrics simMetrics  = parseStrategy(hypothetical.getStrategyBreakdown(), key);

            System.out.println(padRight("── " + key, LABEL_WIDTH) +
                               SEP + centre("", COL_WIDTH) +
                               SEP + centre("", COL_WIDTH));

            printRow("  Budget Deployed",  origMetrics.budgetDeployed,  simMetrics.budgetDeployed);
            printRow("  Capital Reserved", origMetrics.capitalReserved, simMetrics.capitalReserved);
            printRow("  Est. Volume",      origMetrics.estVolume,       simMetrics.estVolume);
            printRow("  Net Return",       origMetrics.netReturn,       simMetrics.netReturn);
            printRow("  Risk Score",       origMetrics.riskScore,       simMetrics.riskScore);

            System.out.println(divider);
        }

        // ── AI Rationale comparison ───────────────────────────────────────────────
        System.out.println(centre("── AI RATIONALE ──", totalWidth));
        System.out.println(divider);

        List<String> origLines = original != null
                ? wrapText(original.getReasoning(),  COL_WIDTH)
                : List.of("No baseline available.");
        List<String> simLines  = wrapText(hypothetical.getReasoning(), COL_WIDTH);

        int maxLines = Math.max(origLines.size(), simLines.size());
        for (int i = 0; i < maxLines; i++) {
            String o = i < origLines.size() ? origLines.get(i) : "";
            String s = i < simLines.size()  ? simLines.get(i)  : "";
            System.out.println(padRight("", LABEL_WIDTH) + SEP + padRight(o, COL_WIDTH) + SEP + padRight(s, COL_WIDTH));
        }

        System.out.println(thick + "\n");
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    /**
     * Parses the pipe-delimited strategy string produced by MultiStrategyGenerator
     * into a StrategyMetrics record for clean display.
     *
     * Expected format (from MultiStrategyGenerator.buildPlan):
     *   [CONSERVATIVE STRATEGY] Crop: X | Budget Deployed: RM Y (Z%) |
     *   Capital Reserved: RM A | Est. Volume: B tonnes |
     *   Projected Net Return: RM C | Adjusted Risk Score: D/10 | Rationale: ...
     */
    private StrategyMetrics parseStrategy(Map<String, String> breakdown, String key) {
        if (breakdown == null || !breakdown.containsKey(key)) {
            return StrategyMetrics.empty();
        }

        String raw = breakdown.get(key);
        String[] parts = raw.split("\\|");

        StrategyMetrics m = new StrategyMetrics();

        for (String part : parts) {
            String p = part.trim();

            if (p.contains("Budget Deployed:")) {
                // "Budget Deployed: RM 5000.00 (50%)"
                m.budgetDeployed = extractAfterColon(p);
            } else if (p.contains("Capital Reserved:")) {
                m.capitalReserved = extractAfterColon(p);
            } else if (p.contains("Est. Volume:")) {
                m.estVolume = extractAfterColon(p);
            } else if (p.contains("Projected Net Return:")) {
                m.netReturn = extractAfterColon(p);
            } else if (p.contains("Adjusted Risk Score:")) {
                m.riskScore = extractAfterColon(p);
            }
        }

        return m;
    }

    private String extractAfterColon(String segment) {
        int idx = segment.indexOf(':');
        return idx >= 0 ? segment.substring(idx + 1).trim() : segment.trim();
    }

    private void printRow(String label, String left, String right) {
        System.out.println(
            padRight(label, LABEL_WIDTH) + SEP +
            padRight(truncate(left,  COL_WIDTH), COL_WIDTH) + SEP +
            padRight(truncate(right, COL_WIDTH), COL_WIDTH)
        );
    }

    private String padRight(String s, int width) {
        if (s == null) s = "";
        return String.format("%-" + width + "s", s.length() > width ? s.substring(0, width) : s);
    }

    private String centre(String s, int width) {
        if (s == null) s = "";
        if (s.length() >= width) return s.substring(0, width);
        int pad = (width - s.length()) / 2;
        return " ".repeat(pad) + s + " ".repeat(width - s.length() - pad);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 3) + "..." : s;
    }

    /** Word-wraps {@code text} to {@code maxWidth} characters per line. */
    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("—");
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            if (current.length() + word.length() + 1 > maxWidth) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                if (current.length() > 0) current.append(' ');
                current.append(word);
            }
        }
        if (current.length() > 0) lines.add(current.toString());
        return lines;
    }

    // ── Inner record for parsed strategy metrics ──────────────────────────────────
    private static class StrategyMetrics {
        String budgetDeployed  = "—";
        String capitalReserved = "—";
        String estVolume       = "—";
        String netReturn       = "—";
        String riskScore       = "—";

        static StrategyMetrics empty() {
            return new StrategyMetrics();
        }
    }
}