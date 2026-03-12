package com.dermadata.service;

import com.dermadata.dto.IngredientInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExtractionServiceTest {

    private ExtractionService extractionService;

    @BeforeEach
    void setUp() {
        extractionService = new ExtractionService();
    }

    @Test
    void whenThreeInputsResolveToSameInci_thenOnlyOneResultIsReturned() {
        // Arrange — three IngredientInput objects all with the same INCI name.
        // This simulates "Methylparaben, Paraben (methyl), Methylparaben"
        // all resolving to the canonical INCI "METHYLPARABEN".
        IngredientInput a = new IngredientInput();
        a.setRawName("Methylparaben");
        a.setInciName("METHYLPARABEN");

        IngredientInput b = new IngredientInput();
        b.setRawName("Paraben methyl");
        b.setInciName("METHYLPARABEN");   // same INCI, different raw label

        IngredientInput c = new IngredientInput();
        c.setRawName("Methylparaben");
        c.setInciName("METHYLPARABEN");   // exact duplicate of a

        List<IngredientInput> inputs = List.of(a, b, c);

        // Act
        ExtractionService.DeduplicationResult result =
                extractionService.deduplicateIngredients(inputs);

        // Assert
        assertEquals(1, result.getDeduplicated().size(),
                "Three inputs sharing the same INCI should collapse to exactly 1 entry");

        assertEquals(2, result.getDuplicatesRemoved(),
                "Two duplicate entries should have been removed");

        List<String> aliases = result.getDeduplicated().get(0).getAliases();
        assertEquals(3, aliases.size(),
                "The surviving entry should carry all 3 raw names as aliases");

        assertTrue(aliases.contains("Methylparaben"), "aliases should contain 'Methylparaben'");
        assertTrue(aliases.contains("Paraben methyl"), "aliases should contain 'Paraben methyl'");
    }

    @Test
    void whenInputsHaveDistinctInciNames_thenNothingIsRemoved() {
        IngredientInput a = new IngredientInput();
        a.setRawName("Aqua");
        a.setInciName("AQUA");

        IngredientInput b = new IngredientInput();
        b.setRawName("Glycerin");
        b.setInciName("GLYCERIN");

        ExtractionService.DeduplicationResult result =
                extractionService.deduplicateIngredients(List.of(a, b));

        assertEquals(2, result.getDeduplicated().size());
        assertEquals(0, result.getDuplicatesRemoved());
    }
}
