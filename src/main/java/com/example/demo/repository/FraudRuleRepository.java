package com.example.demo.repository;

import com.example.demo.entity.FraudRule;
import com.example.demo.entity.FraudRuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Fraud Rules.
 */
@Repository
public interface FraudRuleRepository extends JpaRepository<FraudRule, UUID> {

    List<FraudRule> findByEnabledTrueOrderByPriorityAsc();

    Optional<FraudRule> findByRuleName(String ruleName);

    boolean existsByRuleName(String ruleName);

    List<FraudRule> findByType(FraudRuleType type);
}
