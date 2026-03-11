<p align="center">
  <img src="https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
  <img src="https://img.shields.io/badge/Spring_Boot-3.4.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" />
  <img src="https://img.shields.io/badge/PostgreSQL-17-336791?style=for-the-badge&logo=postgresql&logoColor=white" />
  <img src="https://img.shields.io/badge/Tailwind_CSS-4-06B6D4?style=for-the-badge&logo=tailwindcss&logoColor=white" />
</p>

<h1 align="center">🧪 DermaData</h1>
<h3 align="center">Cosmetic Ingredient Safety Analyzer</h3>

<p align="center">
  Analyze personal care product ingredients against <strong>EU Cosmetics Regulation (EC) No 1223/2009</strong><br/>
  and generate a compliance safety score out of 100.
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
- [Database](#-database)
- [Screenshots](#-screenshots)
- [Future Roadmap](#-future-roadmap)
- [License](#-license)

---

## 🔬 About

**DermaData** is a full-stack web application that analyzes the ingredient lists of personal care products — **shampoos, soaps, and conditioners** — by comparing them against the **EU Cosmetics Regulation (EC) No 1223/2009**. It generates a safety compliance score based on:

- Whether ingredients are **prohibited** under EU law
- Whether concentrations **exceed EU maximum limits**
- **Combination interactions** between ingredients that may pose risks
- Identification of **restricted** substances and **fragrance allergens**

The application supports both **image upload** (OCR-based extraction) and **text input** (comma-separated ingredient lists), making it easy to scan any product label.

---

## ✨ Features

| Feature | Description |
|---------|-------------|
| 🧴 **Ingredient Extraction** | Paste raw ingredient lists or upload label images for AI-powered extraction |
| 📊 **Safety Score (0–100)** | Quantified compliance score with SAFE / CAUTION / DANGER categories |
| 🚫 **Prohibited Detection** | Flags EU-banned substances (Formaldehyde, Hydroquinone, Lilial, etc.) |
| ⚠️ **Concentration Check** | Compares detected % against EU maximum allowed concentrations |
| 🔗 **Combination Analysis** | Detects dangerous ingredient combinations (e.g., MCI + MIT, SLS + MIT) |
| 🌿 **Herbal Name Resolution** | Maps Indian cosmetic names (Amla, Reetha, Methi, Bhringraj) to INCI standards |
| 📥 **Report Download** | Export compliance reports as `.txt` files |
| 🔍 **Search & Filter** | Search through ingredient breakdown results |
| 🎨 **Modern Dark UI** | Glassmorphism design with emerald/cyan accents on pure black background |

---

## 🛠 Tech Stack

### Backend
| Technology | Version | Purpose |
|-----------|---------|---------|
| Java | 21+ | Core language |
| Spring Boot | 3.4.3 | Application framework |
| Spring Data JPA | — | ORM & database access |
| Hibernate | — | JPA implementation |
| PostgreSQL | 15+ | Relational database |
| Maven | 3.9+ | Build & dependency management |

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
┌─────────────────────┐     HTTP/REST      ┌─────────────────────────┐
│                     │  ◄──────────────►  │                         │
│   React Frontend    │    Port 5173       │   Spring Boot Backend   │
│   (Vite + Tailwind) │    (proxied)       │      Port 8080          │
│                     │                    │                         │
└─────────────────────┘                    └────────┬────────────────┘
                                                    │
                                                    │ JPA / Hibernate
                                                    ▼
                                           ┌─────────────────────┐
                                           │   PostgreSQL         │
                                           │   (dermadata DB)     │
                                           │                     │
                                           │  • 103 ingredients  │
                                           │  • 15 combo rules   │
                                           └─────────────────────┘
```

### Request Flow

```
User Input → ExtractionService (text normalization / INCI mapping)
           → SafetyScoreEngineService (regulation check + scoring)
           → LlmService (AI insights — currently mocked)
           → AnalysisReport (JSON response)
```

---

## 📁 Project Structure

```
SpringBootChemicalProject/
│
├── pom.xml                                    # Maven config
├── README.md
│
├── scripts/
│   └── seed_database.py                       # DB seeding (103 ingredients, 15 rules)
│
├── src/main/java/com/dermadata/
│   ├── DermaDataApplication.java              # Spring Boot entry point
│   ├── config/
│   │   └── WebConfig.java                     # CORS configuration
│   ├── controller/
│   │   ├── AnalysisController.java            # POST /api/v1/analyze, /extract
│   │   └── IngredientController.java          # GET /api/v1/ingredients
│   ├── dto/
│   │   ├── AnalysisRequest.java               # Request payload
│   │   ├── AnalysisReport.java                # Response payload
│   │   ├── IngredientInput.java               # Single ingredient input
│   │   ├── IngredientResult.java              # Single ingredient result
│   │   └── CombinationWarning.java            # Combination violation
│   ├── entity/
│   │   ├── IngredientRegulation.java          # JPA entity — EU regulation data
│   │   └── CombinationRule.java               # JPA entity — combination rules
│   ├── repository/
│   │   ├── IngredientRegulationRepository.java
│   │   └── CombinationRuleRepository.java
│   └── service/
│       ├── SafetyScoreEngineService.java       # Core scoring algorithm
│       ├── ExtractionService.java              # INCI name normalization (120+ mappings)
│       └── LlmService.java                     # AI insights (mocked)
│
├── src/main/resources/
│   └── application.properties                  # DB config, server settings
│
└── dermadata-ui/                               # React frontend
    ├── index.html
    ├── vite.config.js                          # Vite + Tailwind + API proxy
    ├── package.json
    └── src/
        ├── main.jsx                            # React entry point
        ├── App.jsx                             # Root component
        ├── index.css                           # Design system (dark theme)
        ├── services/
        │   └── api.js                          # Axios API service layer
        └── components/
            ├── IngredientAnalyzer.jsx           # Input page
            └── SafetyResultsDashboard.jsx       # Results page
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 21+** (JDK)
- **Maven 3.9+**
- **PostgreSQL 15+**
- **Node.js 18+** & npm
- **Python 3.10+** (for DB seeding only)

### 1. Clone the Repository

```bash
git clone https://github.com/yourusername/DermaData.git
cd DermaData
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

### 7. Open the Application

Navigate to **http://localhost:5173** in your browser.

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
| `GET` | `/api/v1/ingredients/search?query=` | Search regulations by name |

### System

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/v1/health` | Health check |

### Example: Analyze Ingredients

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
  "safetyScore": 75,
  "scoreCategory": "SAFE",
  "productType": "Shampoo",
  "totalIngredients": 4,
  "flaggedIngredients": 2,
  "prohibitedCount": 1,
  "exceededCount": 0,
  "combinationViolations": 0,
  "ingredientResults": [
    {
      "inciName": "FORMALDEHYDE",
      "status": "PROHIBITED",
      "penaltyPoints": 20,
      "regulationRef": "Annex II/1577"
    }
  ]
}
```

---

## 🧮 Safety Scoring Algorithm

The scoring engine starts at **100 points** and applies deductions:

| Violation Type | Penalty | Description |
|---------------|---------|-------------|
| **Prohibited Ingredient** | **−20 pts** | Substance banned under EU Annex II |
| **Exceeded Concentration** | **−10 pts** | Detected % exceeds EU maximum limit |
| **Combination Violation** | **−5 pts** | Dangerous ingredient interaction detected |

### Score Categories

| Score Range | Category | Meaning |
|------------|----------|---------|
| 71 – 100 | 🟢 **SAFE** | Product meets EU compliance standards |
| 41 – 70 | 🟡 **CAUTION** | Some ingredients flagged; review recommended |
| 0 – 40 | 🔴 **DANGER** | Multiple violations; product fails compliance |

### Ingredient Status Classification

```
PROHIBITED  → Banned under EU Annex II (e.g., Formaldehyde, Lilial)
EXCEEDED    → Concentration > EU max limit (e.g., SLS > 1%)
RESTRICTED  → Has conditions/limits but within bounds (e.g., Methylparaben ≤ 0.4%)
SAFE        → Regulated and within all limits
NOT_REGULATED → Not found in EU CosIng database
```

---

## 🗄 Database

### Ingredient Regulations (103 entries)

Categories covered:

| Category | Examples | Count |
|----------|----------|-------|
| Surfactants | SLS, SLES, Cocamidopropyl Betaine, Coco Glucoside | 10 |
| Preservatives | Methylparaben, MIT, MCI, Phenoxyethanol, Sodium Benzoate | 13 |
| Prohibited | Formaldehyde, Hydroquinone, Lilial, Mercury, Coal Tar | 9 |
| Actives | Salicylic Acid, Zinc Pyrithione, Ketoconazole, Lactic Acid | 10 |
| Silicones | Dimethicone, Dimethiconol, Amodimethicone, PDMS | 4 |
| Herbal Extracts | Amla, Hibiscus, Reetha, Methi, Henna, Bhringraj, Aloe | 9 |
| Fragrance Allergens | Limonene, Linalool, Hexyl Cinnamal, Benzyl Salicylate | 4 |
| Colorants | CI 19140, CI 16035, Quinazarine Green SS | 6 |
| Emollients | Glycerin, Panthenol, Glycol Distearate, Shea Oil | 12 |
| pH/Chelators | Citric Acid, NaOH, EDTA, Sodium Citrate | 8+ |
| Polymers | Polyquaternium-7/10, Guar HPC, Carbomer | 6 |

### Combination Rules (15 entries)

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

## 🖼 Screenshots

### Input Page
- Dark glassmorphism UI with emerald/cyan accents
- Product type selector (Shampoo / Soap / Conditioner)
- Dual input: image upload + text paste
- Extracted Chemical Profile table with confidence bars

### Results Dashboard
- Circular safety gauge (0–100)
- Stat cards: Total, Flagged, Exceeded, Violations
- Ingredient breakdown with status badges (SAFE / RESTRICTED / PROHIBITED)
- Combination warnings with penalty details
- Search & Download Report functionality

---

## 🗺 Future Roadmap

- [ ] **Real AI Integration** — Replace mock ExtractionService with Gemini/Claude API for true OCR
- [ ] **Redis Caching** — Cache analysis reports for repeat queries
- [ ] **FastAPI Microservice** — Separate Python microservice for AI extraction
- [ ] **pgvector Fuzzy Matching** — Fuzzy INCI name matching for misspelled ingredients
- [ ] **PDF Report Export** — Generate professional PDF compliance reports
- [ ] **User Authentication** — Save analysis history per user
- [ ] **Product Database** — Pre-loaded popular product formulations
- [ ] **Batch Analysis** — Analyze multiple products simultaneously
- [ ] **Mobile Responsive** — Optimize for mobile camera scanning

---

## 📄 License

This project is for **educational and informational purposes** only. It is not a substitute for professional regulatory advice.

---

<p align="center">
  Built with ❤️ using Spring Boot + React
</p>
