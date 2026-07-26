package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.Merchant;
import com.example.demo.entity.Notification;
import com.example.demo.entity.NotificationStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.*;
import com.example.demo.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminDashboardServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AutoPayRepository autoPayRepository;

    @Mock
    private FraudRuleRepository fraudRuleRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @InjectMocks
    private AdminDashboardServiceImpl adminDashboardService;

    private User user;
    private Merchant merchant;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setUpiId("john@upi");

        merchant = new Merchant();
        merchant.setId(UUID.randomUUID());
        merchant.setMerchantName("SuperMart");
        merchant.setBusinessName("SuperMart Retail");
        merchant.setActive(true);
    }

    @Test
    @DisplayName("Get Dashboard Summary - Cache Miss and Full Aggregation")
    void testGetDashboardSummary_CacheMiss() {
        when(redisCacheService.find("admin:dashboard:summary", DashboardSummaryResponse.class)).thenReturn(null);
        when(userRepository.count()).thenReturn(10L);
        when(bankAccountRepository.count()).thenReturn(15L);
        when(merchantRepository.count()).thenReturn(5L);
        when(transactionRepository.findAll()).thenReturn(Collections.emptyList());
        when(autoPayRepository.count()).thenReturn(3L);
        when(fraudRuleRepository.count()).thenReturn(8L);
        when(notificationRepository.count()).thenReturn(25L);

        DashboardSummaryResponse summary = adminDashboardService.getDashboardSummary();

        assertNotNull(summary);
        assertEquals(10L, summary.getTotalUsers());
        assertEquals(15L, summary.getTotalBankAccounts());
        assertEquals(5L, summary.getTotalMerchants());

        verify(redisCacheService).save(eq("admin:dashboard:summary"), any(DashboardSummaryResponse.class), anyLong(), any());
    }

    @Test
    @DisplayName("Get System Health - Returns All UP")
    void testGetSystemHealth() {
        SystemHealthResponse health = adminDashboardService.getSystemHealth();

        assertNotNull(health);
        assertEquals("UP", health.getDatabaseHealth());
        assertEquals("UP", health.getRedisHealth());
        assertEquals("UP", health.getKafkaHealth());
    }

    @Test
    @DisplayName("Update User Status - Success")
    void testUpdateUserStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User updated = adminDashboardService.updateUserStatus(1L, "disable");

        assertNotNull(updated);
        assertEquals("John Doe", updated.getName());
        verify(redisCacheService).delete("admin:dashboard:summary");
    }

    @Test
    @DisplayName("Update Merchant Status - Success")
    void testUpdateMerchantStatus() {
        when(merchantRepository.findAll()).thenReturn(List.of(merchant));
        when(merchantRepository.save(any(Merchant.class))).thenReturn(merchant);

        Merchant updated = adminDashboardService.updateMerchantStatus(1L, "suspend");

        assertNotNull(updated);
        assertFalse(updated.getActive());
        verify(redisCacheService).delete("admin:dashboard:summary");
    }

    @Test
    @DisplayName("Resend Notification - Success")
    void testResendNotification() {
        UUID notifId = UUID.randomUUID();
        Notification notif = new Notification();
        notif.setId(notifId);
        notif.setStatus(NotificationStatus.FAILED);

        when(notificationRepository.findById(notifId)).thenReturn(Optional.of(notif));
        when(notificationRepository.save(any(Notification.class))).thenReturn(notif);

        adminDashboardService.resendNotification(notifId);

        assertEquals(NotificationStatus.SENT, notif.getStatus());
        verify(redisCacheService).delete("admin:dashboard:summary");
    }
}
