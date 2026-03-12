package com.dermadata.service;

import com.dermadata.dto.IngredientInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Mock AI Extraction Service — normalizes raw ingredient text into standard INCI names.
 */
@Service
public class ExtractionService {

    private static final Logger log = LoggerFactory.getLogger(ExtractionService.class);
    private static final Map<String, String> INCI_MAP = new LinkedHashMap<>();

    static {
        // ── Water ──
        INCI_MAP.put("water", "AQUA");
        INCI_MAP.put("aqua", "AQUA");
        INCI_MAP.put("eau", "AQUA");
        INCI_MAP.put("water/eau", "AQUA");

        // ── Surfactants ──
        INCI_MAP.put("sls", "SODIUM LAURYL SULFATE");
        INCI_MAP.put("sodium lauryl sulfate", "SODIUM LAURYL SULFATE");
        INCI_MAP.put("sodium laureth sulfate", "SODIUM LAURETH SULFATE");
        INCI_MAP.put("sodium laureth sulphate", "SODIUM LAURETH SULFATE");
        INCI_MAP.put("sles", "SODIUM LAURETH SULFATE");
        INCI_MAP.put("cocamidopropyl betaine", "COCAMIDOPROPYL BETAINE");
        INCI_MAP.put("coco-betaine", "COCO-BETAINE");
        INCI_MAP.put("coco betaine", "COCO-BETAINE");
        INCI_MAP.put("cocamide mea", "COCAMIDE MEA");
        INCI_MAP.put("sodium cocoyl isethionate", "SODIUM COCOYL ISETHIONATE");
        INCI_MAP.put("sodium lauroyl isethionate", "SODIUM LAUROYL ISETHIONATE");
        INCI_MAP.put("sodium xylenesulfonate", "SODIUM XYLENESULFONATE");
        INCI_MAP.put("coco glucoside", "COCO GLUCOSIDE");

        // ── Preservatives ──
        INCI_MAP.put("methylparaben", "METHYLPARABEN");
        INCI_MAP.put("propylparaben", "PROPYLPARABEN");
        INCI_MAP.put("butylparaben", "BUTYLPARABEN");
        INCI_MAP.put("phenoxyethanol", "PHENOXYETHANOL");
        INCI_MAP.put("benzalkonium chloride", "BENZALKONIUM CHLORIDE");
        INCI_MAP.put("methylisothiazolinone", "METHYLISOTHIAZOLINONE");
        INCI_MAP.put("methylchloroisothiazolinone", "METHYLCHLOROISOTHIAZOLINONE");
        INCI_MAP.put("sodium benzoate", "SODIUM BENZOATE");
        INCI_MAP.put("benzyl alcohol", "BENZYL ALCOHOL");
        INCI_MAP.put("dmdm hydantoin", "DMDM HYDANTOIN");
        INCI_MAP.put("ethylhexylglycerin", "ETHYLHEXYLGLYCERIN");

        // ── Prohibited / Restricted ──
        INCI_MAP.put("formaldehyde", "FORMALDEHYDE");
        INCI_MAP.put("hydroquinone", "HYDROQUINONE");
        INCI_MAP.put("coal tar", "COAL TAR");
        INCI_MAP.put("triclosan", "TRICLOSAN");
        INCI_MAP.put("butylphenyl methylpropional", "BUTYLPHENYL METHYLPROPIONAL");
        INCI_MAP.put("lilial", "BUTYLPHENYL METHYLPROPIONAL");

        // ── Silicones ──
        INCI_MAP.put("dimethicone", "DIMETHICONE");
        INCI_MAP.put("dimethiconol", "DIMETHICONOL");
        INCI_MAP.put("amodimethicone", "AMODIMETHICONE");
        INCI_MAP.put("polydimethylsiloxane", "POLYDIMETHYLSILOXANE");
        INCI_MAP.put("polydimethylsiloxane emulsion", "POLYDIMETHYLSILOXANE");

        // ── Conditioning / Emollients ──
        INCI_MAP.put("glycerin", "GLYCERIN");
        INCI_MAP.put("glycerine", "GLYCERIN");
        INCI_MAP.put("panthenol", "PANTHENOL");
        INCI_MAP.put("tocopherol", "TOCOPHEROL");
        INCI_MAP.put("vitamin e", "TOCOPHEROL");
        INCI_MAP.put("cetyl alcohol", "CETYL ALCOHOL");
        INCI_MAP.put("stearyl alcohol", "STEARYL ALCOHOL");
        INCI_MAP.put("cetearyl alcohol", "CETEARYL ALCOHOL");
        INCI_MAP.put("glycol distearate", "GLYCOL DISTEARATE");
        INCI_MAP.put("stearic acid", "STEARIC ACID");
        INCI_MAP.put("mineral oil", "MINERAL OIL");
        INCI_MAP.put("shea oil", "SHEA OIL");
        INCI_MAP.put("shea butter", "SHEA BUTTER");
        INCI_MAP.put("coconut oil", "COCONUT OIL");
        INCI_MAP.put("argan oil", "ARGAN OIL");
        INCI_MAP.put("jojoba oil", "JOJOBA OIL");
        INCI_MAP.put("menthol", "MENTHOL");

        // ── Oils / Herbal ──
        INCI_MAP.put("rosemary oil", "ROSEMARY OIL");
        INCI_MAP.put("tea tree oil", "TEA TREE OIL");
        INCI_MAP.put("tea free oil", "TEA TREE OIL");
        INCI_MAP.put("aloevera", "ALOE BARBADENSIS");
        INCI_MAP.put("aloe vera", "ALOE BARBADENSIS");
        INCI_MAP.put("aloe barbadensis", "ALOE BARBADENSIS");
        INCI_MAP.put("aloe barbadensis leaf juice", "ALOE BARBADENSIS LEAF JUICE");

        // ── Herbal Extracts (Indian cosmetics) ──
        INCI_MAP.put("amla", "EMBLICA OFFICINALIS");
        INCI_MAP.put("emblica officinalis", "EMBLICA OFFICINALIS");
        INCI_MAP.put("hibiscus rosa-sinensis", "HIBISCUS ROSA-SINENSIS");
        INCI_MAP.put("akusum", "HIBISCUS ROSA-SINENSIS");
        INCI_MAP.put("reetha", "SAPINDUS TRIFOLIATUS");
        INCI_MAP.put("sapindus trifoliatus", "SAPINDUS TRIFOLIATUS");
        INCI_MAP.put("sapindus trifoliatus linn", "SAPINDUS TRIFOLIATUS");
        INCI_MAP.put("methi", "TRIGONELLA FOENUM-GRAECUM");
        INCI_MAP.put("trigonella foenum-graeceum", "TRIGONELLA FOENUM-GRAECUM");
        INCI_MAP.put("trigonella foenum-graecum", "TRIGONELLA FOENUM-GRAECUM");
        INCI_MAP.put("henna", "LAWSONIA INERMIS");
        INCI_MAP.put("lawsonia inermis", "LAWSONIA INERMIS");
        INCI_MAP.put("badam", "PRUNUS AMYGDALUS");
        INCI_MAP.put("almond", "PRUNUS AMYGDALUS");
        INCI_MAP.put("prunus amygdalus", "PRUNUS AMYGDALUS");
        INCI_MAP.put("mulethi", "GLYCYRRHIZA GLABRA");
        INCI_MAP.put("glycyrrhiza glabra", "GLYCYRRHIZA GLABRA");
        INCI_MAP.put("glycyrrhiza glabra linn", "GLYCYRRHIZA GLABRA");
        INCI_MAP.put("bhringraj", "ECLIPTA ALBA");
        INCI_MAP.put("eclipta alba", "ECLIPTA ALBA");
        INCI_MAP.put("kumari", "ALOE BARBADENSIS");

        // ── pH Adjusters / Chelators ──
        INCI_MAP.put("citric acid", "CITRIC ACID");
        INCI_MAP.put("lactic acid", "LACTIC ACID");
        INCI_MAP.put("sodium hydroxide", "SODIUM HYDROXIDE");
        INCI_MAP.put("sodium chloride", "SODIUM CHLORIDE");
        INCI_MAP.put("sodium citrate", "SODIUM CITRATE");
        INCI_MAP.put("sodium palmitate", "SODIUM PALMITATE");
        INCI_MAP.put("tetrasodium edta", "TETRASODIUM EDTA");
        INCI_MAP.put("zinc chloride", "ZINC CHLORIDE");
        INCI_MAP.put("zinc perfume", "ZINC CHLORIDE");

        // ── Actives ──
        INCI_MAP.put("salicylic acid", "SALICYLIC ACID");
        INCI_MAP.put("zinc pyrithione", "ZINC PYRITHIONE");
        INCI_MAP.put("ketoconazole", "KETOCONAZOLE");
        INCI_MAP.put("glycolic acid", "GLYCOLIC ACID");
        INCI_MAP.put("retinol", "RETINOL");
        INCI_MAP.put("retinyl palmitate", "RETINYL PALMITATE");
        INCI_MAP.put("cimbazole", "CIMBAZOLE");
        INCI_MAP.put("climbazole", "CLIMBAZOLE");

        // ── Polymers / Conditioning ──
        INCI_MAP.put("polyquaternium-7", "POLYQUATERNIUM-7");
        INCI_MAP.put("polyquaternium-10", "POLYQUATERNIUM-10");
        INCI_MAP.put("guar hydroxypropyltrimonium chloride", "GUAR HYDROXYPROPYLTRIMONIUM CHLORIDE");
        INCI_MAP.put("carbomer", "CARBOMER");
        INCI_MAP.put("carborner", "CARBOMER");
        INCI_MAP.put("peg-90m", "PEG-90M");
        INCI_MAP.put("ppg-5-ceteth-20", "PPG-5-CETETH-20");
        INCI_MAP.put("laureth-4", "LAURETH-4");
        INCI_MAP.put("laureth", "LAURETH-4");
        INCI_MAP.put("cocodimonium hydroxypropyl hydrolyzed wheat protein", "COCODIMONIUM HYDROXYPROPYL HYDROLYZED WHEAT PROTEIN");
        INCI_MAP.put("2-oleamido-13-octadecane diol", "2-OLEAMIDO-1,3-OCTADECANE DIOL");
        INCI_MAP.put("2-oleamido-1,3-octadecane diol", "2-OLEAMIDO-1,3-OCTADECANE DIOL");
        INCI_MAP.put("titanium dioxide", "TITANIUM DIOXIDE");

        // ── Fragrance / Allergens ──
        INCI_MAP.put("fragrance", "PARFUM");
        INCI_MAP.put("parfum", "PARFUM");
        INCI_MAP.put("fragrance/parfum", "PARFUM");
        INCI_MAP.put("perfume", "PARFUM");
        INCI_MAP.put("limonene", "LIMONENE");
        INCI_MAP.put("linalool", "LINALOOL");
        INCI_MAP.put("hexyl cinnamal", "HEXYL CINNAMAL");
        INCI_MAP.put("benzyl salicylate", "BENZYL SALICYLATE");
        INCI_MAP.put("butylphenyl methylpropional", "BUTYLPHENYL METHYLPROPIONAL");

        // ── Colorants ──
        INCI_MAP.put("ci 19140", "CI 19140");
        INCI_MAP.put("ci 17200", "CI 17200");
        INCI_MAP.put("ci 16035", "CI 16035");
        INCI_MAP.put("quinazarine green ss", "QUINAZARINE GREEN SS");
        INCI_MAP.put("quinoline yellow ws", "QUINOLINE YELLOW WS");
        INCI_MAP.put("toney red", "TONEY RED");

        // ── Misc ──
        INCI_MAP.put("vitamin", "TOCOPHEROL");
    }

    public List<IngredientInput> extractFromText(String rawText) {
        log.info("Mock extraction from text: {} chars", rawText.length());
        String[] parts = rawText.split("[,;\\n]+");
        List<IngredientInput> results = new ArrayList<>();

        for (int i = 0; i < parts.length; i++) {
            String raw = parts[i].trim();
            if (raw.isEmpty()) continue;

            // Extract percentage concentration before cleaning
            Double extractedConcentration = null;
            java.util.regex.Pattern percentPattern = java.util.regex.Pattern.compile("(\\d+\\.?\\d*)\\s*%");
            java.util.regex.Matcher m = percentPattern.matcher(raw);
            if (m.find()) {
                try {
                    extractedConcentration = Double.parseDouble(m.group(1));
                } catch (NumberFormatException e) {
                    log.warn("Failed to parse concentration from: {}", m.group(1));
                }
            }

            // Remove concentration info like "(60 mg)" or "150mg" or "q.s."
            String cleaned = raw.replaceAll("\\(.*?\\)", "")
                                .replaceAll("\\d+\\s*mg", "")
                                .replaceAll("\\d+\\.?\\d*\\s*%", "")
                                .replaceAll("q\\.?s\\.?", "")
                                .replaceAll("[Pp]\\.\\s*[Ee]xt\\.?", "")
                                .replaceAll("[Ff]l\\.\\s*[Ee]xt\\.?", "")
                                .replaceAll("[Ff]r\\.\\s*[Ee]xt\\.?", "")
                                .replaceAll("[Ss]d\\.\\s*[Ee]x[tl]\\.?", "")
                                .replaceAll("[Ll]f\\.\\s*[Ee]xt\\.?", "")
                                .replaceAll("[Kk]r\\.\\s*[Ee]xt\\.?", "")
                                .replaceAll("[Rr]t\\.\\s*[Ee]xt\\.?", "")
                                .replaceAll("[Pp][Ii]\\.\\s*[Oo]il\\.?\\s*[Ee]xt\\.?", "")
                                .replaceAll("[Ll]f\\.\\s*[Oo]il\\.?\\s*[Ee]xt\\.?", "")
                                .replaceAll("\\bLinn\\b", "")
                                .replaceAll("\\bEach\\s+\\d+\\s*ml\\s+contains:?", "")
                                .replaceAll("\\s+", " ")
                                .trim();

            if (cleaned.isEmpty() || cleaned.length() < 2) continue;

            // Skip non-ingredient descriptive text
            String lower = cleaned.toLowerCase();
            if (lower.contains("note:") || lower.contains("nourishes") || lower.contains("conditions hair")
                || lower.contains("efficacy") || lower.contains("excipients:") || lower.contains("preservatives:")
                || lower.contains("colourants:") || lower.contains("shampoo base")) {
                continue;
            }

            // Remove "Excipients:", "Preservatives:" etc. prefix
            cleaned = cleaned.replaceAll("^[A-Za-z]+:\\s*", "").trim();
            if (cleaned.isEmpty()) continue;

            String normalized = normalizeInciName(cleaned);
            double confidence = INCI_MAP.containsKey(cleaned.toLowerCase().trim()) ? 0.95 : 0.70;

            IngredientInput ii = new IngredientInput();
            ii.setRawName(raw.trim());
            ii.setInciName(normalized);
            ii.setPosition(results.size() + 1);
            ii.setConfidenceScore(confidence);
            ii.setConcentration(extractedConcentration);
            results.add(ii);
        }
        return results;
    }

    public List<IngredientInput> extractFromImage(String base64Image) {
        log.info("Mock extraction from image (base64 length: {})", base64Image.length());
        String mockOcr = "Aqua, Sodium Laureth Sulfate, Cocamidopropyl Betaine, "
                + "Glycerin, Sodium Chloride, Parfum, Dimethicone, Panthenol, "
                + "Citric Acid, Phenoxyethanol, Methylparaben";
        return extractFromText(mockOcr);
    }

    private String normalizeInciName(String raw) {
        String key = raw.toLowerCase().trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^a-z0-9\\s,/'-]", "");
        return INCI_MAP.getOrDefault(key, raw.toUpperCase().trim());
    }
}
