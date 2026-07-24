package com.example.demo.repository;

import com.example.demo.entity.OutboxEvent;
import com.example.demo.entity.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for managing OutboxEvent entity persistence and status queries.
 */
@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    Optional<OutboxEvent> findByEventId(UUID eventId);

    boolean existsByEventId(UUID eventId);
}
