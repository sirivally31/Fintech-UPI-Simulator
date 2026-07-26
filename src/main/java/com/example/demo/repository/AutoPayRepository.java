package com.example.demo.repository;

import com.example.demo.entity.AutoPay;
import com.example.demo.entity.AutoPayStatus;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for AutoPay mandates.
 */
@Repository
public interface AutoPayRepository extends JpaRepository<AutoPay, UUID> {

    List<AutoPay> findByOwner(User owner);

    Optional<AutoPay> findByIdAndOwner(UUID id, User owner);

    Optional<AutoPay> findByMandateReference(String mandateReference);

    boolean existsByMandateReference(String mandateReference);

    List<AutoPay> findByStatusAndActiveTrueAndNextExecutionTimeLessThanEqual(AutoPayStatus status, LocalDateTime now);
}
