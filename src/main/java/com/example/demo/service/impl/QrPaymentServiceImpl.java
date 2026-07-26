package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.events.QrPaymentSuccessEvent;
import com.example.demo.exception.*;
import com.example.demo.metrics.BusinessMetricsService;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.*;
import com.example.demo.service.OutboxService;
import com.example.demo.service.QrPaymentService;
import com.example.demo.service.RedisCacheService;
import com.example.demo.service.UpiPinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Implementation of QrPaymentService orchestrating financial validations,
 * atomic balance debit/credit, transaction persistence, outbox event generation,
 * Kafka message publishing, and Redis cache invalidation.
 */
@Service
public class QrPaymentServiceImpl implements QrPaymentService {

    private static final Logger log = LoggerFactory.getLogger(QrPaymentServiceImpl.class);
    private static final String IDEMPOTENCY_PREFIX = "idempotency:qr:";

    private final MerchantRepository merchantRepository;
    private final MerchantQrRepository merchantQrRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UpiIdRepository upiIdRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final UpiPinService upiPinService;
    private final OutboxService outboxService;
    private final RedisCacheService redisCacheService;
    private final EventPublisher eventPublisher;
    private final BusinessMetricsService businessMetricsService;

    public QrPaymentServiceImpl(MerchantRepository merchantRepository,
                                MerchantQrRepository merchantQrRepository,
                                BankAccountRepository bankAccountRepository,
                                UpiIdRepository upiIdRepository,
                                TransactionRepository transactionRepository,
                                UserRepository userRepository,
                                UpiPinService upiPinService,
                                OutboxService outboxService,
                                RedisCacheService redisCacheService,
                                EventPublisher eventPublisher,
                                BusinessMetricsService businessMetricsService) {
        this.merchantRepository = merchantRepository;
        this.merchantQrRepository = merchantQrRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.upiIdRepository = upiIdRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.upiPinService = upiPinService;
        this.outboxService = outboxService;
        this.redisCacheService = redisCacheService;
        this.eventPublisher = eventPublisher;
        this.businessMetricsService = businessMetricsService;
    }

    @Override
    @Transactional
    public PayQrResponse payQr(PayQrRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("Processing QR payment request from [{}] for QR token [{}]", request.getPayerUpiId(), request.getQrToken());

        // 1. Idempotency check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            String cacheKey = IDEMPOTENCY_PREFIX + request.getIdempotencyKey();
            PayQrResponse cachedResponse = redisCacheService.find(cacheKey, PayQrResponse.class);
            if (cachedResponse != null) {
                log.info("Idempotent retry detected for key [{}]. Returning cached response.", request.getIdempotencyKey());
                return cachedResponse;
            }
        }

        // 2. Validate QR Code & Merchant
        MerchantQr merchantQr = merchantQrRepository.findByQrToken(request.getQrToken())
                .orElseThrow(() -> new InvalidQrException("QR code invalid or not found with token: " + request.getQrToken()));

        Merchant merchant = merchantQr.getMerchant();
        if (!Boolean.TRUE.equals(merchant.getActive())) {
            throw new PaymentProcessingException("Merchant is inactive: " + merchant.getBusinessName(), PaymentResult.FAILED_INACTIVE_MERCHANT);
        }

        if (merchantQr.getType() == QRType.DYNAMIC) {
            if (Boolean.TRUE.equals(merchantQr.getUsed())) {
                throw new PaymentProcessingException("Dynamic QR code has already been used", PaymentResult.FAILED_USED_QR);
            }
            if (merchantQr.getExpiryTime() != null && LocalDateTime.now().isAfter(merchantQr.getExpiryTime())) {
                throw new PaymentProcessingException("Dynamic QR code has expired", PaymentResult.FAILED_EXPIRED_QR);
            }
        }

        // 3. Validate Payer UPI & Bank Account
        UpiId payerUpi = upiIdRepository.findByUpiId(request.getPayerUpiId())
                .orElseThrow(() -> new PaymentProcessingException("Payer UPI ID not found: " + request.getPayerUpiId(), PaymentResult.FAILED_GENERAL));

        if (payerUpi.getStatus() != UpiStatus.ACTIVE) {
            throw new PaymentProcessingException("Payer UPI ID is not active", PaymentResult.FAILED_GENERAL);
        }

        BankAccount payerAccount = payerUpi.getBankAccount();
        if (payerAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new PaymentProcessingException("Payer bank account is not active or blocked", PaymentResult.FAILED_GENERAL);
        }

        // 4. Validate Merchant UPI & Bank Account
        UpiId merchantUpi = upiIdRepository.findByUpiId(merchant.getUpiId())
                .orElseThrow(() -> new PaymentProcessingException("Merchant UPI ID not registered: " + merchant.getUpiId(), PaymentResult.FAILED_GENERAL));

        BankAccount merchantAccount = merchantUpi.getBankAccount();
        if (merchantAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new PaymentProcessingException("Merchant bank account is not active", PaymentResult.FAILED_INACTIVE_MERCHANT);
        }

        // 5. Verify UPI PIN
        VerifyUpiPinRequest verifyReq = new VerifyUpiPinRequest();
        verifyReq.setBankAccountId(payerAccount.getId());
        verifyReq.setPin(request.getUpiPin());
        if (!upiPinService.verifyUpiPin(verifyReq)) {
            throw new PaymentProcessingException("Invalid UPI PIN provided", PaymentResult.FAILED_INVALID_PIN);
        }

        // 6. Validate Payment Amount & Sufficient Balance
        BigDecimal payAmount = request.getAmount();
        if (merchantQr.getType() == QRType.DYNAMIC && merchantQr.getAmount() != null) {
            payAmount = merchantQr.getAmount();
        }

        if (payAmount == null || payAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentProcessingException("Invalid payment amount", PaymentResult.FAILED_GENERAL);
        }

        if (payerAccount.getBalance().compareTo(payAmount) < 0) {
            throw new PaymentProcessingException("Insufficient account balance", PaymentResult.FAILED_INSUFFICIENT_FUNDS);
        }

        // 7. Atomic Financial Transfer
        payerAccount.setBalance(payerAccount.getBalance().subtract(payAmount));
        merchantAccount.setBalance(merchantAccount.getBalance().add(payAmount));

        bankAccountRepository.save(payerAccount);
        bankAccountRepository.save(merchantAccount);

        // 8. Generate References & Save Transaction
        String txnRef = generateTransactionReference();
        String utrNo = generateUtrNumber();

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(txnRef);
        transaction.setSenderBankAccount(payerAccount);
        transaction.setReceiverBankAccount(merchantAccount);
        transaction.setSenderUpiId(payerUpi);
        transaction.setReceiverUpiId(merchantUpi);
        transaction.setAmount(payAmount);
        transaction.setRemarks("QR Payment to " + merchant.getBusinessName());
        transaction.setStatus(TransactionStatus.SUCCESS);

        Transaction savedTxn = transactionRepository.save(transaction);

        // 9. Mark Dynamic QR as USED
        if (merchantQr.getType() == QRType.DYNAMIC) {
            merchantQr.setUsed(true);
            merchantQrRepository.save(merchantQr);
        }

        // 10. Save Outbox Event
        String correlationId = UUID.randomUUID().toString();
        QrPaymentSuccessEvent event = QrPaymentSuccessEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("QR_PAYMENT_SUCCESS")
                .correlationId(correlationId)
                .transactionReference(txnRef)
                .utrNumber(utrNo)
                .qrToken(request.getQrToken())
                .payerUpiId(request.getPayerUpiId())
                .merchantUpiId(merchant.getUpiId())
                .merchantBusinessName(merchant.getBusinessName())
                .amount(payAmount)
                .status("SUCCESS")
                .build();

        outboxService.saveOutboxEvent(
                event.getEventId(),
                "QR_PAYMENT",
                savedTxn.getId(),
                "QR_PAYMENT_SUCCESS",
                correlationId,
                event
        );

        // 11. Publish Kafka Event
        eventPublisher.publishQrPaymentSuccess(event);

        // 12. Redis Cache Eviction & Invalidation
        redisCacheService.delete("merchant:id:" + merchant.getId());
        redisCacheService.delete("qr:token:" + request.getQrToken());
        redisCacheService.delete("account:balance:" + payerAccount.getAccountNumber());

        PayQrResponse response = new PayQrResponse(
                txnRef,
                utrNo,
                request.getPayerUpiId(),
                merchant.getUpiId(),
                merchant.getBusinessName(),
                payAmount,
                PaymentResult.SUCCESS,
                "SUCCESS",
                "QR Payment completed successfully",
                savedTxn.getCreatedAt()
        );

        // Cache Idempotency response if key provided
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            redisCacheService.save(IDEMPOTENCY_PREFIX + request.getIdempotencyKey(), response, 24, TimeUnit.HOURS);
        }

        businessMetricsService.recordTransactionSuccess("QR_PAYMENT");
        businessMetricsService.recordTransactionAmount(payAmount.doubleValue(), "QR_PAYMENT");
        businessMetricsService.recordTransactionProcessingTime(System.currentTimeMillis() - startTime, "QR_PAYMENT");

        log.info("QR Payment successful | reference: [{}] | UTR: [{}] | amount: [{}]", txnRef, utrNo, payAmount);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<QrTransactionHistoryResponse> getQrTransactionHistory() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUpiId(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));

        List<BankAccount> userAccounts = bankAccountRepository.findAllByUser(currentUser);

        return userAccounts.stream()
                .flatMap(acc -> transactionRepository.findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc(acc, acc).stream())
                .filter(t -> t.getRemarks() != null && t.getRemarks().startsWith("QR Payment"))
                .distinct()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public QrTransactionHistoryResponse getQrTransactionById(Long transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException("QR Transaction not found with ID: " + transactionId));

        return mapToHistoryResponse(transaction);
    }

    private String generateTransactionReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int sequence = 1;
        String baseRef;
        do {
            baseRef = String.format("TXN%s%04d", datePart, sequence++);
        } while (transactionRepository.existsByTransactionReference(baseRef));
        return baseRef;
    }

    private String generateUtrNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long randomPart = (long) (Math.random() * 90000000L) + 10000000L;
        return "UTR" + datePart + randomPart;
    }

    private QrTransactionHistoryResponse mapToHistoryResponse(Transaction transaction) {
        String merchantUpi = transaction.getReceiverUpiId() != null ? transaction.getReceiverUpiId().getUpiId() : "";
        Merchant merchant = merchantRepository.findByUpiId(merchantUpi).orElse(null);

        String merchantName = merchant != null ? merchant.getMerchantName() : "Merchant";
        String businessName = merchant != null ? merchant.getBusinessName() : "Merchant Store";

        return new QrTransactionHistoryResponse(
                transaction.getId(),
                transaction.getTransactionReference(),
                merchantName,
                businessName,
                merchantUpi,
                transaction.getSenderUpiId() != null ? transaction.getSenderUpiId().getUpiId() : "",
                transaction.getAmount(),
                transaction.getStatus() != null ? transaction.getStatus().name() : "SUCCESS",
                "UTR" + transaction.getTransactionReference().replaceAll("[^0-9]", ""),
                "DYNAMIC",
                transaction.getCreatedAt()
        );
    }
}
