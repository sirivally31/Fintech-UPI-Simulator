package com.example.demo.service.impl;

import com.example.demo.dto.NotificationRequest;
import com.example.demo.dto.NotificationResponse;
import com.example.demo.entity.*;
import com.example.demo.events.TransactionCompletedEvent;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private User user;
    private Notification notification;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUpiId("john@upi");

        notificationId = UUID.randomUUID();
        notification = new Notification(
                notificationId, 1L, NotificationType.PAYMENT_SUCCESS, NotificationChannel.IN_APP,
                "Payment Sent", "Rs. 500 transferred to Alice", NotificationStatus.SENT, 1,
                "TXN1001", LocalDateTime.now(), LocalDateTime.now(), null
        );

        Authentication auth = mock(Authentication.class);
        lenient().when(auth.getName()).thenReturn("john@upi");
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Create Notification - Success")
    void testCreateNotification_Success() {
        NotificationRequest req = new NotificationRequest(
                1L, NotificationType.PAYMENT_SUCCESS, NotificationChannel.IN_APP,
                "Payment Sent", "Rs. 500 transferred", 1, "TXN1001"
        );

        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> {
            Notification n = i.getArgument(0);
            n.setId(notificationId);
            return n;
        });

        NotificationResponse response = notificationService.createNotification(req);

        assertNotNull(response);
        assertEquals("Payment Sent", response.getTitle());
        assertEquals(NotificationStatus.SENT, response.getStatus());

        verify(redisCacheService).delete("notification:unread:1");
        verify(outboxService).saveOutboxEvent(any(), eq("NOTIFICATION"), eq(1L), eq("NOTIFICATION_SENT"), anyString(), any());
    }

    @Test
    @DisplayName("Mark As Read - Success")
    void testMarkAsRead_Success() {
        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(user));
        when(notificationRepository.findByIdAndUserId(notificationId, 1L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        notificationService.markAsRead(notificationId);

        assertEquals(NotificationStatus.READ, notification.getStatus());
        assertNotNull(notification.getReadAt());
        verify(redisCacheService).delete("notification:unread:1");
    }

    @Test
    @DisplayName("Get Unread Count - Cache Miss and Cache Hit")
    void testGetUnreadCount() {
        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(user));
        when(redisCacheService.find("notification:unread:1", Long.class)).thenReturn(null);
        when(notificationRepository.countByUserIdAndStatusNot(1L, NotificationStatus.READ)).thenReturn(3L);

        long count = notificationService.getUnreadCount();

        assertEquals(3L, count);
        verify(redisCacheService).save(eq("notification:unread:1"), eq(3L), anyLong(), any());
    }

    @Test
    @DisplayName("Notify Transaction Completed - Triggers Payer and Payee Notifications")
    void testNotifyTransactionCompleted() {
        User sender = new User();
        sender.setId(1L);
        sender.setUpiId("john@upi");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUpiId("alice@upi");

        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(sender));
        when(userRepository.findByUpiId("alice@upi")).thenReturn(Optional.of(receiver));
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArgument(0));

        TransactionCompletedEvent event = TransactionCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("TRANSACTION_COMPLETED")
                .correlationId(UUID.randomUUID().toString())
                .transactionReference("TXN9999")
                .senderUpiId("john@upi")
                .receiverUpiId("alice@upi")
                .amount(new BigDecimal("250.00"))
                .status("SUCCESS")
                .build();

        notificationService.notifyTransactionCompleted(event);

        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
}
