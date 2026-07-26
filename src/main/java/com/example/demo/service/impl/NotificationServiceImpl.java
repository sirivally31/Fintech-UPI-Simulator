package com.example.demo.service.impl;

import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.entity.*;
import com.example.demo.events.PaymentRequestCreatedEvent;
import com.example.demo.events.TransactionCompletedEvent;
import com.example.demo.exception.NotificationNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Enterprise Production-ready NotificationService implementation handling
 * real-time user communications, Kafka event logging, Outbox integration, and Redis caching.
 */
@Service
public class NotificationServiceImpl implements NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceImpl.class);

    private static final String CACHE_UNREAD_PREFIX = "notification:unread:";
    private static final String CACHE_USER_PREFIX = "notification:user:";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;
    private final OutboxService outboxService;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserRepository userRepository,
                                   RedisCacheService redisCacheService,
                                   OutboxService outboxService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.redisCacheService = redisCacheService;
        this.outboxService = outboxService;
    }

    private User getCurrentOwner() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUpiId(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    @Override
    public void notifyTransactionCompleted(TransactionCompletedEvent event) {
        log.info("[NOTIFICATION SIMULATION] Transaction Completed Alert | " +
                        "reference: [{}] | sender: [{}] | receiver: [{}] | amount: [{}] | " +
                        "status: [{}] | eventTime: [{}] | correlationId: [{}]",
                event.getTransactionReference(),
                event.getSenderUpiId(),
                event.getReceiverUpiId(),
                event.getAmount(),
                event.getStatus(),
                event.getEventTime(),
                event.getCorrelationId());

        userRepository.findByUpiId(event.getSenderUpiId()).ifPresent(sender -> {
            NotificationRequest req = new NotificationRequest(
                    sender.getId(),
                    NotificationType.PAYMENT_SUCCESS,
                    NotificationChannel.IN_APP,
                    "Payment Sent Successfully",
                    "Rs. " + event.getAmount() + " transferred to " + event.getReceiverUpiId() + ".",
                    1,
                    event.getTransactionReference()
            );
            createNotification(req);
        });

        userRepository.findByUpiId(event.getReceiverUpiId()).ifPresent(receiver -> {
            NotificationRequest req = new NotificationRequest(
                    receiver.getId(),
                    NotificationType.PAYMENT_SUCCESS,
                    NotificationChannel.IN_APP,
                    "Payment Received",
                    "Rs. " + event.getAmount() + " received from " + event.getSenderUpiId() + ".",
                    1,
                    event.getTransactionReference()
            );
            createNotification(req);
        });
    }

    @Override
    public void notifyPaymentRequestCreated(PaymentRequestCreatedEvent event) {
        log.info("[NOTIFICATION SIMULATION] Payment Request Created Alert | " +
                        "reference: [{}] | requester: [{}] | payer: [{}] | amount: [{}] | " +
                        "note: [{}] | expiresAt: [{}] | eventTime: [{}] | correlationId: [{}]",
                event.getRequestReference(),
                event.getReceiverUpiId(),
                event.getSenderUpiId(),
                event.getAmount(),
                event.getNote(),
                event.getExpiresAt(),
                event.getEventTime(),
                event.getCorrelationId());

        userRepository.findByUpiId(event.getSenderUpiId()).ifPresent(payer -> {
            NotificationRequest req = new NotificationRequest(
                    payer.getId(),
                    NotificationType.SYSTEM,
                    NotificationChannel.IN_APP,
                    "Payment Request Received",
                    event.getReceiverUpiId() + " requested Rs. " + event.getAmount() + ".",
                    2,
                    event.getRequestReference()
            );
            createNotification(req);
        });
    }

    @Override
    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        log.info("Creating notification for userId [{}] title [{}]", request.getUserId(), request.getTitle());

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType(request.getType());
        notification.setChannel(request.getChannel());
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        notification.setReferenceId(request.getReferenceId());
        notification.setStatus(NotificationStatus.SENT);
        notification.setSentAt(LocalDateTime.now());

        Notification saved = notificationRepository.save(notification);
        NotificationResponse response = mapToResponse(saved);

        evictUserCache(request.getUserId());

        String correlationId = UUID.randomUUID().toString();
        outboxService.saveOutboxEvent(UUID.randomUUID(), "NOTIFICATION", request.getUserId(), "NOTIFICATION_SENT", correlationId, response);

        return response;
    }

    @Override
    @Transactional
    public void markAsRead(UUID id) {
        User owner = getCurrentOwner();
        log.info("Marking notification ID [{}] as read for user [{}]", id, owner.getId());

        Notification notification = notificationRepository.findByIdAndUserId(id, owner.getId())
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        notification.setStatus(NotificationStatus.READ);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);

        evictUserCache(owner.getId());
    }

    @Override
    @Transactional
    public void markAllAsRead() {
        User owner = getCurrentOwner();
        log.info("Marking all notifications as read for user [{}]", owner.getId());

        List<Notification> unreadList = notificationRepository.findByUserIdAndStatusNot(owner.getId(), NotificationStatus.READ);
        LocalDateTime now = LocalDateTime.now();

        for (Notification notification : unreadList) {
            notification.setStatus(NotificationStatus.READ);
            notification.setReadAt(now);
        }
        notificationRepository.saveAll(unreadList);

        evictUserCache(owner.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getNotifications() {
        User owner = getCurrentOwner();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(owner.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount() {
        User owner = getCurrentOwner();
        String unreadKey = CACHE_UNREAD_PREFIX + owner.getId();

        Long cachedCount = redisCacheService.find(unreadKey, Long.class);
        if (cachedCount != null) {
            return cachedCount;
        }

        long unread = notificationRepository.countByUserIdAndStatusNot(owner.getId(), NotificationStatus.READ);
        redisCacheService.save(unreadKey, unread, 1, TimeUnit.HOURS);
        return unread;
    }

    @Override
    @Transactional
    public void deleteNotification(UUID id) {
        User owner = getCurrentOwner();
        log.info("Deleting notification ID [{}] for user [{}]", id, owner.getId());

        Notification notification = notificationRepository.findByIdAndUserId(id, owner.getId())
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found with ID: " + id));

        notificationRepository.delete(notification);
        evictUserCache(owner.getId());
    }

    private void evictUserCache(Long userId) {
        redisCacheService.delete(CACHE_UNREAD_PREFIX + userId);
        redisCacheService.delete(CACHE_USER_PREFIX + userId);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getUserId(),
                notification.getType(),
                notification.getChannel(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getStatus(),
                notification.getPriority(),
                notification.getReferenceId(),
                notification.getCreatedAt(),
                notification.getSentAt(),
                notification.getReadAt()
        );
    }
}
