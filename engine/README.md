# UM-HACK-2026 — Part 4: Decision Core (engine)

## Files in this deliverable

| File | Role |
|---|---|
| `PromptBuilder.java` | Combines FarmerProfile + market data + weather into a GLM prompt |
| `GlmClient.java` | HTTP POST wrapper for the Z.AI GLM-4 API |
| `ZaiRationaleGenerator.java` | Parses raw GLM JSON output → AnalysisResult (4 core fields) |
| `MultiStrategyGenerator.java` | Derives Conservative / Balanced / Aggressive plans from the base result |
| `DecisionService.java` | Single public entry point — orchestrates the full pipeline |

---

## How to integrate

Place all five `.java` files into your `engine/` source folder (alongside `model/` and `data/`).

---

## Required patches to earlier zips (DO BEFORE COMPILING)

### 1 — `model/AnalysisResult.java` (zip 1)

`strategyBreakdown` is declared in the model but has **no setter or getter**.
Add these two methods so `DecisionService` can attach the strategy map:

```java
public void setStrategyBreakdown(Map<String, String> strategyBreakdown) {
    this.strategyBreakdown = strategyBreakdown;
}

public Map<String, String> getStrategyBreakdown() {
    return strategyBreakdown;
}
```

Also add the missing import at the top of `AnalysisResult.java` if not already there:
```java
import java.util.Map;
```
*(The field already exists — this just exposes it.)*

---

### 2 — `model/CropData.java` (zip 1)

`MarketDataClient` (zip 2) calls `new CropData(name, price, yield, water)` but `CropData` only
has a no-arg constructor. Add:

```java
public CropData(String name, double marketPrice, double expectedYield, double waterReq) {
    this.name          = name;
    this.marketPrice   = marketPrice;
    this.expectedYield = expectedYield;
    this.waterReq      = waterReq;
}
```

---

## Pipeline flow

```
FarmerProfile ──┐
List<CropData> ─┼──► PromptBuilder ──► GlmClient ──► ZaiRationaleGenerator ──► AnalysisResult
weatherContext ─┘                                                                      │
                                                                MultiStrategyGenerator ─┘
                                                                  (attaches strategyBreakdown)
```

---

## Calling DecisionService from your controllers (Part 7)

```java
// Part 8 supplies the key — never hardcode it
String apiKey = AppConfig.getGlmApiKey();
DecisionService service = new DecisionService(apiKey);

// Part 3 supplies the data
List<CropData> market  = new MarketDataClient().fetchCurrentMarketPrices();
String         weather = new WeatherNewsClient().fetchUnstructuredContext(profile.getLocation());

// Run the engine
AnalysisResult result = service.analyze(profile, market, weather);

// Access results
result.getRecommendedCrop()        // e.g. "Chili"
result.getReasoning()              // full AI explanation
result.getRiskScore()              // 1–10
result.getEconomicImpact()         // projected RM return
result.getStrategyBreakdown()      // Map: "Conservative" / "Balanced" / "Aggressive"
```

---

## Dependency

Jackson Databind is required (already referenced in Part 8).
Make sure your `pom.xml` / `build.gradle` includes:

```xml
<!-- Maven -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.15.2</version>
</dependency>
```
