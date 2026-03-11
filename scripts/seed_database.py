"""
DermaData — Database Seeding Script
Populates PostgreSQL with 100+ INCI chemicals from EU CosIng (realistic mock data)
and 15+ combination rules.

Usage:
    pip install psycopg2-binary
    python seed_database.py
"""

import psycopg2
import sys

DB_CONFIG = {
    "host": "localhost",
    "port": 5432,
    "dbname": "dermadata",
    "user": "postgres",
    "password": "avirat"
}

# ════════════════════════════════════════════
# 100+ INCI Ingredient Regulations
# ════════════════════════════════════════════
INGREDIENTS = [
    # (inci_name, max_concentration, restricted, prohibited, conditions, product_types, regulation_ref)

    # ── Surfactants ──
    ("SODIUM LAURYL SULFATE", 1.0, True, False, "Max 1% in leave-on products", "Soap,Shampoo,Conditioner", "Annex III/49"),
    ("SODIUM LAURETH SULFATE", 3.0, False, False, "Generally recognized as safe surfactant", "Shampoo,Soap", None),
    ("COCAMIDOPROPYL BETAINE", 5.0, False, False, "Amphoteric surfactant, may cause sensitization", "Shampoo,Soap", None),
    ("SODIUM COCOYL ISETHIONATE", None, False, False, "Mild surfactant", "Soap,Shampoo", None),
    ("DISODIUM LAURETH SULFOSUCCINATE", None, False, False, "Mild anionic surfactant", "Shampoo", None),
    ("COCAMIDE MEA", None, False, False, "Non-ionic surfactant / foam booster; may contain DEA impurities", "Shampoo,Soap", None),
    ("SODIUM XYLENESULFONATE", None, False, False, "Hydrotrope / solubilizer; no EU restriction", "Shampoo,Soap", None),
    ("COCO-BETAINE", 5.0, False, False, "Amphoteric surfactant derived from coconut", "Shampoo,Soap", None),
    ("SODIUM LAUROYL ISETHIONATE", None, False, False, "Mild anionic surfactant from coconut", "Soap,Shampoo", None),
    ("COCO GLUCOSIDE", None, False, False, "Mild non-ionic surfactant; sugar-derived", "Shampoo,Soap", None),

    # ── Preservatives (Annex V) ──
    ("METHYLPARABEN", 0.4, True, False, "Max 0.4% as acid; total parabens max 0.8%", "Soap,Shampoo,Conditioner", "Annex V/12"),
    ("PROPYLPARABEN", 0.14, True, False, "Max 0.14% as acid; prohibited in leave-on for under 3", "Soap,Shampoo,Conditioner", "Annex V/12a"),
    ("BUTYLPARABEN", 0.14, True, False, "Max 0.14% as acid", "Soap,Shampoo,Conditioner", "Annex V/12a"),
    ("PHENOXYETHANOL", 1.0, True, False, "Max 1.0%", "Soap,Shampoo,Conditioner", "Annex V/29"),
    ("BENZALKONIUM CHLORIDE", 0.1, True, False, "Max 0.1% in rinse-off products", "Shampoo,Soap", "Annex V/44"),
    ("METHYLISOTHIAZOLINONE", 0.0015, True, False, "Max 0.0015% in rinse-off only; prohibited in leave-on", "Shampoo,Soap", "Annex V/57"),
    ("METHYLCHLOROISOTHIAZOLINONE", 0.0015, True, False, "Max 0.0015% in rinse-off only (mixed with MIT 3:1)", "Shampoo,Soap", "Annex V/39"),
    ("DMDM HYDANTOIN", 0.6, True, False, "Formaldehyde releaser; max 0.6%", "Shampoo,Soap", "Annex V/13"),
    ("IMIDAZOLIDINYL UREA", 0.6, True, False, "Formaldehyde releaser; max 0.6%", "Soap,Shampoo,Conditioner", "Annex V/15"),
    ("SODIUM BENZOATE", 2.5, False, False, "Max 2.5% as acid", "Soap,Shampoo,Conditioner", "Annex V/1"),
    ("POTASSIUM SORBATE", 0.6, False, False, "Max 0.6% as acid", "Soap,Shampoo,Conditioner", "Annex V/4"),
    ("ETHYLHEXYLGLYCERIN", None, False, False, "Preservative booster, no EU limit", "Soap,Shampoo,Conditioner", None),
    ("BENZYL ALCOHOL", 1.0, True, False, "Max 1.0% as preservative", "Soap,Shampoo,Conditioner", "Annex V/34"),

    # ── Prohibited Substances (Annex II) ──
    ("FORMALDEHYDE", None, False, True, "CMR Category 1B; prohibited as ingredient", "Soap,Shampoo,Conditioner", "Annex II/1577"),
    ("HYDROQUINONE", None, False, True, "Prohibited in cosmetics (except hair dyes)", "Soap,Shampoo", "Annex II/1339"),
    ("COAL TAR", None, False, True, "Prohibited; carcinogenic", "Shampoo", "Annex II/1395"),
    ("TRICLOSAN", 0.3, True, False, "Max 0.3% in hand soaps, not allowed in other products", "Soap", "Annex V/25"),
    ("MERCURY", None, False, True, "Prohibited", "Soap,Shampoo,Conditioner", "Annex II/221"),
    ("LEAD ACETATE", None, False, True, "Prohibited", "Shampoo", "Annex II/289"),
    ("CHLOROFORM", None, False, True, "Prohibited", "Soap,Shampoo,Conditioner", "Annex II/182"),
    ("BITHIONOL", None, False, True, "Prohibited", "Soap,Shampoo", "Annex II/362"),
    ("BUTYLPHENYL METHYLPROPIONAL", None, False, True, "Lilial; prohibited since 2022 (CMR substance)", "Soap,Shampoo,Conditioner", "Annex II/1666"),

    # ── Active/Functional Ingredients ──
    ("SALICYLIC ACID", 2.0, True, False, "Max 2.0% in rinse-off hair products", "Shampoo", "Annex III/26"),
    ("ZINC PYRITHIONE", 1.0, True, False, "Max 1.0% in rinse-off hair products only", "Shampoo", "Annex III/30"),
    ("KETOCONAZOLE", 2.0, True, False, "Max 2.0% in anti-dandruff shampoos", "Shampoo", "Annex III"),
    ("GLYCOLIC ACID", 4.0, True, False, "Max 4.0%, pH >= 3.5", "Soap,Shampoo", "Annex III/28"),
    ("RETINOL", 0.3, True, False, "Max 0.3% Retinol equivalent in body products", "Soap", "Annex III"),
    ("RETINYL PALMITATE", 0.55, True, False, "Max 0.055% as Retinol equivalent in body", "Soap", "Annex III"),
    ("HYDROGEN PEROXIDE", 12.0, True, False, "Max 12% in hair products", "Shampoo,Conditioner", "Annex III/12"),
    ("SELENIUM DISULFIDE", 1.0, True, False, "Max 1% in anti-dandruff shampoos", "Shampoo", "Annex III/49"),
    ("PIROCTONE OLAMINE", 1.0, False, False, "Max 1.0% anti-dandruff agent", "Shampoo", "Annex III"),
    ("CLIMBAZOLE", 0.5, True, False, "Max 0.5% in rinse-off products", "Shampoo", "Annex III/54"),
    ("CIMBAZOLE", 0.5, True, False, "Alternate spelling of Climbazole; Max 0.5% in rinse-off", "Shampoo", "Annex III/54"),
    ("LACTIC ACID", 2.5, True, False, "Max 2.5% in leave-on; no limit in rinse-off at pH >= 3.5", "Shampoo,Soap,Conditioner", "Annex III/28"),
    ("MENTHOL", None, False, False, "Cooling agent; no EU restriction", "Shampoo,Soap,Conditioner", None),

    # ── Emollients, Conditioning, Silicones, Thickeners ──
    ("GLYCERIN", None, False, False, "Humectant; no restriction", "Soap,Shampoo,Conditioner", None),
    ("DIMETHICONE", None, False, False, "Silicone conditioner; no restriction", "Shampoo,Conditioner", None),
    ("DIMETHICONOL", None, False, False, "Silicone conditioning agent; no restriction", "Shampoo,Conditioner", None),
    ("AMODIMETHICONE", None, False, False, "Amino-functional silicone; selective conditioning", "Shampoo,Conditioner", None),
    ("POLYDIMETHYLSILOXANE", None, False, False, "Silicone polymer (PDMS); conditioning, no EU restriction", "Shampoo,Conditioner", None),
    ("PANTHENOL", None, False, False, "Pro-vitamin B5; no restriction", "Shampoo,Conditioner", None),
    ("CETYL ALCOHOL", None, False, False, "Fatty alcohol emollient", "Conditioner,Soap", None),
    ("STEARYL ALCOHOL", None, False, False, "Fatty alcohol emollient", "Conditioner,Soap", None),
    ("CETEARYL ALCOHOL", None, False, False, "Emulsifying fatty alcohol", "Conditioner,Shampoo", None),
    ("TOCOPHEROL", None, False, False, "Vitamin E; antioxidant", "Soap,Shampoo,Conditioner", None),
    ("ALOE BARBADENSIS LEAF JUICE", None, False, False, "Aloe vera; skin soothing", "Soap,Shampoo,Conditioner", None),
    ("ARGAN OIL", None, False, False, "Conditioning oil", "Shampoo,Conditioner", None),
    ("COCONUT OIL", None, False, False, "COCOS NUCIFERA OIL; no restriction", "Soap,Shampoo,Conditioner", None),
    ("SHEA BUTTER", None, False, False, "BUTYROSPERMUM PARKII BUTTER", "Soap,Conditioner", None),
    ("SHEA OIL", None, False, False, "BUTYROSPERMUM PARKII OIL; emollient", "Soap,Conditioner", None),
    ("JOJOBA OIL", None, False, False, "SIMMONDSIA CHINENSIS SEED OIL", "Shampoo,Conditioner", None),
    ("MINERAL OIL", None, False, False, "PARAFFINUM LIQUIDUM; emollient, no EU restriction", "Soap,Shampoo,Conditioner", None),
    ("GLYCOL DISTEARATE", None, False, False, "Pearlescent agent / emollient; no EU restriction", "Shampoo,Soap", None),
    ("STEARIC ACID", None, False, False, "Fatty acid emulsifier; no restriction", "Soap,Conditioner", None),
    ("ROSEMARY OIL", None, False, False, "ROSMARINUS OFFICINALIS LEAF OIL; hair conditioning", "Shampoo,Conditioner", None),
    ("TEA TREE OIL", None, False, False, "MELALEUCA ALTERNIFOLIA LEAF OIL; antimicrobial properties", "Shampoo,Soap", None),

    # ── Herbal / Plant Extracts ──
    ("EMBLICA OFFICINALIS", None, False, False, "Amla extract; hair conditioning, no EU restriction", "Shampoo,Conditioner", None),
    ("HIBISCUS ROSA-SINENSIS", None, False, False, "Hibiscus flower extract; hair conditioning", "Shampoo,Conditioner", None),
    ("SAPINDUS TRIFOLIATUS", None, False, False, "Reetha/Soapnut extract; natural surfactant", "Shampoo,Soap", None),
    ("TRIGONELLA FOENUM-GRAECUM", None, False, False, "Fenugreek (Methi) seed extract; hair conditioning", "Shampoo,Conditioner", None),
    ("LAWSONIA INERMIS", None, False, False, "Henna leaf extract; colourant/conditioner", "Shampoo,Conditioner", None),
    ("PRUNUS AMYGDALUS", None, False, False, "Almond (Badam) kernel extract; emollient", "Shampoo,Conditioner,Soap", None),
    ("GLYCYRRHIZA GLABRA", None, False, False, "Mulethi/Licorice root extract; soothing agent", "Shampoo,Conditioner", None),
    ("ECLIPTA ALBA", None, False, False, "Bhringraj plant oil extract; traditional hair care", "Shampoo,Conditioner", None),
    ("ALOE BARBADENSIS", None, False, False, "Aloe vera / Kumari; soothing and moisturizing", "Shampoo,Conditioner,Soap", None),

    # ── pH Adjusters / Chelators / Misc ──
    ("CITRIC ACID", None, False, False, "pH adjuster; no restriction", "Soap,Shampoo,Conditioner", None),
    ("SODIUM HYDROXIDE", 5.0, True, False, "Max 5% in hair straighteners", "Soap,Shampoo", "Annex III/15a"),
    ("SODIUM CHLORIDE", None, False, False, "Viscosity modifier; no restriction", "Shampoo,Soap", None),
    ("SODIUM CITRATE", None, False, False, "Buffering / chelating agent; no restriction", "Shampoo,Soap,Conditioner", None),
    ("SODIUM PALMITATE", None, False, False, "Soap base salt (sodium salt of palmitic acid); no restriction", "Soap", None),
    ("TETRASODIUM EDTA", None, False, False, "Chelating agent; stabilizer; no EU cosmetic restriction", "Shampoo,Soap,Conditioner", None),
    ("PARFUM", None, False, False, "Fragrance; allergens must be declared if above threshold", "Soap,Shampoo,Conditioner", "Annex III/67-92"),
    ("CARBOMER", None, False, False, "Thickener / viscosity controller; no restriction", "Shampoo,Soap,Conditioner", None),
    ("PEG-90M", None, False, False, "Thickener / film former; no restriction", "Shampoo,Conditioner", None),
    ("PPG-5-CETETH-20", None, False, False, "Emulsifier / solubilizer; no EU restriction", "Shampoo,Conditioner", None),
    ("LAURETH-4", None, False, False, "Non-ionic surfactant / emulsifier; no restriction", "Shampoo,Conditioner", None),
    ("TITANIUM DIOXIDE", None, True, False, "CI 77891; restricted as colourant in certain forms", "Soap,Shampoo", "Annex IV"),

    # ── Polymers / Conditioning Agents / Film-Formers ──
    ("POLYQUATERNIUM-7", None, False, False, "Conditioning polymer", "Shampoo,Conditioner", None),
    ("POLYQUATERNIUM-10", None, False, False, "Anti-static agent", "Shampoo,Conditioner", None),
    ("GUAR HYDROXYPROPYLTRIMONIUM CHLORIDE", None, False, False, "Conditioning polymer", "Shampoo,Conditioner", None),
    ("PEG-150 DISTEARATE", None, False, False, "Thickener/emulsifier", "Shampoo", None),
    ("COCODIMONIUM HYDROXYPROPYL HYDROLYZED WHEAT PROTEIN", None, False, False, "Protein conditioning agent", "Shampoo,Conditioner", None),
    ("2-OLEAMIDO-1,3-OCTADECANE DIOL", None, False, False, "Ceramide analog; hair repair agent; no restriction", "Shampoo,Conditioner", None),

    # ── Fragrance Allergens (Annex III/67-92) ──
    ("LIMONENE", None, True, False, "Fragrance allergen; must be declared above 0.01% rinse-off / 0.001% leave-on", "Soap,Shampoo,Conditioner", "Annex III/67"),
    ("LINALOOL", None, True, False, "Fragrance allergen; must be declared above 0.01% rinse-off / 0.001% leave-on", "Soap,Shampoo,Conditioner", "Annex III/74"),
    ("HEXYL CINNAMAL", None, True, False, "Fragrance allergen; must be declared above threshold", "Soap,Shampoo,Conditioner", "Annex III/72"),
    ("BENZYL SALICYLATE", None, True, False, "Fragrance allergen; must be declared above threshold", "Soap,Shampoo,Conditioner", "Annex III/85"),

    # ── Colorants (Annex IV) ──
    ("CI 19140", None, True, False, "Tartrazine yellow; must be listed, allergen risk", "Shampoo,Soap", "Annex IV"),
    ("CI 17200", None, True, False, "Red 33; restricted colorant", "Shampoo,Soap", "Annex IV"),
    ("CI 16035", None, True, False, "Allura Red AC; permitted food/cosmetic colorant", "Shampoo,Soap", "Annex IV"),
    ("QUINAZARINE GREEN SS", None, True, False, "Green dye; limited to rinse-off products", "Shampoo", "Annex IV"),
    ("QUINOLINE YELLOW WS", None, True, False, "CI 47005; yellow dye; restricted colorant", "Shampoo,Soap", "Annex IV"),
    ("TONEY RED", None, True, False, "Red dye colourant for cosmetics", "Shampoo,Soap", "Annex IV"),
]

# ════════════════════════════════════════════
# 15+ Combination Rules
# ════════════════════════════════════════════
COMBINATION_RULES = [
    # (ingredient_a, ingredient_b, condition, safe_conc_a, required_conc_b, explanation, source)
    ("METHYLPARABEN", "PROPYLPARABEN",
     "Combined parabens must not exceed 0.8% total",
     0.4, 0.14,
     "When both parabens are present, total concentration of all parabens (as acid) must not exceed 0.8%. Individual limits also apply.",
     "EU Reg. (EC) 1223/2009, Annex V/12"),

    ("SODIUM LAURYL SULFATE", "METHYLISOTHIAZOLINONE",
     "SLS enhances penetration of sensitizing preservatives",
     1.0, 0.0015,
     "SLS disrupts skin barrier, increasing penetration of MIT. Combined use increases sensitization risk significantly.",
     "SCCS/1521/13"),

    ("SALICYLIC ACID", "GLYCOLIC ACID",
     "Dual acid combination — pH and total concentration critical",
     2.0, 4.0,
     "Combined AHA/BHA increases exfoliation intensity. Total acid concentration must be managed; pH must remain >= 3.5.",
     "SCCS/1629/21"),

    ("METHYLCHLOROISOTHIAZOLINONE", "METHYLISOTHIAZOLINONE",
     "Must be used in 3:1 ratio; max combined 0.0015%",
     0.0015, 0.0015,
     "MCI and MIT must be used together in 3:1 ratio (MCI:MIT). Maximum combined concentration 0.0015% in rinse-off only.",
     "EU Reg. Annex V/39"),

    ("PHENOXYETHANOL", "ETHYLHEXYLGLYCERIN",
     "Synergistic preservative system",
     1.0, None,
     "Ethylhexylglycerin boosts phenoxyethanol's antimicrobial activity. This allows lower effective concentrations. No violation — beneficial combination.",
     "Cosmetic preservation best practices"),

    ("SODIUM HYDROXIDE", "HYDROGEN PEROXIDE",
     "Strongly reactive combination",
     5.0, 12.0,
     "NaOH and H2O2 together cause exothermic reaction. Must be carefully buffered in hair treatments. pH and concentration controls essential.",
     "SCCS/1553/15"),

    ("RETINOL", "SALICYLIC ACID",
     "Increased irritation potential",
     0.3, 2.0,
     "Retinol combined with Salicylic Acid significantly increases skin irritation and photosensitivity. Not recommended in same leave-on product.",
     "Dermatological consensus / SCCS opinion"),

    ("RETINOL", "GLYCOLIC ACID",
     "Increased irritation and photosensitivity",
     0.3, 4.0,
     "AHA + Retinoid combination dramatically increases skin irritation. Should not be used in same leave-on product.",
     "Dermatological consensus"),

    ("FORMALDEHYDE", "DMDM HYDANTOIN",
     "Double formaldehyde exposure",
     None, 0.6,
     "DMDM Hydantoin releases formaldehyde. If free formaldehyde is also present, combined exposure far exceeds safe limits.",
     "EU Reg. Annex II/1577 + Annex V/13"),

    ("TRICLOSAN", "SODIUM LAURYL SULFATE",
     "Enhanced systemic absorption",
     0.3, 1.0,
     "SLS increases dermal penetration of Triclosan, raising concerns about endocrine disruption at sub-threshold concentrations.",
     "Environmental Health Perspectives"),

    ("ZINC PYRITHIONE", "SELENIUM DISULFIDE",
     "Incompatible anti-dandruff actives",
     1.0, 1.0,
     "Both are anti-dandruff actives with different mechanisms. Combined use provides no benefit and increases scalp irritation.",
     "Dermatological formulary guidance"),

    ("BENZALKONIUM CHLORIDE", "SODIUM LAURYL SULFATE",
     "Cationic-anionic incompatibility",
     0.1, 1.0,
     "Cationic (BAC) and anionic (SLS) surfactants form insoluble precipitates, reducing efficacy of both and causing product instability.",
     "Surfactant chemistry principles"),

    ("SALICYLIC ACID", "LACTIC ACID",
     "Dual acid combination — enhanced exfoliation risk",
     2.0, 2.5,
     "BHA + AHA combination lowers effective pH significantly. Combined use increases irritation potential. Monitor total acid concentration and pH.",
     "SCCS guidance on AHA/BHA products"),

    ("SODIUM LAURYL SULFATE", "DIMETHICONE",
     "Surfactant-silicone interaction in shampoos",
     1.0, None,
     "SLS can strip silicone buildup but also increases scalp dryness. High SLS with heavy silicones may cause residue buildup requiring clarifying.",
     "Cosmetic chemistry formulation guidance"),

    ("BUTYLPHENYL METHYLPROPIONAL", "LINALOOL",
     "Double fragrance allergen exposure",
     None, None,
     "Both are significant fragrance allergens. Combined presence increases risk of contact sensitization. Butylphenyl methylpropional (Lilial) is now prohibited in EU.",
     "SCCS/1459/11 + EU Reg. Annex II/1666"),
]


def seed_database():
    """Connect to PostgreSQL and insert seed data."""
    try:
        conn = psycopg2.connect(**DB_CONFIG)
        cur = conn.cursor()
        print("✓ Connected to PostgreSQL")

        # ── Insert Ingredient Regulations ──
        insert_ingredient_sql = """
            INSERT INTO ingredient_regulation 
                (inci_name, max_concentration, restricted, prohibited, conditions, product_types, regulation_ref)
            VALUES (%s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (inci_name) DO UPDATE SET
                max_concentration = EXCLUDED.max_concentration,
                restricted = EXCLUDED.restricted,
                prohibited = EXCLUDED.prohibited,
                conditions = EXCLUDED.conditions,
                product_types = EXCLUDED.product_types,
                regulation_ref = EXCLUDED.regulation_ref;
        """

        for ing in INGREDIENTS:
            cur.execute(insert_ingredient_sql, ing)

        print(f"✓ Inserted/updated {len(INGREDIENTS)} ingredient regulations")

        # ── Insert Combination Rules ──
        insert_rule_sql = """
            INSERT INTO combination_rule 
                (ingredient_a, ingredient_b, condition_desc, safe_concentration_a, required_concentration_b, explanation, source)
            VALUES (%s, %s, %s, %s, %s, %s, %s);
        """

        # Clear existing rules first to avoid duplicates on re-run
        cur.execute("DELETE FROM combination_rule;")

        for rule in COMBINATION_RULES:
            cur.execute(insert_rule_sql, rule)

        print(f"✓ Inserted {len(COMBINATION_RULES)} combination rules")

        conn.commit()
        print(f"\n✓ Database seeded successfully!")
        print(f"  → {len(INGREDIENTS)} ingredients")
        print(f"  → {len(COMBINATION_RULES)} combination rules")

        cur.close()
        conn.close()

    except psycopg2.OperationalError as e:
        print(f"✗ Database connection failed: {e}")
        print("  Make sure PostgreSQL is running and the 'dermadata' database exists.")
        print("  Create it with: CREATE DATABASE dermadata;")
        sys.exit(1)
    except Exception as e:
        print(f"✗ Error: {e}")
        sys.exit(1)


if __name__ == "__main__":
    seed_database()
