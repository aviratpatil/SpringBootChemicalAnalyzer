package com.dermadata.repository;

import com.dermadata.entity.IngredientRegulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRegulationRepository extends JpaRepository<IngredientRegulation, String> {

    Optional<IngredientRegulation> findByInciNameIgnoreCase(String inciName);

    List<IngredientRegulation> findByProhibitedTrue();

    List<IngredientRegulation> findByRestrictedTrue();

    @Query("SELECT i FROM IngredientRegulation i WHERE LOWER(i.inciName) IN :names")
    List<IngredientRegulation> findAllByInciNames(@Param("names") List<String> names);

    @Query("SELECT i FROM IngredientRegulation i WHERE LOWER(i.inciName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<IngredientRegulation> searchByName(@Param("query") String query);
}
