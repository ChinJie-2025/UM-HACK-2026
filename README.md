Documentation: 
1. [QUALITY ASSURANCE TESTING DOCUMENTATION (QATD) _168.pdf](https://github.com/user-attachments/files/27085029/QUALITY.ASSURANCE.TESTING.DOCUMENTATION.QATD._168.pdf)
2. [SYSTEM ANALYSIS DOCUMENTATION (SAD)_168.pdf](https://github.com/user-attachments/files/27085028/SYSTEM.ANALYSIS.DOCUMENTATION.SAD._168.pdf)
3. [PRODUCT REQUIREMENT DOCUMENT (PRD)_168.pdf](https://github.com/user-attachments/files/27085026/PRODUCT.REQUIREMENT.DOCUMENT.PRD._168.pdf)

Pitch Deck:
Pitch Video:


# 🌾 AgriWise - AI-Powered Agricultural Investment Risk Analyst

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)

> An intelligent decision support system that helps farmers make data-driven investment decisions by analyzing crop selection, market conditions, weather patterns, and financial risks.

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)
- [Core Components](#-core-components)
- [Decision Ledger System](#-decision-ledger-system)
- [Configuration](#-configuration)
- [Usage Examples](#-usage-examples)
- [Contributing](#-contributing)
- [Team](#-team)

---

## 🎯 Overview

AgriWise is an **AI-powered agricultural investment decision system** designed for **UM HACK 2026**. It combines machine learning, real-time market data, and weather intelligence to provide farmers with:

- **Multi-strategy investment recommendations** (Conservative, Balanced, Aggressive)
- **Risk assessment** and scoring (1-10 scale)
- **Economic impact projections** with quantifiable metrics
- **Decision tracking and outcome analysis** through a comprehensive ledger system
- **Interactive chat interface** for personalized agricultural advice

The system uses **GLM (General Language Model) API** to generate intelligent crop recommendations based on:
- Farmer profile (location, land size, budget, risk tolerance)
- Current market conditions and crop prices
- Weather forecasts and historical patterns
- Resource requirements (water, fertilizer, labor)

---

## ✨ Features

### 🤖 AI-Powered Decision Engine
- **Intelligent Crop Recommendations**: GLM-based analysis considering multiple factors
- **Multi-Strategy Generation**: Provides Conservative, Balanced, and Aggressive investment plans
- **Risk Scoring**: Quantitative risk assessment (1-10 scale) for each recommendation
- **Economic Impact Projection**: Estimated profit/revenue for each strategy

### 📊 Data Management
- **Farmer Profiles**: Store location, land size, budget, and risk tolerance
- **Crop Plot Tracking**: Manage multiple plots with crop-land mapping
- **Market Data Integration**: Real-time crop prices and yield information
- **Weather Context**: Weather-aware recommendations

### 📝 Decision Ledger & Feedback Loop
- **Decision Logging**: Track every AI recommendation with unique IDs
- **User Action Tracking**: Record which strategy farmers actually chose
- **Outcome Recording**: Capture harvest results (actual yield and profit)
- **Performance Analysis**: Compare predicted vs. actual results
- **Accuracy Metrics**: Calculate economic accuracy and AI performance

### 💬 Interactive Features
- **AI Chat Interface**: Natural language interaction for crop advice
- **Scenario Simulation**: Test "what-if" scenarios before making decisions
- **Visual Dashboard**: Monitor all plots and recommendations in one place

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Frontend (index.html)                    │
│          Tailwind CSS + Lucide Icons + Vanilla JS            │
└───────────────────────┬─────────────────────────────────────┘
                        │ REST API Calls
                        ▼
┌─────────────────────────────────────────────────────────────┐
│              Spring Boot Application Layer                   │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│  │   Analysis   │  │     Chat     │  │  Simulation  │      │
│  │  Controller  │  │  Controller  │  │  Controller  │      │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘      │
│         │                  │                  │              │
│         └──────────────────┼──────────────────┘              │
│                            ▼                                 │
│  ┌─────────────────────────────────────────────────────┐    │
│  │          Decision Service (Core Engine)             │    │
│  │  • PromptBuilder                                    │    │
│  │  • GlmClient (API Integration)                      │    │
│  │  • ZaiRationaleGenerator (Response Parser)          │    │
│  │  • MultiStrategyGenerator                           │    │
│  └─────────────────────────────────────────────────────┘    │
│                            │                                 │
│                            ▼                                 │
│  ┌─────────────────────────────────────────────────────┐    │
│  │         Decision Ledger System                      │    │
│  │  • DecisionLogger (Persistence)                     │    │
│  │  • UserActionTracker (Choice Recording)             │    │
│  │  • OutcomeInput (Harvest Results)                   │    │
│  │  • LoopbackService (Performance Analysis)           │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│              Data Layer & External Services                  │
│  • crop_database.csv (Plot Persistence)                     │
│  • ledger.json (Decision History)                           │
│  • GLM API (AI/ML Service)                                  │
│  • Market Data APIs (Future: Real-time prices)              │
│  • Weather APIs (Future: Forecasts)                         │
└─────────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

### Backend
- **Framework**: Spring Boot 3.2.5
- **Language**: Java 17
- **Build Tool**: Maven
- **Environment Management**: dotenv-java 3.0.0
- **JSON Processing**: Jackson (built-in)
- **API Integration**: RestTemplate (Spring Web)

### Frontend
- **HTML5** + **CSS3** + **Vanilla JavaScript**
- **Styling**: Tailwind CSS (CDN)
- **Icons**: Lucide Icons
- **Fonts**: Google Fonts (Inter)

### AI/ML
- **GLM API**: General Language Model for crop recommendations
- **Prompt Engineering**: Custom-built structured prompts
- **JSON Response Parsing**: Deterministic output format

### Data Storage
- **CSV**: Plot data persistence (`crop_database.csv`)
- **JSON**: Decision ledger storage (`ledger.json`)

---

## 📁 Project Structure

```
UM-HACK-2026/
├── src/
│   └── main/
│       ├── java/com/agri/
│       │   ├── AgriwiseApplication.java      # Main Spring Boot application
│       │   ├── Controller/
│       │   │   ├── AnalysisController.java   # Analysis API endpoints
│       │   │   └── ChatController.java       # Chat API endpoints
│       │   ├── DTO/
│       │   │   └── AnalysisResponseDTO.java  # Data Transfer Objects
│       │   ├── model/
│       │   │   ├── User.java                 # User entity
│       │   │   ├── FarmerProfile.java        # Farmer profile data
│       │   │   ├── CropPlot.java             # Plot management
│       │   │   ├── CropData.java             # Crop market data
│       │   │   ├── AnalysisResult.java       # AI response model
│       │   │   └── SimulationRequest.java    # Simulation input
│       │   ├── engine/                       # Decision Core (Part 4)
│       │   │   ├── DecisionService.java      # Main orchestrator
│       │   │   ├── GlmClient.java            # GLM API client
│       │   │   ├── PromptBuilder.java        # Prompt assembly
│       │   │   ├── ZaiRationaleGenerator.java # Response parser
│       │   │   ├── MultiStrategyGenerator.java # Strategy creation
│       │   │   └── README.md                 # Engine documentation
│       │   ├── ledger/                       # Decision Ledger (Part 6)
│       │   │   ├── DecisionLogger.java       # Persistence layer
│       │   │   ├── LedgerEntry.java          # Entry model
│       │   │   ├── UserActionTracker.java    # Choice tracking
│       │   │   ├── OutcomeInput.java         # Harvest data entry
│       │   │   └── LoopbackService.java      # Performance analysis
│       │   ├── sandbox/                      # Simulation (Part 7)
│       │   │   ├── SimulationController.java # Scenario API
│       │   │   └── ScenarioSolver.java       # What-if engine
│       │   ├── auth/
│       │   │   ├── AuthController.java       # Authentication
│       │   │   └── ProfileEditor.java        # Profile management
│       │   ├── data/                         # External Data (Part 3)
│       │   │   ├── MarketDataClient.java     # Market API client
│       │   │   └── WeatherNewsClient.java    # Weather API client
│       │   ├── config/
│       │   │   └── AppConfig.java            # Environment config
│       │   └── resources/
│       │       ├── AgriWise_Crop_Dataset.csv # Sample crop data
│       │       └── field_intelligence.txt    # Agricultural knowledge
│       └── resources/
│           └── static/
│               └── index.html                # Frontend UI
├── pom.xml                                   # Maven dependencies
├── .env                                      # Environment variables (API keys)
├── .gitignore                                # Git ignore rules
├── crop_database.csv                         # Plot data storage
└── README.md                                 # This file
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher
- **Maven 3.6+**
- **GLM API Key** (obtain from GLM platform)
- **Git** (for version control)

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/UM-HACK-2026.git
   cd UM-HACK-2026
   ```

2. **Configure environment variables**
   
   Create a `.env` file in the project root:
   ```env
   API_KEY=your_glm_api_key_here
   ```

3. **Install dependencies**
   ```bash
   mvn clean install
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   
   Open your browser and navigate to:
   ```
   http://localhost:8080
   ```

---

## 📡 API Documentation

### Crop Management

#### Register New Plot
```http
POST /api/crops/register
Content-Type: application/json

{
  "plotId": "PLOT001",
  "cropName": "Durian",
  "landSize": "2.5",
  "plantingDate": "2024-04-25"
}

Response:
{
  "expectedYield": "8.5 MT",
  "waterReq": "Moderate (Optimized)"
}
```

#### Get All Plots
```http
GET /api/crops/all

Response:
[
  {
    "plotId": "PLOT001",
    "cropName": "Durian",
    "landSize": "2.5",
    "plantingDate": "2024-04-25",
    "expectedYield": "Synced",
    "waterReq": "Optimized"
  }
]
```

### AI Chat

#### Send Chat Message
```http
POST /api/crops/chat
Content-Type: application/json

{
  "message": "Should I use the aggressive plan for my corn?"
}

Response:
{
  "reply": "<b>Aggressive Plan:</b> Increase NPK by 15%."
}
```

### Analysis (In Progress)

#### Get Latest Analysis
```http
GET /api/analysis/latest

Response:
[
  {
    "recommendedCrop": "Corn",
    "reasoning": "High market demand",
    "riskScore": 0.2,
    "economicImpact": "RM 5,000",
    "strategyType": "Conservative"
  }
]
```

---

## 🔧 Core Components

### 1. Data Models (`model/`)

#### **User**
Stores user account information and credentials.

#### **FarmerProfile**
```java
- location: String          // GPS coordinates or region
- landSize: double          // Hectares
- budget: double            // Available investment (RM)
- riskTolerance: String     // "LOW", "MEDIUM", "HIGH"
```

#### **CropPlot**
Represents a single plot (the "Box"):
```java
- plotId: String            // Unique identifier
- cropName: String          // Crop type
- landSize: double          // Plot area
- plantingDate: LocalDate   // When planted
```

#### **CropData**
Market information for crops:
```java
- name: String              // Crop name
- yield: double             // Expected yield (MT/ha)
- marketPrice: double       // Current price (RM/kg)
- waterReq: String          // Water requirements
- fertilizerReq: String     // Fertilizer needs
```

#### **AnalysisResult**
GLM output structure:
```java
- recommendedCrop: String   // Best crop suggestion
- reasoning: String         // Explanation (required for transparency)
- riskScore: double         // 1-10 scale (required for risk assessment)
- economicImpact: String    // Projected profit (required for quantifiable impact)
```

---

### 2. Decision Engine (`engine/`)

#### **DecisionService**
Main orchestrator that:
1. Receives farmer profile and context
2. Calls PromptBuilder to create structured prompt
3. Sends request to GlmClient
4. Parses response with ZaiRationaleGenerator
5. Returns AnalysisResult

#### **PromptBuilder**
Constructs deterministic prompts forcing JSON output:
```
Context: Farmer in Kuala Lumpur with 5 hectares, RM 50,000 budget, HIGH risk tolerance
Market: Durian at RM 12/kg, Corn at RM 3/kg, Rice at RM 2.5/kg
Weather: Monsoon season approaching

STRICT OUTPUT FORMAT (JSON ONLY):
{
  "recommendedCrop": "...",
  "reasoning": "...",
  "riskScore": 1-10,
  "economicImpact": "RM X,XXX"
}
```

#### **GlmClient**
Handles HTTP communication with GLM API:
- Sends POST request with prompt
- Manages authentication (API key)
- Returns raw text response

#### **ZaiRationaleGenerator**
Parses GLM's text response:
- Strips markdown fences (```json)
- Validates JSON structure
- Clamps riskScore to 1-10 range
- Populates AnalysisResult object

#### **MultiStrategyGenerator**
Generates three investment strategies:
- **Conservative**: Low risk, stable returns
- **Balanced**: Moderate risk/reward
- **Aggressive**: High risk, high potential returns

---

### 3. Decision Ledger System (`ledger/`)

The **Decision Ledger** is a 3-stage lifecycle tracking system:

#### **Stage 1: Recommendation Generation** (DecisionLogger)
When the AI produces a recommendation:
```java
DecisionLogger logger = new DecisionLogger();
String ledgerId = logger.logDecision(analysisResult, farmerProfile);
// Creates new LedgerEntry with unique UUID
```

#### **Stage 2: User Choice** (UserActionTracker)
When farmer selects a strategy:
```java
UserActionTracker tracker = new UserActionTracker(logger);
tracker.recordChoice(ledgerId, "Balanced");
// Updates LedgerEntry.chosenPlan and timestamp
```

#### **Stage 3: Harvest Outcome** (OutcomeInput)
After harvest:
```java
OutcomeInput outcome = new OutcomeInput(logger);
outcome.recordOutcome(ledgerId, actualYield, actualProfit);
// Updates LedgerEntry.actualYield and actualProfit
```

#### **Performance Analysis** (LoopbackService)
Compare predicted vs. actual:
```java
LoopbackService loopback = new LoopbackService(logger);

// Single entry analysis
String report = loopback.generateReport(ledgerId);

// Aggregate metrics across all decisions
String summary = loopback.generateSummaryReport();
```

**Metrics Calculated:**
- **Economic Accuracy (%)** = (actualProfit / projectedProfit) × 100
  - 100% = perfect prediction
  - \>100% = AI underestimated (good surprise!)
  - <100% = AI overestimated

---

### 4. Simulation Sandbox (`sandbox/`)

#### **SimulationController**
API endpoint for "what-if" scenarios:
```http
POST /api/simulation/run
{
  "farmerProfile": {...},
  "scenarios": [
    { "crop": "Durian", "landSize": 3 },
    { "crop": "Corn", "landSize": 5 }
  ]
}
```

#### **ScenarioSolver**
Runs multiple scenarios in parallel and compares outcomes.

---

### 5. External Data Integration (`data/`)

#### **MarketDataClient** (Future Enhancement)
- Real-time crop prices
- Supply/demand trends
- Commodity futures

#### **WeatherNewsClient** (Future Enhancement)
- Weather forecasts
- Climate patterns
- Agricultural alerts

---

## ⚙️ Configuration

### Environment Variables (.env)

```env
# GLM API Configuration
API_KEY=your_glm_api_key_here

# Future: Market Data API
# MARKET_DATA_API_KEY=xxx

# Future: Weather API
# WEATHER_API_KEY=xxx
```

### Application Properties

Located in `.env` file, managed by `AppConfig.java`:
```java
public class AppConfig {
    public static String getGlmApiKey() {
        // Loads from .env file
        return dotenv.get("API_KEY");
    }
}
```

**Security Note**: Never commit `.env` to Git! It's listed in `.gitignore`.

---

## 💡 Usage Examples

### Example 1: Getting a Crop Recommendation

```java
// 1. Create farmer profile
FarmerProfile profile = new FarmerProfile();
profile.setLocation("Kuala Lumpur, Malaysia");
profile.setLandSize(5.0);
profile.setBudget(50000.0);
profile.setRiskTolerance("HIGH");

// 2. Get market data
List<CropData> marketData = getMarketData(); // From database

// 3. Get weather context
String weather = "Monsoon season, high rainfall expected";

// 4. Call decision service
DecisionService service = new DecisionService();
AnalysisResult result = service.analyze(profile, marketData, weather);

// 5. Log the decision
DecisionLogger logger = new DecisionLogger();
String ledgerId = logger.logDecision(result, profile);

System.out.println("Recommended: " + result.getRecommendedCrop());
System.out.println("Reasoning: " + result.getReasoning());
System.out.println("Risk Score: " + result.getRiskScore() + "/10");
System.out.println("Expected Impact: " + result.getEconomicImpact());
```

### Example 2: Recording Farmer's Choice

```java
// After farmer selects "Balanced" plan from UI
UserActionTracker tracker = new UserActionTracker(logger);
tracker.recordChoice(ledgerId, "Balanced");
```

### Example 3: Recording Harvest Results

```java
// After harvest season
OutcomeInput outcome = new OutcomeInput(logger);
outcome.recordOutcome(
    ledgerId,
    8.5,      // Actual yield (MT)
    45000.0   // Actual profit (RM)
);
```

### Example 4: Analyzing AI Performance

```java
LoopbackService loopback = new LoopbackService(logger);

// Get detailed report for one decision
String detailedReport = loopback.generateReport(ledgerId);
System.out.println(detailedReport);

// Get aggregate performance across all decisions
String summary = loopback.generateSummaryReport();
System.out.println(summary);
```

---

## 🎨 Frontend Features

The web interface (`index.html`) provides:

### Dashboard View
- **Plot Management**: View all registered plots
- **Quick Actions**: Register new plots
- **Status Overview**: Monitor crop health and progress

### AI Chat Interface
- **Natural Language**: Ask questions in plain language
- **Real-time Responses**: Instant AI-powered advice
- **Context-Aware**: Remembers plot details

### Analysis Panel
- **Multi-Strategy Display**: See Conservative/Balanced/Aggressive options
- **Risk Visualization**: Color-coded risk indicators
- **Economic Projections**: Profit estimates

---

## 🔮 Future Enhancements

### Phase 1 (Current Hackathon)
- ✅ Core decision engine
- ✅ Multi-strategy generation
- ✅ Decision ledger system
- ✅ Basic UI
- 🔄 Complete GLM integration
- 🔄 Full simulation sandbox

### Phase 2 (Post-Hackathon)
- 🔄 Real-time market data integration
- 🔄 Weather API integration
- 🔄 Mobile app (React Native)
- 🔄 User authentication & authorization
- 🔄 Database migration (PostgreSQL)
- 🔄 Advanced analytics dashboard

### Phase 3 (Production)
- 🔄 Multi-language support (Malay, Chinese, Tamil)
- 🔄 IoT sensor integration (soil moisture, pH)
- 🔄 Blockchain for supply chain tracking
- 🔄 Community marketplace
- 🔄 Insurance integration

---

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Coding Standards
- Follow Java naming conventions
- Use meaningful variable/method names
- Add JavaDoc comments for public methods
- Write unit tests for new features

---

## 👥 Team

**UM HACK 2026 Team**

1. Chin Jie
2. Ivory Liong Jin Earn
3. Tan Winny
4. Lin Zhi Heng

---

## 🙏 Acknowledgments

- **UM HACK 2026** organizers for the opportunity
- **GLM Platform** for AI/ML capabilities
- **Spring Boot** community for excellent documentation
- **Tailwind CSS** for beautiful UI components
- **Malaysian farmers** who inspired this project

---

## 📞 Support

For questions or support:
- 📞 Phone number : +60 10-967 3678

---
