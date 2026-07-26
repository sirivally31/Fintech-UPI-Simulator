package com.example.demo.repository;

import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Notifications.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Notification> findByIdAndUserId(UUID id, Long userId);

    long countByUserIdAndStatusNot(Long userId, NotificationStatus status);

    List<Notification> findByUserIdAndStatusNot(Long userId, NotificationStatus status);
}
