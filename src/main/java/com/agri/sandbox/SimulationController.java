package com.agri.sandbox;

import com.agri.model.AnalysisResult;
import com.agri.model.CropData;
import com.agri.model.FarmerProfile;
import com.agri.model.SimulationRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Part 5 – Decision Sandbox: SimulationController + Comparative View
 * Corrected for Spring Boot Autowiring.
 */
@Component
public class SimulationController {

    // ── Column widths for the side-by-side display ───────────────────────────────
    private static final int COL_WIDTH  = 44;
    private static final int LABEL_WIDTH = 22;
    private static final String SEP = " │ ";

    // ── Dependencies ─────────────────────────────────────────────────────────────
    // Only the Solver is a Spring Service (Bean)
    private final ScenarioSolver solver;
    
    // These are data objects that will be set via setSessionData()
    private FarmerProfile currentSessionProfile;
    private List<CropData> marketData;
    private String weatherContext;
    private AnalysisResult originalResult;

    // ── Constructors ─────────────────────────────────────────────────────────────

    /**
     * Primary Spring Constructor.
     * Spring only needs the ScenarioSolver bean to create this controller.
     */
    @Autowired
    public SimulationController(ScenarioSolver solver) {
        this.solver = solver;
    }

    /**
     * Data Initialization Method.
     * Call this from your AgriwiseApplication to "plug in" the real-world data.
     */
    public void setSessionData(FarmerProfile profile, List<CropData> market, String weather) {
        this.currentSessionProfile = profile;
        this.marketData = market;
        this.weatherContext = weather;
    }

    /** Allows setting or updating the baseline after construction. */
    public void setOriginalResult(AnalysisResult originalResult) {
        this.originalResult = originalResult;
    }

    // ── Public API ────────────────────────────────────────────────────────────────

    public AnalysisResult handleSimulationRequest(SimulationRequest request) throws IOException {
        // Safety check to ensure data was loaded
        // 1. Safety Check: If session data is null, initialize with default values
    if (this.currentSessionProfile == null) {
        System.out.println("[SimulationController] Data not initialized. Using default test profile.");
        this.currentSessionProfile = new FarmerProfile(); 
        this.marketData = new ArrayList<>();
        this.weatherContext = "Normal";
    }

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

        // 3. Render the full Comparative View to Console
        displayComparativeView(originalResult, hypothetical);

        return hypothetical;
    }

    /**
     * Comparative View – renders a full side-by-side table.
     */
    public void displayComparativeView(AnalysisResult original, AnalysisResult hypothetical) {
        int totalWidth = LABEL_WIDTH + COL_WIDTH + SEP.length() + COL_WIDTH;
        String divider = "─".repeat(totalWidth);
        String thick   = "═".repeat(totalWidth);

        System.out.println("\n" + thick);
        System.out.println(centre("COMPARATIVE VIEW — WHAT-IF SIMULATION RESULT", totalWidth));
        System.out.println(thick);

        System.out.println(
            padRight("", LABEL_WIDTH) + SEP +
            centre("ORIGINAL (Baseline)", COL_WIDTH) + SEP +
            centre("SIMULATED (What-If)", COL_WIDTH)
        );
        System.out.println(divider);

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

        if (original != null) {
            double delta = hypothetical.getEconomicImpact() - original.getEconomicImpact();
            String deltaStr = (delta >= 0 ? "+" : "") + String.format("RM %.2f", delta);
            String deltaLabel = delta >= 0 ? "▲ GAIN vs Baseline" : "▼ LOSS vs Baseline";
            printRow(deltaLabel, "—", deltaStr);
            System.out.println(divider);
        }

        System.out.println(centre("── STRATEGY BREAKDOWN ──", totalWidth));
        System.out.println(divider);

        String[] strategyKeys = {"Conservative", "Balanced", "Aggressive"};
        for (String key : strategyKeys) {
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

        System.out.println(centre("── AI RATIONALE ──", totalWidth));
        System.out.println(divider);

        List<String> origLines = original != null ? wrapText(original.getReasoning(),  COL_WIDTH) : List.of("No baseline available.");
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

    private StrategyMetrics parseStrategy(Map<String, String> breakdown, String key) {
        if (breakdown == null || !breakdown.containsKey(key)) return StrategyMetrics.empty();
        String raw = breakdown.get(key);
        String[] parts = raw.split("\\|");
        StrategyMetrics m = new StrategyMetrics();
        for (String part : parts) {
            String p = part.trim();
            if (p.contains("Budget Deployed:")) m.budgetDeployed = extractAfterColon(p);
            else if (p.contains("Capital Reserved:")) m.capitalReserved = extractAfterColon(p);
            else if (p.contains("Est. Volume:")) m.estVolume = extractAfterColon(p);
            else if (p.contains("Projected Net Return:")) m.netReturn = extractAfterColon(p);
            else if (p.contains("Adjusted Risk Score:")) m.riskScore = extractAfterColon(p);
        }
        return m;
    }

    private String extractAfterColon(String segment) {
        int idx = segment.indexOf(':');
        return idx >= 0 ? segment.substring(idx + 1).trim() : segment.trim();
    }

    private void printRow(String label, String left, String right) {
        System.out.println(padRight(label, LABEL_WIDTH) + SEP + padRight(truncate(left, COL_WIDTH), COL_WIDTH) + SEP + padRight(truncate(right, COL_WIDTH), COL_WIDTH));
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

    private List<String> wrapText(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) { lines.add("—"); return lines; }
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

    private static class StrategyMetrics {
        String budgetDeployed = "—", capitalReserved = "—", estVolume = "—", netReturn = "—", riskScore = "—";
        static StrategyMetrics empty() { return new StrategyMetrics(); }
    }
}