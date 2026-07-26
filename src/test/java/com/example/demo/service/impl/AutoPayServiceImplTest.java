package com.example.demo.service.impl;

import com.example.demo.dto.AutoPayResponse;
import com.example.demo.dto.CreateAutoPayRequest;
import com.example.demo.dto.UpdateAutoPayRequest;
import com.example.demo.entity.*;
import com.example.demo.exception.AutoPayValidationException;
import com.example.demo.metrics.BusinessMetricsService;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.*;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AutoPayServiceImplTest {

    @Mock
    private AutoPayRepository autoPayRepository;

    @Mock
    private BeneficiaryRepository beneficiaryRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UpiIdRepository upiIdRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private BusinessMetricsService businessMetricsService;

    @InjectMocks
    private AutoPayServiceImpl autoPayService;

    private User owner;
    private Beneficiary beneficiary;
    private AutoPay autoPay;
    private UUID autoPayId;
    private UUID beneficiaryId;
    private BankAccount ownerAccount;
    private BankAccount beneficiaryAccount;
    private UpiId beneficiaryUpi;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUpiId("john@upi");

        beneficiaryId = UUID.randomUUID();
        beneficiary = new Beneficiary(beneficiaryId, owner, "Alice Smith", "alice@upi", "Alice Work", false, true, LocalDateTime.now(), LocalDateTime.now());

        autoPayId = UUID.randomUUID();
        autoPay = new AutoPay(autoPayId, owner, beneficiary, "MN202607261001", new BigDecimal("500.00"),
                AutoPayFrequency.MONTHLY, AutoPayStatus.ACTIVE, LocalDate.now(), LocalDate.now().plusYears(1),
                LocalDateTime.now().minusMinutes(1), 0, true, LocalDateTime.now(), LocalDateTime.now());

        ownerAccount = new BankAccount();
        ownerAccount.setId(10L);
        ownerAccount.setAccountNumber("ACC-OWNER-1");
        ownerAccount.setBalance(new BigDecimal("2000.00"));
        ownerAccount.setStatus(AccountStatus.ACTIVE);

        beneficiaryAccount = new BankAccount();
        beneficiaryAccount.setId(20L);
        beneficiaryAccount.setAccountNumber("ACC-BEN-2");
        beneficiaryAccount.setBalance(new BigDecimal("1000.00"));
        beneficiaryAccount.setStatus(AccountStatus.ACTIVE);

        beneficiaryUpi = new UpiId();
        beneficiaryUpi.setId(100L);
        beneficiaryUpi.setUpiId("alice@upi");
        beneficiaryUpi.setBankAccount(beneficiaryAccount);
        beneficiaryUpi.setStatus(UpiStatus.ACTIVE);

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
    @DisplayName("Create AutoPay - Successful Mandate Creation")
    void testCreateAutoPay_Success() {
        CreateAutoPayRequest request = new CreateAutoPayRequest(beneficiaryId, new BigDecimal("500.00"), AutoPayFrequency.MONTHLY, LocalDate.now(), LocalDate.now().plusYears(1));

        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));
        when(beneficiaryRepository.findByIdAndOwner(beneficiaryId, owner)).thenReturn(Optional.of(beneficiary));
        when(autoPayRepository.save(any(AutoPay.class))).thenAnswer(i -> {
            AutoPay a = i.getArgument(0);
            a.setId(autoPayId);
            return a;
        });

        AutoPayResponse response = autoPayService.createAutoPay(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals(AutoPayFrequency.MONTHLY, response.getFrequency());

        verify(outboxService).saveOutboxEvent(any(), eq("AUTOPAY"), anyLong(), eq("AUTOPAY_CREATED"), anyString(), any());
        verify(eventPublisher).publishAutoPayCreated(any());
    }

    @Test
    @DisplayName("Create AutoPay - Invalid Dates Throws Exception")
    void testCreateAutoPay_InvalidDates_ThrowsException() {
        CreateAutoPayRequest request = new CreateAutoPayRequest(beneficiaryId, new BigDecimal("500.00"), AutoPayFrequency.MONTHLY, LocalDate.now().plusDays(5), LocalDate.now());

        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));

        assertThrows(AutoPayValidationException.class, () -> autoPayService.createAutoPay(request));
        verify(autoPayRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pause & Resume AutoPay - Success")
    void testPauseAndResumeAutoPay_Success() {
        when(userRepository.findByUpiId("john@upi")).thenReturn(Optional.of(owner));
        when(autoPayRepository.findByIdAndOwner(autoPayId, owner)).thenReturn(Optional.of(autoPay));
        when(autoPayRepository.save(any(AutoPay.class))).thenReturn(autoPay);

        AutoPayResponse pausedResp = autoPayService.pauseAutoPay(autoPayId);
        assertEquals(AutoPayStatus.PAUSED, autoPay.getStatus());

        AutoPayResponse resumedResp = autoPayService.resumeAutoPay(autoPayId);
        assertEquals(AutoPayStatus.ACTIVE, autoPay.getStatus());
    }

    @Test
    @DisplayName("Execute Due AutoPay - Successful Transfer")
    void testExecuteDueAutoPay_Success() {
        when(bankAccountRepository.findAllByUser(owner)).thenReturn(List.of(ownerAccount));
        when(upiIdRepository.findByUpiId("alice@upi")).thenReturn(Optional.of(beneficiaryUpi));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(101L);
            return t;
        });

        autoPayService.executeDueAutoPay(autoPay);

        assertEquals(new BigDecimal("1500.00"), ownerAccount.getBalance());
        assertEquals(new BigDecimal("1500.00"), beneficiaryAccount.getBalance());
        assertEquals(0, autoPay.getRetryCount());

        verify(bankAccountRepository).save(ownerAccount);
        verify(bankAccountRepository).save(beneficiaryAccount);
        verify(outboxService).saveOutboxEvent(any(), eq("AUTOPAY"), anyLong(), eq("AUTOPAY_EXECUTED"), anyString(), any());
        verify(eventPublisher).publishAutoPayExecuted(any());
    }

    @Test
    @DisplayName("Execute Due AutoPay - Insufficient Funds Increments Retry")
    void testExecuteDueAutoPay_InsufficientBalance_TriggersRetry() {
        ownerAccount.setBalance(new BigDecimal("100.00")); // Less than mandate amount 500.00

        when(bankAccountRepository.findAllByUser(owner)).thenReturn(List.of(ownerAccount));
        when(upiIdRepository.findByUpiId("alice@upi")).thenReturn(Optional.of(beneficiaryUpi));

        autoPayService.executeDueAutoPay(autoPay);

        assertEquals(1, autoPay.getRetryCount());
        verify(outboxService).saveOutboxEvent(any(), eq("AUTOPAY"), anyLong(), eq("AUTOPAY_FAILED"), anyString(), any());
        verify(eventPublisher).publishAutoPayFailed(any());
    }
}
