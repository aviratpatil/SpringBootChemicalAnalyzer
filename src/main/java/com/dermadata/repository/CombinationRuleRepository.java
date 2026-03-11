package com.dermadata.repository;

import com.dermadata.entity.CombinationRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CombinationRuleRepository extends JpaRepository<CombinationRule, Long> {

    List<CombinationRule> findByIngredientAIgnoreCaseAndIngredientBIgnoreCase(
            String ingredientA, String ingredientB);

    @Query("SELECT c FROM CombinationRule c WHERE " +
           "LOWER(c.ingredientA) IN :names AND LOWER(c.ingredientB) IN :names")
    List<CombinationRule> findRulesForIngredients(@Param("names") List<String> names);

    @Query("SELECT c FROM CombinationRule c WHERE " +
           "LOWER(c.ingredientA) = LOWER(:name) OR LOWER(c.ingredientB) = LOWER(:name)")
    List<CombinationRule> findRulesInvolvingIngredient(@Param("name") String ingredientName);
}
