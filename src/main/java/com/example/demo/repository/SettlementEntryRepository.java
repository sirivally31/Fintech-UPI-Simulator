package com.example.demo.repository;

import com.example.demo.entity.SettlementBatch;
import com.example.demo.entity.SettlementEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data JPA Repository for SettlementEntries.
 */
@Repository
public interface SettlementEntryRepository extends JpaRepository<SettlementEntry, UUID> {

    List<SettlementEntry> findByBatch(SettlementBatch batch);

    List<SettlementEntry> findByTransactionReference(String transactionReference);
}
