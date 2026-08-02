package com.example.demo.repository;

import com.example.demo.entity.AuditAction;
import com.example.demo.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for managing and querying AuditLog entities with Specification support.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long>, JpaSpecificationExecutor<AuditLog> {

    Optional<AuditLog> findByEventId(String eventId);

    long countBySuccess(Boolean success);

    long countByAction(AuditAction action);

    List<AuditLog> findTop20ByOrderByTimestampDesc();

    @Query("SELECT a.module, COUNT(a) FROM AuditLog a GROUP BY a.module")
    List<Object[]> countLogEntriesByModule();

    @Query("SELECT a FROM AuditLog a WHERE " +
            "(:username IS NULL OR a.username = :username) AND " +
            "(:module IS NULL OR a.module = :module) AND " +
            "(:action IS NULL OR a.action = :action) AND " +
            "(:success IS NULL OR a.success = :success) AND " +
            "(:startDate IS NULL OR a.timestamp >= :startDate) AND " +
            "(:endDate IS NULL OR a.timestamp <= :endDate)")
    Page<AuditLog> searchAuditLogs(
            @Param("username") String username,
            @Param("module") String module,
            @Param("action") AuditAction action,
            @Param("success") Boolean success,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
}
