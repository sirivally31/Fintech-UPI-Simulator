package com.example.demo.service;

import com.example.demo.dto.SendMoneyRequest;
import com.example.demo.dto.TransactionHistoryResponse;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.dto.TransactionSummaryResponse;
import com.example.demo.dto.VerifyUpiPinRequest;
import com.example.demo.entity.AccountStatus;
import com.example.demo.entity.BankAccount;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.TransactionStatus;
import com.example.demo.entity.UpiId;
import com.example.demo.entity.UpiStatus;
import com.example.demo.entity.User;
import com.example.demo.exception.UnauthorizedAccountAccessException;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

/**
 * Enterprise Unit Tests for TransactionServiceImpl.
 * Verifies financial logic, balance debits/credits, security checks, and event publishing.
 */
@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UpiIdRepository upiIdRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UpiPinService upiPinService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private com.example.demo.metrics.BusinessMetricsService businessMetricsService;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User senderUser;
    private User receiverUser;
    private BankAccount senderAccount;
    private BankAccount receiverAccount;
    private UpiId senderUpiId;
    private UpiId receiverUpiId;

    @BeforeEach
    void setUp() {
        // Setup Security Context
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("sender@upi");
        SecurityContextHolder.setContext(securityContext);

        // Domain models setup
        senderUser = new User();
        senderUser.setId(1L);
        senderUser.setUpiId("sender@upi");

        receiverUser = new User();
        receiverUser.setId(2L);
        receiverUser.setUpiId("receiver@upi");

        senderAccount = new BankAccount();
        senderAccount.setId(10L);
        senderAccount.setBalance(new BigDecimal("1000.00"));
        senderAccount.setStatus(AccountStatus.ACTIVE);
        senderAccount.setUser(senderUser);

        receiverAccount = new BankAccount();
        receiverAccount.setId(20L);
        receiverAccount.setBalance(new BigDecimal("500.00"));
        receiverAccount.setStatus(AccountStatus.ACTIVE);
        receiverAccount.setUser(receiverUser);

        senderUpiId = new UpiId();
        senderUpiId.setId(100L);
        senderUpiId.setUpiId("sender@upi");
        senderUpiId.setStatus(UpiStatus.ACTIVE);
        senderUpiId.setBankAccount(senderAccount);

        receiverUpiId = new UpiId();
        receiverUpiId.setId(200L);
        receiverUpiId.setUpiId("receiver@upi");
        receiverUpiId.setStatus(UpiStatus.ACTIVE);
        receiverUpiId.setBankAccount(receiverAccount);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should transfer money successfully when all validations pass")
    void shouldTransferMoneySuccessfully() {
        // Arrange
        SendMoneyRequest request = new SendMoneyRequest();
        request.setSenderBankAccountId(10L);
        request.setReceiverUpiId("receiver@upi");
        request.setAmount(new BigDecimal("200.00"));
        request.setUpiPin("1234");
        request.setRemarks("Dinner payment");

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findByIdAndUser(10L, senderUser)).thenReturn(Optional.of(senderAccount));
        when(upiIdRepository.findByUpiId("receiver@upi")).thenReturn(Optional.of(receiverUpiId));
        when(upiPinService.verifyUpiPin(any(VerifyUpiPinRequest.class))).thenReturn(true);
        when(transactionRepository.existsByTransactionReference(anyString())).thenReturn(false);
        when(upiIdRepository.findByBankAccountAndIsPrimaryTrue(senderAccount)).thenReturn(Optional.of(senderUpiId));

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction txn = invocation.getArgument(0);
            txn.setId(99L);
            txn.setCreatedAt(LocalDateTime.now());
            return txn;
        });

        // Act
        TransactionResponse response = transactionService.sendMoney(request);

        // Assert
        assertNotNull(response);
        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals(new BigDecimal("200.00"), response.getAmount());
        assertEquals("sender@upi", response.getSenderUpiId());
        assertEquals("receiver@upi", response.getReceiverUpiId());

        // Verify balance updates (Sender: 1000 - 200 = 800, Receiver: 500 + 200 = 700)
        assertEquals(new BigDecimal("800.00"), senderAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), receiverAccount.getBalance());

        verify(bankAccountRepository, times(2)).save(any(BankAccount.class));
        verify(transactionRepository).save(any(Transaction.class));

        // Verify outbox transaction event published
        verify(outboxService).saveOutboxEvent(
                any(UUID.class),
                eq("TRANSACTION"),
                eq(99L),
                eq("TRANSACTION_COMPLETED"),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("Should throw exception when balance is insufficient")
    void shouldThrowExceptionWhenBalanceIsInsufficient() {
        // Arrange
        SendMoneyRequest request = new SendMoneyRequest();
        request.setSenderBankAccountId(10L);
        request.setReceiverUpiId("receiver@upi");
        request.setAmount(new BigDecimal("5000.00")); // Balance is 1000.00
        request.setUpiPin("1234");

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findByIdAndUser(10L, senderUser)).thenReturn(Optional.of(senderAccount));
        when(upiIdRepository.findByUpiId("receiver@upi")).thenReturn(Optional.of(receiverUpiId));
        when(upiPinService.verifyUpiPin(any(VerifyUpiPinRequest.class))).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transactionService.sendMoney(request)
        );

        assertEquals("Insufficient balance", exception.getMessage());
        verify(bankAccountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when sender account not found or unauthorized")
    void shouldThrowExceptionWhenSenderAccountNotFound() {
        // Arrange
        SendMoneyRequest request = new SendMoneyRequest();
        request.setSenderBankAccountId(999L);

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findByIdAndUser(999L, senderUser)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedAccountAccessException.class, () ->
                transactionService.sendMoney(request)
        );

        verifyNoInteractions(upiIdRepository, upiPinService, transactionRepository);
    }

    @Test
    @DisplayName("Should throw exception when receiver UPI ID not found")
    void shouldThrowExceptionWhenReceiverUpiIdNotFound() {
        // Arrange
        SendMoneyRequest request = new SendMoneyRequest();
        request.setSenderBankAccountId(10L);
        request.setReceiverUpiId("invalid@upi");

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findByIdAndUser(10L, senderUser)).thenReturn(Optional.of(senderAccount));
        when(upiIdRepository.findByUpiId("invalid@upi")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transactionService.sendMoney(request)
        );

        assertEquals("Receiver UPI ID not found", exception.getMessage());
        verifyNoInteractions(upiPinService, transactionRepository);
    }

    @Test
    @DisplayName("Should throw exception when UPI PIN is invalid")
    void shouldThrowExceptionWhenUpiPinIsInvalid() {
        // Arrange
        SendMoneyRequest request = new SendMoneyRequest();
        request.setSenderBankAccountId(10L);
        request.setReceiverUpiId("receiver@upi");
        request.setUpiPin("9999"); // Wrong PIN

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findByIdAndUser(10L, senderUser)).thenReturn(Optional.of(senderAccount));
        when(upiIdRepository.findByUpiId("receiver@upi")).thenReturn(Optional.of(receiverUpiId));
        when(upiPinService.verifyUpiPin(any(VerifyUpiPinRequest.class))).thenReturn(false);

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () ->
                transactionService.sendMoney(request)
        );

        assertEquals("Invalid UPI PIN", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when bank account is inactive")
    void shouldThrowExceptionWhenBankAccountIsInactive() {
        // Arrange
        senderAccount.setStatus(AccountStatus.BLOCKED);

        SendMoneyRequest request = new SendMoneyRequest();
        request.setSenderBankAccountId(10L);
        request.setReceiverUpiId("receiver@upi");

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findByIdAndUser(10L, senderUser)).thenReturn(Optional.of(senderAccount));
        when(upiIdRepository.findByUpiId("receiver@upi")).thenReturn(Optional.of(receiverUpiId));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                transactionService.sendMoney(request)
        );

        assertEquals("Sender bank account is not active", exception.getMessage());
    }

    @Test
    @DisplayName("Should retrieve transaction history for user accounts")
    void shouldRetrieveTransactionHistory() {
        // Arrange
        Transaction txn = new Transaction();
        txn.setTransactionReference("TXN12345");
        txn.setSenderUpiId(senderUpiId);
        txn.setReceiverUpiId(receiverUpiId);
        txn.setSenderBankAccount(senderAccount);
        txn.setReceiverBankAccount(receiverAccount);
        txn.setAmount(new BigDecimal("150.00"));
        txn.setStatus(TransactionStatus.SUCCESS);
        txn.setCreatedAt(LocalDateTime.now());

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findAllByUser(senderUser)).thenReturn(List.of(senderAccount));
        when(transactionRepository.findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc(senderAccount, senderAccount))
                .thenReturn(List.of(txn));

        // Act
        List<TransactionHistoryResponse> history = transactionService.getTransactionHistory();

        // Assert
        assertNotNull(history);
        assertEquals(1, history.size());
        assertEquals("TXN12345", history.get(0).getTransactionReference());
        assertEquals(new BigDecimal("150.00"), history.get(0).getAmount());
    }

    @Test
    @DisplayName("Should generate accurate transaction summary")
    void shouldGenerateTransactionSummary() {
        // Arrange
        Transaction txnSent = new Transaction();
        txnSent.setSenderBankAccount(senderAccount);
        txnSent.setReceiverBankAccount(receiverAccount);
        txnSent.setAmount(new BigDecimal("300.00"));
        txnSent.setStatus(TransactionStatus.SUCCESS);

        Transaction txnReceived = new Transaction();
        txnReceived.setSenderBankAccount(receiverAccount);
        txnReceived.setReceiverBankAccount(senderAccount);
        txnReceived.setAmount(new BigDecimal("500.00"));
        txnReceived.setStatus(TransactionStatus.SUCCESS);

        when(userRepository.findByUpiId("sender@upi")).thenReturn(Optional.of(senderUser));
        when(bankAccountRepository.findAllByUser(senderUser)).thenReturn(List.of(senderAccount));
        when(transactionRepository.findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc(senderAccount, senderAccount))
                .thenReturn(List.of(txnSent, txnReceived));

        // Act
        TransactionSummaryResponse summary = transactionService.getTransactionSummary();

        // Assert
        assertNotNull(summary);
        assertEquals(new BigDecimal("300.00"), summary.getTotalSent());
        assertEquals(new BigDecimal("500.00"), summary.getTotalReceived());
        assertEquals(2L, summary.getTotalTransactions());
    }
}
