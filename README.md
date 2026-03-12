<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/PostgreSQL-16-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" />
  <img src="https://img.shields.io/badge/Tests-4%20Passing-brightgreen?style=for-the-badge&logo=junit5&logoColor=white" />
</p>

<h1 align="center">🧪 DermaData</h1>
<h3 align="center">Cosmetic Ingredient Safety Analyzer — EU Compliance Engine</h3>

<p align="center">
  Analyze personal care product ingredients against <strong>EU Cosmetics Regulation (EC) No 1223/2009</strong><br/>
  and generate a compliance safety score out of 100, complete with hard-cap override, alias deduplication,<br/>
  and order-agnostic combination violation detection.
</p>

---

## 📋 Table of Contents

- [About](#-about)
- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [API Reference](#-api-reference)
- [Safety Scoring Algorithm](#-safety-scoring-algorithm)
- [Scoring Rules — Strategy Pattern](#-scoring-rules--strategy-pattern)
- [Ingredient Deduplication](#-ingredient-deduplication)
- [Database](#-database)
- [Unit Tests](#-unit-tests)
- [Configuration](#-configuration)
- [Future Roadmap](#-future-roadmap)
- [License](#-license)

---

## 🔬 About

**DermaData** is a full-stack web application that analyzes ingredient lists of personal care products — **shampoos, soaps, and conditioners** — by comparing them against the **EU Cosmetics Regulation (EC) No 1223/2009**. It generates a rigorous safety compliance score based on:

- Whether ingredients are **prohibited** under EU law (hard-capped to DANGER regardless of count)
- Whether concentrations **exceed EU maximum limits**
- **Combination interactions** between ingredients that may pose risks (order-agnostic detection)
- Identification of **restricted** substances and **fragrance allergens**
- **Unknown ingredient precautionary penalties** — substances not found in EU CosIng are not treated as safe
- **Intelligent deduplication** — aliased/duplicate ingredients are collapsed to prevent unfair score penalties

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🧴 **Ingredient Extraction** | Paste raw ingredient lists or upload label images for AI-powered extraction |
| 📊 **Safety Score (0–100)** | Quantified compliance score with SAFE / CAUTION / DANGER categories |
| 🚫 **Prohibited Detection + Hard Cap** | Flags EU-banned substances; any prohibited ingredient hard-caps score at ≤40 (DANGER) |
| ⚠️ **Concentration Check** | Compares detected % against EU maximum allowed concentrations |
| 🔗 **Combination Analysis** | Detects dangerous ingredient combinations — order-agnostic (SLS+MIT = MIT+SLS) |
| 🟡 **NOT_REGULATED Penalty** | Unknown ingredients receive −3 pts precautionary penalty + warning message |
| 🔄 **Alias Deduplication** | Collapses "Methylparaben / Paraben methyl / Methylparaben" → 1 result with aliases shown |
| 🌿 **Herbal Name Resolution** | Maps 120+ Indian cosmetic names (Amla, Reetha, Methi, Bhringraj) to INCI standards |
| 📥 **Report Download** | Export compliance reports as `.txt` files |
| 🔍 **Search & Filter** | Search through ingredient breakdown results |
| 🎨 **Modern Dark UI** | Glassmorphism design with emerald/cyan accents on pure black background |

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21 | Core language |
| Spring Boot | 3.4.3 | Application framework |
| Spring Data JPA | — | ORM & database access |
| Hibernate | 6.x | JPA implementation |
| PostgreSQL | 16 | Relational database |
| Flyway | — | Database migrations |
| Jackson | — | JSON serialization |
| Maven | 3.9+ | Build & dependency management |
| JUnit 5 | — | Unit testing |
| Mockito | — | Mocking framework |

### Frontend
| Technology | Version | Purpose |
|-----------|---------|---------|
| React | 19 | UI framework |
| Vite | 6 | Build tool & dev server |
| Tailwind CSS | 4 | Utility-first CSS |
| Recharts | 2 | Circular gauge chart |
| Axios | — | HTTP client |
| React Dropzone | — | Drag & drop file upload |
| Lucide React | — | Icon library |

### Database Seeding
| Technology | Purpose |
|-----------|---------|
| Python 3.10+ | Database seeding script |
| psycopg2-binary | PostgreSQL driver for Python |

---

## 🏗 Architecture

```
┌─────────────────────┐     HTTP/REST      ┌─────────────────────────────────────────┐
│                     │  ◄──────────────►  │                                         │
│   React Frontend    │    Port 5173       │        Spring Boot Backend               │
│   (Vite + Tailwind) │    (proxied)       │             Port 8080                   │
│                     │                    │                                         │
│  SafeResultsDash    │                    │  AnalysisController                     │
│  IngredientAnalyzer │                    │    │                                    │
└─────────────────────┘                    │    ├─ ExtractionService                  │
                                           │    │    └─ deduplicateIngredients()      │
                                           │    ├─ SafetyScoreEngineService           │
                                           │    │    └─ ScoringRuleRegistry           │
                                           │    │         ├─ ProhibitedScoringRule    │
                                           │    │         ├─ ExceededConcentrationRule│
                                           │    │         ├─ NotRegulatedScoringRule  │
                                           │    │         └─ CombinationViolationRule │
                                           │    └─ LlmService (mocked)               │
                                           └──────────────┬──────────────────────────┘
                                                          │ Spring Data JPA
                                                          ▼
                                                 ┌─────────────────────┐
                                                 │     PostgreSQL       │
                                                 │   (dermadata DB)     │
                                                 │                     │
                                                 │  • 103 ingredients  │
                                                 │  • 15 combo rules   │
                                                 └─────────────────────┘
```

### Request Flow

```
User Input (raw text / image)
  │
  ▼
ExtractionService.extractFromText()
  │   • Splits on [,;\n]
  │   • Strips concentrations, ext. abbreviations, Latin suffixes
  │   • INCI_MAP lookup (120+ entries): "aloe vera" → "ALOE BARBADENSIS"
  │   • Falls back to UPPERCASE raw name if not mapped
  ▼
ExtractionService.deduplicateIngredients()
  │   • Groups by inciName.toLowerCase() using LinkedHashMap
  │   • First occurrence wins; later occurrences' rawNames → aliases list
  │   • Returns DeduplicationResult { deduplicated, duplicatesRemoved }
  ▼
SafetyScoreEngineService.analyze()
  │   • score = 100
  │   • For each IngredientInput:
  │       ├─ regulationRepo.findByInciNameIgnoreCase() → PROHIBITED / EXCEEDED / RESTRICTED / SAFE
  │       │   └─ applyScoringRules() via ScoringRuleRegistry → subtract penalty
  │       └─ Not found → NOT_REGULATED, −3 pts precautionary penalty
  │   • Combination check (order-agnostic Set<String> lookup)
  │   • Hard cap: prohibitedCount ≥ 1 → score = min(score, 40)
  │   • scoreCategory = DANGER / CAUTION / SAFE
  ▼
LlmService.getLlmCombinationAnalysis()   (mocked — returns rule-based insights)
  ▼
AnalysisReport (JSON response)
```

---

## 📁 Project Structure

```
SpringBootChemicalProject/
│
├── pom.xml                                       # Maven config (Spring Boot 3.4.3, Java 21)
├── README.md
│
├── scripts/
│   └── seed_database.py                          # DB seeding (103 ingredients, 15 rules)
│
├── src/main/java/com/dermadata/
│   ├── DermaDataApplication.java                 # Spring Boot entry point
│   │
│   ├── config/
│   │   └── WebConfig.java                        # CORS: allows localhost:5173, localhost:3000
│   │
│   ├── controller/
│   │   ├── AnalysisController.java               # POST /api/v1/analyze, /extract
│   │   └── IngredientController.java             # GET  /api/v1/ingredients
│   │
│   ├── dto/
│   │   ├── AnalysisRequest.java                  # { ingredients, rawText, imageBase64, productType }
│   │   ├── AnalysisReport.java                   # Full response with score, flags, duplicatesRemoved
│   │   ├── IngredientInput.java                  # { inciName, rawName, concentration, aliases }
│   │   ├── IngredientResult.java                 # { inciName, status, penalty, aliases, euMaxConc }
│   │   └── CombinationWarning.java               # { ingredientA, ingredientB, description, penalty }
│   │
│   ├── entity/
│   │   ├── IngredientRegulation.java             # JPA: ingredient_regulations table
│   │   └── CombinationRule.java                  # JPA: combination_rules table
│   │
│   ├── repository/
│   │   ├── IngredientRegulationRepository.java   # findByInciNameIgnoreCase()
│   │   └── CombinationRuleRepository.java        # findRulesForIngredients(List<String>)
│   │
│   ├── scoring/                                  # Strategy Pattern — scoring rules
│   │   ├── ScoringRule.java                      # interface: applies(result), getPenalty(result)
│   │   ├── ScoringRuleRegistry.java              # holds List<ScoringRule> + CombinationViolationRule
│   │   ├── ProhibitedScoringRule.java            # −20 pts if status = PROHIBITED
│   │   ├── ExceededConcentrationRule.java        # −10 pts if status = EXCEEDED
│   │   ├── NotRegulatedScoringRule.java          # −3  pts if status = NOT_REGULATED
│   │   └── CombinationViolationRule.java         # −5  pts per combination hit
│   │
│   └── service/
│       ├── ExtractionService.java                # INCI normalization + deduplicateIngredients()
│       ├── SafetyScoreEngineService.java         # Core scoring + hard-cap + combination detection
│       └── LlmService.java                       # AI insights (rule-based mock)
│
├── src/main/resources/
│   ├── application.properties                    # DB config + scoring penalties
│   └── db/migration/
│       └── V1__create_ingredient_table.sql       # Flyway migration
│
├── src/test/java/com/dermadata/service/
│   ├── SafetyScoreEngineServiceTest.java         # 2 tests: hard-cap + combination order
│   └── ExtractionServiceTest.java                # 2 tests: dedup + distinct names
│
└── dermadata-ui/                                 # React frontend
    ├── index.html
    ├── vite.config.js                            # Vite + Tailwind + proxy to :8080
    ├── package.json
    └── src/
        ├── main.jsx
        ├── App.jsx
        ├── index.css                             # Dark glassmorphism design system
        ├── services/
        │   └── api.js                            # Axios API service layer
        └── components/
            ├── IngredientAnalyzer.jsx            # Input page
            └── SafetyResultsDashboard.jsx        # Results page with gauge + badges
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+** (JDK)
- **Maven 3.9+**
- **PostgreSQL 16+**
- **Node.js 18+** & npm
- **Python 3.10+** (for DB seeding only)

### 1. Clone the Repository

```bash
git clone https://github.com/aviratpatil/SpringBootChemicalAnalyzer.git
cd SpringBootChemicalAnalyzer
```

### 2. Create the Database

```sql
-- In psql or pgAdmin:
CREATE DATABASE dermadata;
```

### 3. Configure Database Connection

Edit `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/dermadata
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 4. Seed the Database

```bash
pip install psycopg2-binary
python scripts/seed_database.py
```

Expected output:
```
✓ Connected to PostgreSQL
✓ Inserted/updated 103 ingredient regulations
✓ Inserted 15 combination rules
✓ Database seeded successfully!
```

### 5. Run the Backend

```bash
mvn spring-boot:run
```

Backend starts on **http://localhost:8080**

### 6. Run the Frontend

```bash
cd dermadata-ui
npm install
npm run dev
```

Frontend starts on **http://localhost:5173**

### 7. Run Unit Tests

```bash
mvn test
```

Expected: **4 tests, 0 failures, 0 errors.**

---

## 📡 API Reference

### Analysis Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/v1/analyze` | Full ingredient analysis with safety score |
| `POST` | `/api/v1/extract` | Extract & normalize ingredients from text/image |

### Ingredient Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/ingredients` | List all ingredient regulations |
| `GET` | `/api/v1/ingredients/search?query=` | Search regulations by INCI name |

### System

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/health` | Health check |

---

### Example: Analyze with Prohibited Ingredient

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "rawText": "Aqua, Sodium Laureth Sulfate, Methylparaben, Formaldehyde",
    "productType": "Shampoo"
  }'
```

**Response:**
```json
{
  "safetyScore": 40,
  "scoreCategory": "DANGER",
  "productType": "Shampoo",
  "totalIngredients": 4,
  "flaggedIngredients": 2,
  "prohibitedCount": 1,
  "exceededCount": 0,
  "combinationViolations": 0,
  "notRegulatedCount": 0,
  "duplicatesRemoved": 0,
  "hardCapApplied": true,
  "overrideReason": "Score overridden: 1 EU-prohibited substance detected",
  "ingredientResults": [
    {
      "inciName": "FORMALDEHYDE",
      "status": "PROHIBITED",
      "penaltyPoints": 20,
      "regulationRef": "Annex II/1577",
      "aliases": ["Formaldehyde"]
    },
    {
      "inciName": "METHYLPARABEN",
      "status": "RESTRICTED",
      "penaltyPoints": 0,
      "euMaxConcentration": 0.4,
      "aliases": ["Methylparaben"]
    }
  ],
  "llmInsights": [
    "⚠️ Formaldehyde is a known carcinogen banned under EU Annex II."
  ]
}
```

---

### Example: Deduplicated Aliases

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "rawText": "Methylparaben, Paraben methyl, Methylparaben",
    "productType": "Shampoo"
  }'
```

**Response snippet:**
```json
{
  "duplicatesRemoved": 2,
  "ingredientResults": [
    {
      "inciName": "METHYLPARABEN",
      "aliases": ["Methylparaben", "Paraben methyl", "Methylparaben"],
      "status": "RESTRICTED",
      "penaltyPoints": 0
    }
  ]
}
```

---

## 🧮 Safety Scoring Algorithm

The scoring engine starts at **100 points** and applies deductions via the **Strategy Pattern**:

| Rule Class | Violation Type | Penalty | Configurable via |
|---|---|---|---|
| `ProhibitedScoringRule` | Prohibited Ingredient (EU Annex II) | **−20 pts** | `scoring.penalty.prohibited` |
| `ExceededConcentrationRule` | Concentration exceeds EU max limit | **−10 pts** | `scoring.penalty.exceeded` |
| `CombinationViolationRule` | Dangerous ingredient combination | **−5 pts** | `scoring.penalty.combination` |
| `NotRegulatedScoringRule` | Not found in EU CosIng database | **−3 pts** | `scoring.penalty.not-regulated` |

### 🚨 Hard Cap Override

> **If `prohibitedCount ≥ 1`, the final score is hard-capped at `40` — regardless of arithmetic.**

This ensures a product containing a single EU-banned carcinogen (e.g., Formaldehyde) **cannot** score above 40 or be classified as SAFE/CAUTION. The response includes:

```json
"hardCapApplied": true,
"overrideReason": "Score overridden: 1 EU-prohibited substance detected"
```

The frontend renders a red banner: `⛔ AUTOMATIC DANGER — Prohibited substance detected`.

### Score Categories

| Score Range | Category | Meaning |
|------------|----------|---------| 
| 71 – 100 | 🟢 **SAFE** | Product meets EU compliance standards |
| 41 – 70 | 🟡 **CAUTION** | Some ingredients flagged; review recommended |
| 0 – 40 | 🔴 **DANGER** | Fails compliance; prohibited substances or multiple violations |

### NOT_REGULATED Precautionary Penalty

Ingredients **not found** in the EU CosIng database are not treated as safe. They receive:
- Status: `NOT_REGULATED`
- Penalty: **−3 pts**
- Warning in `llmInsights`: `"X ingredient(s) not found in EU CosIng database — safety unknown."`
- Frontend badge: gray/yellow ⚠️ icon

### Ingredient Status Classification

| Status | Meaning |
|--------|---------|
| `PROHIBITED` | Banned under EU Annex II (e.g., Formaldehyde, Lilial, Hydroquinone) |
| `EXCEEDED` | Concentration detected > EU max limit (e.g., SLS > 1%) |
| `RESTRICTED` | Has conditions/limits but within bounds (e.g., Methylparaben ≤ 0.4%) |
| `SAFE` | Regulated and within all limits |
| `NOT_REGULATED` | Not found in EU CosIng database — safety unknown |

---

## 🏗 Scoring Rules — Strategy Pattern

All scoring logic follows the **Strategy (Open/Closed) design pattern**. Adding a new penalty type requires only implementing one interface:

```java
public interface ScoringRule {
    boolean applies(IngredientResult result);
    int getPenalty(IngredientResult result);
}
```

**Existing implementations:**

```
scoring/
  ├── ProhibitedScoringRule.java       @Value("${scoring.penalty.prohibited:20}")
  ├── ExceededConcentrationRule.java   @Value("${scoring.penalty.exceeded:10}")
  ├── CombinationViolationRule.java    @Value("${scoring.penalty.combination:5}")
  └── NotRegulatedScoringRule.java     @Value("${scoring.penalty.not-regulated:3}")
```

All rules are registered via `ScoringRuleRegistry` injected into `SafetyScoreEngineService`.

---

## 🔄 Ingredient Deduplication

`ExtractionService.deduplicateIngredients(List<IngredientInput>)` prevents the same substance from being penalized multiple times:

**Algorithm:**
1. Iterates inputs in encounter order using a `LinkedHashMap<String, IngredientInput>`.
2. **First occurrence** of an INCI name → canonical entry; its `rawName` seeds the `aliases` list.
3. **Subsequent duplicates** → their `rawName` is appended to the canonical entry's aliases; entry is dropped.
4. Returns `DeduplicationResult { List<IngredientInput> deduplicated, int duplicatesRemoved }`.

**Example:**

| Input | INCI Resolved | Result |
|---|---|---|
| "Methylparaben" | METHYLPARABEN | ✅ Kept (canonical) |
| "Paraben methyl" | METHYLPARABEN | ❌ Merged — alias added |
| "Methylparaben" | METHYLPARABEN | ❌ Merged — alias added |

`duplicatesRemoved = 2` · `aliases = ["Methylparaben", "Paraben methyl", "Methylparaben"]`

---

## ⚙️ Order-Agnostic Combination Detection

Combination rules stored in PostgreSQL as `ingredient_a / ingredient_b` pairs are matched using a **`Set<String>`** of all normalized ingredient names:

```java
Set<String> nameSet = inputs.stream()
    .map(i -> i.getInciName().trim().toLowerCase())
    .collect(Collectors.toSet());

// Both "SLS+MIT" and "MIT+SLS" produce the same set → same detection result
boolean violated = nameSet.contains(ruleA) && nameSet.contains(ruleB);
```

This eliminates false negatives caused by ingredient list ordering.

---

## 🗄 Database

### Ingredient Regulations (`ingredient_regulations`) — 103 entries

| Category | Examples | Count |
|----------|----------|-------|
| Surfactants | SLS, SLES, Cocamidopropyl Betaine, Coco Glucoside | 10 |
| Preservatives | Methylparaben, MIT, MCI, Phenoxyethanol, Sodium Benzoate | 13 |
| **Prohibited** | **Formaldehyde, Hydroquinone, Lilial, Mercury, Coal Tar** | **9** |
| Actives | Salicylic Acid, Zinc Pyrithione, Ketoconazole, Lactic Acid | 10 |
| Silicones | Dimethicone, Dimethiconol, Amodimethicone | 4 |
| Herbal Extracts | Amla (Emblica), Hibiscus, Reetha, Methi, Henna, Bhringraj, Aloe | 9 |
| Fragrance Allergens | Limonene, Linalool, Hexyl Cinnamal, Benzyl Salicylate | 4 |
| Colorants | CI 19140, CI 16035, Quinazarine Green SS | 6 |
| Emollients | Glycerin, Panthenol, Glycol Distearate, Shea Oil/Butter | 12 |
| pH / Chelators | Citric Acid, NaOH, EDTA, Sodium Citrate | 8+ |
| Polymers | Polyquaternium-7/10, Guar HPC, Carbomer | 6 |

### Combination Rules (`combination_rules`) — 15 entries

| Combination | Risk |
|-------------|------|
| Methylparaben + Propylparaben | Total parabens > 0.8% |
| SLS + Methylisothiazolinone | Enhanced sensitization |
| MCI + MIT | Must be 3:1 ratio, max 0.0015% |
| Salicylic Acid + Glycolic Acid | Dual acid pH risk |
| Formaldehyde + DMDM Hydantoin | Double formaldehyde exposure |
| Retinol + Salicylic Acid | Increased irritation |
| Lilial + Linalool | Double allergen exposure |

---

## 🧪 Unit Tests

**4 tests — all passing ✅**

| Test Class | Test Method | What it proves |
|---|---|---|
| `SafetyScoreEngineServiceTest` | `whenFormaldehydeIsPresent_thenHardCapOf40IsApplied` | Any FORMALDEHYDE input → score ≤ 40, category = DANGER, `hardCapApplied = true` |
| `SafetyScoreEngineServiceTest` | `whenCombinationIsPresentInAnyOrder_thenPenaltyIsApplied` | "SLS + MIT" and "MIT + SLS" both produce `combinationViolations = 1` |
| `ExtractionServiceTest` | `whenThreeInputsResolveToSameInci_thenOnlyOneResultIsReturned` | 3 inputs → 1 result, `duplicatesRemoved = 2`, aliases contains all 3 raw names |
| `ExtractionServiceTest` | `whenInputsHaveDistinctInciNames_thenNothingIsRemoved` | 2 distinct inputs → 2 results, `duplicatesRemoved = 0` |

---

## ⚙️ Configuration

All scoring penalties are externalized in `application.properties` — **no code changes needed**:

```properties
# Scoring Engine — Penalty values (configurable without recompile)
scoring.penalty.prohibited=20
scoring.penalty.exceeded=10
scoring.penalty.combination=5
scoring.penalty.not-regulated=3

# CORS
app.cors.allowed-origins=http://localhost:5173,http://localhost:3000
```

---

## 🗺 Future Roadmap

- [ ] **Real AI Integration** — Replace mock ExtractionService with Gemini/Claude API for true OCR
- [ ] **Redis Caching** — Cache analysis reports for repeat queries
- [ ] **FastAPI Microservice** — Separate Python microservice for AI extraction
- [ ] **pgvector Fuzzy Matching** — Fuzzy INCI name matching for misspelled ingredients
- [ ] **PDF Report Export** — Generate professional PDF compliance reports
- [ ] **User Authentication** — Save analysis history per user (Spring Security + JWT)
- [ ] **Product Database** — Pre-loaded popular product formulations
- [ ] **Batch Analysis** — Analyze multiple products simultaneously
- [ ] **Mobile Responsive** — Optimize for mobile camera scanning

---

## 📄 License

This project is for **educational and informational purposes** only. It is not a substitute for professional regulatory advice. Always consult a qualified regulatory expert for commercial product compliance.

---

<p align="center">
  Built with ❤️ using Spring Boot 3.4.3 + React 19 + PostgreSQL 16
</p>
