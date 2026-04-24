package com.agri.ledger;

import com.agri.model.AnalysisResult;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Part 6 – Decision Ledger
 *
 * DecisionLogger is the persistence layer for the ledger.
 * Every time DecisionService produces an AnalysisResult, the caller should invoke
 * {@link #log(String, AnalysisResult)} to create a permanent, auditable record.
 *
 * Storage:
 *   Entries are stored as a JSON array in {@code ledger.json} in the working directory
 *   (the same directory as {@code .env} and {@code pom.xml}).
 *   Jackson handles all serialization / deserialization.
 *
 * Thread safety:
 *   This class is NOT thread-safe. For a hackathon single-user context this is fine.
 *   For production, wrap the file I/O in a ReentrantLock.
 *
 * Usage (from a controller):
 * <pre>
 *   DecisionLogger logger   = new DecisionLogger();
 *   AnalysisResult result   = decisionService.analyze(profile, market, weather);
 *   String recommendationId = logger.log(profile.getFarmerName(), result);
 *   // Pass recommendationId to UserActionTracker and OutcomeInput later.
 * </pre>
 */
public class DecisionLogger {

    private static final String LEDGER_FILE = "ledger.json";
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final ObjectMapper mapper;
    private final File         ledgerFile;

    public DecisionLogger() {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.ledgerFile = new File(LEDGER_FILE);
    }

    /** Constructor for tests or custom file locations. */
    public DecisionLogger(String filePath) {
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        this.ledgerFile = new File(filePath);
    }

    // ── Public API ────────────────────────────────────────────────────────────────

    /**
     * Creates and persists a new LedgerEntry from the given AnalysisResult.
     * Called immediately after DecisionService.analyze() returns.
     *
     * @param farmerName The farmer's name (from FarmerProfile.getFarmerName()).
     * @param result     The AnalysisResult returned by DecisionService.
     * @return           The generated unique recommendationId (UUID string).
     *                   Store this ID to later call UserActionTracker and OutcomeInput.
     */
    public String log(String farmerName, AnalysisResult result) {

        String id = UUID.randomUUID().toString();

        LedgerEntry entry = new LedgerEntry();
        entry.setRecommendationId(id);
        entry.setTimestamp(LocalDateTime.now().format(ISO));
        entry.setFarmerName(farmerName);
        entry.setRecommendedCrop(result.getRecommendedCrop());
        entry.setRecommendedStrategy(result.getRecommendedStrategy());
        entry.setProjectedEconomicImpact(result.getEconomicImpact());
        entry.setRiskScore(result.getRiskScore());
        entry.setReasoning(result.getReasoning());
        entry.setStrategyBreakdown(result.getStrategyBreakdown());

        try {
            List<LedgerEntry> all = loadAll();
            all.add(entry);
            saveAll(all);
            log("INFO", "Recommendation logged. ID: " + id + " | Farmer: " + farmerName
                    + " | Crop: " + result.getRecommendedCrop());
        } catch (IOException e) {
            log("ERROR", "Failed to persist ledger entry: " + e.getMessage());
            // Non-fatal: return the ID even if disk write fails so the caller
            // can still use it in memory.
        }

        return id;
    }

    /**
     * Loads all existing ledger entries from disk.
     * Returns an empty list if the file does not exist yet.
     *
     * @return Mutable list of all LedgerEntry records.
     * @throws IOException If the file exists but cannot be read or parsed.
     */
    public List<LedgerEntry> loadAll() throws IOException {
        if (!ledgerFile.exists() || ledgerFile.length() == 0) {
            return new ArrayList<>();
        }
        return mapper.readValue(ledgerFile, new TypeReference<List<LedgerEntry>>() {});
    }

    /**
     * Finds a single entry by its recommendationId.
     *
     * @param id The UUID string returned by {@link #log}.
     * @return   The matching LedgerEntry, or null if not found.
     */
    public LedgerEntry findById(String id) {
        if (id == null) return null;
        try {
            for (LedgerEntry entry : loadAll()) {
                if (id.equals(entry.getRecommendationId())) {
                    return entry;
                }
            }
        } catch (IOException e) {
            log("ERROR", "Failed to read ledger for findById: " + e.getMessage());
        }
        return null;
    }

    /**
     * Replaces the stored entry with the same recommendationId.
     * Used by UserActionTracker and OutcomeInput to update existing entries.
     *
     * @param updated The modified LedgerEntry to persist.
     */
    public void updateEntry(LedgerEntry updated) {
        try {
            List<LedgerEntry> all = loadAll();
            boolean found = false;

            for (int i = 0; i < all.size(); i++) {
                if (updated.getRecommendationId().equals(all.get(i).getRecommendationId())) {
                    all.set(i, updated);
                    found = true;
                    break;
                }
            }

            if (!found) {
                log("WARN", "updateEntry: entry not found for ID " + updated.getRecommendationId()
                        + ". Appending as new.");
                all.add(updated);
            }

            saveAll(all);
            log("INFO", "Entry updated. ID: " + updated.getRecommendationId());

        } catch (IOException e) {
            log("ERROR", "Failed to update ledger entry: " + e.getMessage());
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    /** Writes the full list back to ledger.json (overwrite). */
    private void saveAll(List<LedgerEntry> entries) throws IOException {
        mapper.writeValue(ledgerFile, entries);
    }

    private void log(String level, String msg) {
        System.out.println("[" + level + "][DecisionLogger] " + msg);
    }
}
