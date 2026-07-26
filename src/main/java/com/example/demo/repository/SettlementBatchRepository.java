package com.example.demo.repository;

import com.example.demo.entity.SettlementBatch;
import com.example.demo.entity.SettlementStatus;
import com.example.demo.entity.SettlementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for SettlementBatches.
 */
@Repository
public interface SettlementBatchRepository extends JpaRepository<SettlementBatch, UUID> {

    Optional<SettlementBatch> findByBatchReference(String batchReference);

    boolean existsByBatchReference(String batchReference);

    List<SettlementBatch> findByStatus(SettlementStatus status);

    List<SettlementBatch> findByType(SettlementType type);
}
