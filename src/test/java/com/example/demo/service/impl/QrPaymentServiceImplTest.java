package com.example.demo.service.impl;

import com.example.demo.dto.PayQrRequest;
import com.example.demo.dto.PayQrResponse;
import com.example.demo.dto.PaymentResult;

import com.example.demo.entity.*;
import com.example.demo.exception.InvalidQrException;
import com.example.demo.exception.PaymentProcessingException;

import com.example.demo.metrics.BusinessMetricsService;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.*;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import com.example.demo.service.UpiPinService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QrPaymentServiceImplTest {

    @Mock
    private MerchantRepository merchantRepository;

    @Mock
    private MerchantQrRepository merchantQrRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private UpiIdRepository upiIdRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UpiPinService upiPinService;

    @Mock
    private OutboxService outboxService;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private BusinessMetricsService businessMetricsService;

    @InjectMocks
    private QrPaymentServiceImpl qrPaymentService;

    private Merchant merchant;
    private MerchantQr staticQr;
    private MerchantQr dynamicQr;
    private UpiId payerUpi;
    private UpiId merchantUpi;
    private BankAccount payerAccount;
    private BankAccount merchantAccount;

    @BeforeEach
    void setUp() {
        merchant = new Merchant(UUID.randomUUID(), "Jane Doe", "Doe Retail Store", "MERCH101", "doeretail@upi", "RETAIL", true, LocalDateTime.now());

        staticQr = new MerchantQr(UUID.randomUUID(), merchant, "qr_static_123", new BigDecimal("100.00"), "Static Payment", QRType.STATIC, false, null, LocalDateTime.now());

        dynamicQr = new MerchantQr(UUID.randomUUID(), merchant, "qr_dynamic_456", new BigDecimal("250.00"), "Dynamic Payment", QRType.DYNAMIC, false, LocalDateTime.now().plusMinutes(15), LocalDateTime.now());

        payerAccount = new BankAccount();
        payerAccount.setId(1L);
        payerAccount.setAccountNumber("ACC-PAYER-100");
        payerAccount.setBalance(new BigDecimal("1000.00"));
        payerAccount.setStatus(AccountStatus.ACTIVE);

        merchantAccount = new BankAccount();
        merchantAccount.setId(2L);
        merchantAccount.setAccountNumber("ACC-MERCH-200");
        merchantAccount.setBalance(new BigDecimal("5000.00"));
        merchantAccount.setStatus(AccountStatus.ACTIVE);

        payerUpi = new UpiId();
        payerUpi.setId(10L);
        payerUpi.setUpiId("john@upi");
        payerUpi.setBankAccount(payerAccount);
        payerUpi.setStatus(UpiStatus.ACTIVE);

        merchantUpi = new UpiId();
        merchantUpi.setId(20L);
        merchantUpi.setUpiId("doeretail@upi");
        merchantUpi.setBankAccount(merchantAccount);
        merchantUpi.setStatus(UpiStatus.ACTIVE);
    }

    @Test
    @DisplayName("Pay QR - Successful Static QR Payment")
    void testPayQr_SuccessfulPayment() {
        PayQrRequest request = new PayQrRequest("john@upi", "qr_static_123", "1234", new BigDecimal("100.00"), null);

        when(merchantQrRepository.findByQrToken("qr_static_123")).thenReturn(Optional.of(staticQr));
        when(upiIdRepository.findByUpiId("john@upi")).thenReturn(Optional.of(payerUpi));
        when(upiIdRepository.findByUpiId("doeretail@upi")).thenReturn(Optional.of(merchantUpi));
        when(upiPinService.verifyUpiPin(any())).thenReturn(true);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(99L);
            return t;
        });

        PayQrResponse response = qrPaymentService.payQr(request);

        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertEquals(PaymentResult.SUCCESS, response.getResult());
        assertEquals(new BigDecimal("900.00"), payerAccount.getBalance());
        assertEquals(new BigDecimal("5100.00"), merchantAccount.getBalance());

        verify(bankAccountRepository).save(payerAccount);
        verify(bankAccountRepository).save(merchantAccount);
        verify(outboxService).saveOutboxEvent(any(), anyString(), anyLong(), anyString(), anyString(), any());
        verify(eventPublisher).publishQrPaymentSuccess(any());
    }

    @Test
    @DisplayName("Pay QR - Wrong PIN Throws Exception")
    void testPayQr_WrongPin_ThrowsException() {
        PayQrRequest request = new PayQrRequest("john@upi", "qr_static_123", "9999", new BigDecimal("100.00"), null);

        when(merchantQrRepository.findByQrToken("qr_static_123")).thenReturn(Optional.of(staticQr));
        when(upiIdRepository.findByUpiId("john@upi")).thenReturn(Optional.of(payerUpi));
        when(upiIdRepository.findByUpiId("doeretail@upi")).thenReturn(Optional.of(merchantUpi));
        when(upiPinService.verifyUpiPin(any())).thenReturn(false);

        PaymentProcessingException ex = assertThrows(PaymentProcessingException.class, () -> qrPaymentService.payQr(request));
        assertEquals(PaymentResult.FAILED_INVALID_PIN, ex.getResult());

        verify(bankAccountRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay QR - Insufficient Balance Throws Exception")
    void testPayQr_InsufficientBalance_ThrowsException() {
        PayQrRequest request = new PayQrRequest("john@upi", "qr_static_123", "1234", new BigDecimal("5000.00"), null);

        when(merchantQrRepository.findByQrToken("qr_static_123")).thenReturn(Optional.of(staticQr));
        when(upiIdRepository.findByUpiId("john@upi")).thenReturn(Optional.of(payerUpi));
        when(upiIdRepository.findByUpiId("doeretail@upi")).thenReturn(Optional.of(merchantUpi));
        when(upiPinService.verifyUpiPin(any())).thenReturn(true);

        PaymentProcessingException ex = assertThrows(PaymentProcessingException.class, () -> qrPaymentService.payQr(request));
        assertEquals(PaymentResult.FAILED_INSUFFICIENT_FUNDS, ex.getResult());

        verify(bankAccountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Pay QR - Expired Dynamic QR Throws Exception")
    void testPayQr_ExpiredQr_ThrowsException() {
        dynamicQr.setExpiryTime(LocalDateTime.now().minusMinutes(5));
        PayQrRequest request = new PayQrRequest("john@upi", "qr_dynamic_456", "1234", new BigDecimal("250.00"), null);

        when(merchantQrRepository.findByQrToken("qr_dynamic_456")).thenReturn(Optional.of(dynamicQr));

        PaymentProcessingException ex = assertThrows(PaymentProcessingException.class, () -> qrPaymentService.payQr(request));
        assertEquals(PaymentResult.FAILED_EXPIRED_QR, ex.getResult());
    }

    @Test
    @DisplayName("Pay QR - Inactive Merchant Throws Exception")
    void testPayQr_InactiveMerchant_ThrowsException() {
        merchant.setActive(false);
        PayQrRequest request = new PayQrRequest("john@upi", "qr_static_123", "1234", new BigDecimal("100.00"), null);

        when(merchantQrRepository.findByQrToken("qr_static_123")).thenReturn(Optional.of(staticQr));

        PaymentProcessingException ex = assertThrows(PaymentProcessingException.class, () -> qrPaymentService.payQr(request));
        assertEquals(PaymentResult.FAILED_INACTIVE_MERCHANT, ex.getResult());
    }

    @Test
    @DisplayName("Pay QR - Dynamic QR Already Used Throws Exception")
    void testPayQr_DynamicQrAlreadyUsed_ThrowsException() {
        dynamicQr.setUsed(true);
        PayQrRequest request = new PayQrRequest("john@upi", "qr_dynamic_456", "1234", new BigDecimal("250.00"), null);

        when(merchantQrRepository.findByQrToken("qr_dynamic_456")).thenReturn(Optional.of(dynamicQr));

        PaymentProcessingException ex = assertThrows(PaymentProcessingException.class, () -> qrPaymentService.payQr(request));
        assertEquals(PaymentResult.FAILED_USED_QR, ex.getResult());
    }

    @Test
    @DisplayName("Pay QR - Duplicate Payment Returns Cached Response")
    void testPayQr_DuplicatePaymentIdempotency() {
        PayQrRequest request = new PayQrRequest("john@upi", "qr_static_123", "1234", new BigDecimal("100.00"), "IDEM-KEY-99");
        PayQrResponse cachedResponse = new PayQrResponse("TXN100", "UTR100", "john@upi", "doeretail@upi", "Doe Retail", new BigDecimal("100.00"), PaymentResult.SUCCESS, "SUCCESS", "Already processed", LocalDateTime.now());

        when(redisCacheService.find("idempotency:qr:IDEM-KEY-99", PayQrResponse.class)).thenReturn(cachedResponse);

        PayQrResponse response = qrPaymentService.payQr(request);

        assertNotNull(response);
        assertEquals("TXN100", response.getTransactionReference());
        verify(merchantQrRepository, never()).findByQrToken(anyString());
    }
}
