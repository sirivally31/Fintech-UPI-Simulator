package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.repository.PaymentRequestRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OutboxService;
import com.example.demo.service.TransactionService;
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

/**
 * Enterprise Unit Tests for PaymentRequestServiceImpl.
 * Verifies UPI Collect Request lifecycle (creation, acceptance, rejection, cancellation, expiration).
 */
@ExtendWith(MockitoExtension.class)
class PaymentRequestServiceImplTest {

    @Mock
    private PaymentRequestRepository paymentRequestRepository;

    @Mock
    private UpiIdRepository upiIdRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionService transactionService;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private PaymentRequestServiceImpl paymentRequestService;

    private User requesterUser;
    private User payerUser;
    private BankAccount requesterAccount;
    private BankAccount payerAccount;
    private UpiId requesterUpiId;
    private UpiId payerUpiId;

    @BeforeEach
    void setUp() {
        // Setup Security Context for Requester
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        lenient().when(authentication.getName()).thenReturn("requester@upi");
        SecurityContextHolder.setContext(securityContext);

        requesterUser = new User();
        requesterUser.setId(10L);
        requesterUser.setUpiId("requester@upi");

        payerUser = new User();
        payerUser.setId(20L);
        payerUser.setUpiId("payer@upi");

        requesterAccount = new BankAccount();
        requesterAccount.setId(100L);
        requesterAccount.setUser(requesterUser);

        payerAccount = new BankAccount();
        payerAccount.setId(200L);
        payerAccount.setUser(payerUser);

        requesterUpiId = new UpiId();
        requesterUpiId.setId(1000L);
        requesterUpiId.setUpiId("requester@upi");
        requesterUpiId.setPrimary(true);
        requesterUpiId.setStatus(UpiStatus.ACTIVE);
        requesterUpiId.setBankAccount(requesterAccount);

        payerUpiId = new UpiId();
        payerUpiId.setId(2000L);
        payerUpiId.setUpiId("payer@upi");
        payerUpiId.setPrimary(true);
        payerUpiId.setStatus(UpiStatus.ACTIVE);
        payerUpiId.setBankAccount(payerAccount);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should create payment request successfully")
    void shouldCreatePaymentRequestSuccessfully() {
        // Arrange
        CreatePaymentRequestRequest request = new CreatePaymentRequestRequest();
        request.setReceiverUpiId("payer@upi"); // Target to pay
        request.setAmount(new BigDecimal("250.00"));
        request.setNote("Dinner split");

        when(userRepository.findByUpiId("requester@upi")).thenReturn(Optional.of(requesterUser));
        when(upiIdRepository.findAll()).thenReturn(List.of(requesterUpiId));
        when(upiIdRepository.findByUpiId("payer@upi")).thenReturn(Optional.of(payerUpiId));
        when(paymentRequestRepository.existsByRequestReference(anyString())).thenReturn(false);

        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(invocation -> {
            PaymentRequest req = invocation.getArgument(0);
            req.setId(1L);
            req.setCreatedAt(LocalDateTime.now());
            req.setExpiresAt(LocalDateTime.now().plusHours(24));
            return req;
        });

        // Act
        PaymentRequestResponse response = paymentRequestService.createRequest(request);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentRequestStatus.PENDING, response.getStatus());
        assertEquals(new BigDecimal("250.00"), response.getAmount());
        assertEquals("payer@upi", response.getSenderUpiId()); // Payer will send money
        assertEquals("requester@upi", response.getReceiverUpiId()); // Requester receives

        verify(paymentRequestRepository).save(any(PaymentRequest.class));
        verify(outboxService).saveOutboxEvent(
                any(UUID.class),
                eq("PAYMENT_REQUEST"),
                eq(1L),
                eq("PAYMENT_REQUEST_CREATED"),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("Should throw exception when requesting money from yourself")
    void shouldThrowExceptionWhenRequestingMoneyFromSelf() {
        // Arrange
        CreatePaymentRequestRequest request = new CreatePaymentRequestRequest();
        request.setReceiverUpiId("requester@upi");
        request.setAmount(new BigDecimal("100.00"));

        when(userRepository.findByUpiId("requester@upi")).thenReturn(Optional.of(requesterUser));
        when(upiIdRepository.findAll()).thenReturn(List.of(requesterUpiId));
        when(upiIdRepository.findByUpiId("requester@upi")).thenReturn(Optional.of(requesterUpiId));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                paymentRequestService.createRequest(request)
        );

        assertEquals("Cannot request money from yourself", exception.getMessage());
        verify(paymentRequestRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when target receiver UPI ID not found")
    void shouldThrowExceptionWhenReceiverUpiIdNotFound() {
        // Arrange
        CreatePaymentRequestRequest request = new CreatePaymentRequestRequest();
        request.setReceiverUpiId("unknown@upi");

        when(userRepository.findByUpiId("requester@upi")).thenReturn(Optional.of(requesterUser));
        when(upiIdRepository.findAll()).thenReturn(List.of(requesterUpiId));
        when(upiIdRepository.findByUpiId("unknown@upi")).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                paymentRequestService.createRequest(request)
        );

        assertEquals("Target UPI ID not found", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when target UPI ID is inactive")
    void shouldThrowExceptionWhenTargetUpiIdInactive() {
        // Arrange
        payerUpiId.setStatus(UpiStatus.INACTIVE);

        CreatePaymentRequestRequest request = new CreatePaymentRequestRequest();
        request.setReceiverUpiId("payer@upi");

        when(userRepository.findByUpiId("requester@upi")).thenReturn(Optional.of(requesterUser));
        when(upiIdRepository.findAll()).thenReturn(List.of(requesterUpiId));
        when(upiIdRepository.findByUpiId("payer@upi")).thenReturn(Optional.of(payerUpiId));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                paymentRequestService.createRequest(request)
        );

        assertEquals("Target UPI ID is not active", exception.getMessage());
    }

    @Test
    @DisplayName("Should accept payment request successfully")
    void shouldAcceptPaymentRequestSuccessfully() {
        // Switch context to Payer (who is accepting the request to pay)
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("payer@upi");
        SecurityContextHolder.setContext(securityContext);

        PaymentRequest req = new PaymentRequest();
        req.setId(5L);
        req.setRequestReference("REQ123");
        req.setSenderUpiId(payerUpiId); // Payer
        req.setReceiverUpiId(requesterUpiId); // Requester
        req.setAmount(new BigDecimal("300.00"));
        req.setStatus(PaymentRequestStatus.PENDING);
        req.setExpiresAt(LocalDateTime.now().plusHours(12));

        AcceptPaymentRequestRequest acceptReq = new AcceptPaymentRequestRequest();
        acceptReq.setUpiPin("1234");

        TransactionResponse txnResp = new TransactionResponse();
        txnResp.setTransactionReference("TXN9999");
        txnResp.setStatus(TransactionStatus.SUCCESS);

        when(paymentRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userRepository.findByUpiId("payer@upi")).thenReturn(Optional.of(payerUser));
        when(transactionService.sendMoney(any(SendMoneyRequest.class))).thenReturn(txnResp);
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PaymentRequestResponse response = paymentRequestService.acceptRequest(5L, acceptReq);

        // Assert
        assertNotNull(response);
        assertEquals(PaymentRequestStatus.ACCEPTED, response.getStatus());
        assertNotNull(response.getRespondedAt());

        verify(transactionService).sendMoney(any(SendMoneyRequest.class));
        verify(paymentRequestRepository).save(req);
        verify(outboxService).saveOutboxEvent(
                any(UUID.class),
                eq("PAYMENT_REQUEST"),
                eq(5L),
                eq("PAYMENT_REQUEST_ACCEPTED"),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("Should reject payment request successfully")
    void shouldRejectPaymentRequest() {
        // Context: Payer
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("payer@upi");
        SecurityContextHolder.setContext(securityContext);

        PaymentRequest req = new PaymentRequest();
        req.setId(5L);
        req.setSenderUpiId(payerUpiId);
        req.setReceiverUpiId(requesterUpiId);
        req.setStatus(PaymentRequestStatus.PENDING);
        req.setExpiresAt(LocalDateTime.now().plusHours(12));

        RejectPaymentRequestRequest rejectReq = new RejectPaymentRequestRequest();
        rejectReq.setReason("Not recognizing request");

        when(paymentRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userRepository.findByUpiId("payer@upi")).thenReturn(Optional.of(payerUser));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PaymentRequestResponse response = paymentRequestService.rejectRequest(5L, rejectReq);

        // Assert
        assertEquals(PaymentRequestStatus.REJECTED, response.getStatus());
        verify(transactionService, never()).sendMoney(any());
        verify(outboxService).saveOutboxEvent(
                any(UUID.class),
                eq("PAYMENT_REQUEST"),
                eq(5L),
                eq("PAYMENT_REQUEST_REJECTED"),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("Should cancel payment request by requester")
    void shouldCancelPaymentRequest() {
        // Context: Requester (who created the request)
        when(userRepository.findByUpiId("requester@upi")).thenReturn(Optional.of(requesterUser));

        PaymentRequest req = new PaymentRequest();
        req.setId(5L);
        req.setSenderUpiId(payerUpiId);
        req.setReceiverUpiId(requesterUpiId);
        req.setStatus(PaymentRequestStatus.PENDING);
        req.setExpiresAt(LocalDateTime.now().plusHours(12));

        CancelPaymentRequestRequest cancelReq = new CancelPaymentRequestRequest();

        when(paymentRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(paymentRequestRepository.save(any(PaymentRequest.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        PaymentRequestResponse response = paymentRequestService.cancelRequest(5L, cancelReq);

        // Assert
        assertEquals(PaymentRequestStatus.CANCELLED, response.getStatus());
        verify(outboxService).saveOutboxEvent(
                any(UUID.class),
                eq("PAYMENT_REQUEST"),
                eq(5L),
                eq("PAYMENT_REQUEST_CANCELLED"),
                anyString(),
                any()
        );
    }

    @Test
    @DisplayName("Should throw exception when payment request is expired")
    void shouldThrowExceptionWhenPaymentRequestIsExpired() {
        // Context: Payer
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("payer@upi");
        SecurityContextHolder.setContext(securityContext);

        PaymentRequest req = new PaymentRequest();
        req.setId(5L);
        req.setSenderUpiId(payerUpiId);
        req.setReceiverUpiId(requesterUpiId);
        req.setStatus(PaymentRequestStatus.PENDING);
        req.setExpiresAt(LocalDateTime.now().minusMinutes(5)); // Past expiration time

        AcceptPaymentRequestRequest acceptReq = new AcceptPaymentRequestRequest();

        when(paymentRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userRepository.findByUpiId("payer@upi")).thenReturn(Optional.of(payerUser));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                paymentRequestService.acceptRequest(5L, acceptReq)
        );

        assertEquals("Payment request has expired", exception.getMessage());
        assertEquals(PaymentRequestStatus.EXPIRED, req.getStatus());
        verify(paymentRequestRepository).save(req); // Saves as EXPIRED
    }

    @Test
    @DisplayName("Should throw exception when unauthorized user attempts to respond")
    void shouldThrowExceptionWhenUserIsUnauthorizedToRespond() {
        // Context: Unauthorized user (neither requester nor payer)
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("intruder@upi");
        SecurityContextHolder.setContext(securityContext);

        User intruder = new User();
        intruder.setId(999L);
        intruder.setUpiId("intruder@upi");

        PaymentRequest req = new PaymentRequest();
        req.setId(5L);
        req.setSenderUpiId(payerUpiId);
        req.setReceiverUpiId(requesterUpiId);

        when(paymentRequestRepository.findById(5L)).thenReturn(Optional.of(req));
        when(userRepository.findByUpiId("intruder@upi")).thenReturn(Optional.of(intruder));

        // Act & Assert
        SecurityException exception = assertThrows(SecurityException.class, () ->
                paymentRequestService.acceptRequest(5L, new AcceptPaymentRequestRequest())
        );

        assertEquals("You are not authorized to respond to this request", exception.getMessage());
    }
}
