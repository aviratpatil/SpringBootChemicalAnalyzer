package com.dermadata.controller;

import com.dermadata.entity.IngredientRegulation;
import com.dermadata.repository.IngredientRegulationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ingredients")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class IngredientController {

    private final IngredientRegulationRepository repository;

    public IngredientController(IngredientRegulationRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<IngredientRegulation>> getAllIngredients() {
        return ResponseEntity.ok(repository.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<List<IngredientRegulation>> searchIngredients(@RequestParam String query) {
        return ResponseEntity.ok(repository.searchByName(query));
    }

    @GetMapping("/prohibited")
    public ResponseEntity<List<IngredientRegulation>> getProhibited() {
        return ResponseEntity.ok(repository.findByProhibitedTrue());
    }

    @GetMapping("/restricted")
    public ResponseEntity<List<IngredientRegulation>> getRestricted() {
        return ResponseEntity.ok(repository.findByRestrictedTrue());
    }

    @GetMapping("/{inciName}")
    public ResponseEntity<IngredientRegulation> getByName(@PathVariable String inciName) {
        return repository.findByInciNameIgnoreCase(inciName)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
