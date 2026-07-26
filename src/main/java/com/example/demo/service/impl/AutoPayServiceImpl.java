package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.events.AutoPayCancelledEvent;
import com.example.demo.events.AutoPayCreatedEvent;
import com.example.demo.events.AutoPayExecutedEvent;
import com.example.demo.events.AutoPayFailedEvent;
import com.example.demo.exception.*;
import com.example.demo.metrics.BusinessMetricsService;
import com.example.demo.producer.EventPublisher;
import com.example.demo.repository.*;
import com.example.demo.service.AutoPayService;
import com.example.demo.service.OutboxService;
import com.example.demo.service.RedisCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Production-grade Service Implementation for AutoPay mandate management and scheduled execution.
 */
@Service
public class AutoPayServiceImpl implements AutoPayService {

    private static final Logger log = LoggerFactory.getLogger(AutoPayServiceImpl.class);
    private static final String CACHE_ID_PREFIX = "autopay:id:";

    @Value("${app.autopay.max-retries:3}")
    private int maxRetries = 3;

    private final AutoPayRepository autoPayRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UpiIdRepository upiIdRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final RedisCacheService redisCacheService;
    private final OutboxService outboxService;
    private final EventPublisher eventPublisher;
    private final BusinessMetricsService businessMetricsService;

    public AutoPayServiceImpl(AutoPayRepository autoPayRepository,
                              BeneficiaryRepository beneficiaryRepository,
                              BankAccountRepository bankAccountRepository,
                              UpiIdRepository upiIdRepository,
                              TransactionRepository transactionRepository,
                              UserRepository userRepository,
                              RedisCacheService redisCacheService,
                              OutboxService outboxService,
                              EventPublisher eventPublisher,
                              BusinessMetricsService businessMetricsService) {
        this.autoPayRepository = autoPayRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.upiIdRepository = upiIdRepository;
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.redisCacheService = redisCacheService;
        this.outboxService = outboxService;
        this.eventPublisher = eventPublisher;
        this.businessMetricsService = businessMetricsService;
    }

    private User getCurrentOwner() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUpiId(username)
                .orElseThrow(() -> new UserNotFoundException("Authenticated user not found"));
    }

    @Override
    @Transactional
    public AutoPayResponse createAutoPay(CreateAutoPayRequest request) {
        User owner = getCurrentOwner();
        log.info("Creating AutoPay mandate for owner [{}] to beneficiary [{}]", owner.getUpiId(), request.getBeneficiaryId());

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new AutoPayValidationException("Mandate start date cannot be after end date.");
        }

        Beneficiary beneficiary = beneficiaryRepository.findByIdAndOwner(request.getBeneficiaryId(), owner)
                .orElseThrow(() -> new BeneficiaryNotFoundException("Beneficiary not found with ID: " + request.getBeneficiaryId()));

        String mandateRef = generateMandateReference();
        LocalDateTime nextExecution = calculateFirstExecutionTime(request.getStartDate());

        AutoPay autoPay = new AutoPay();
        autoPay.setOwner(owner);
        autoPay.setBeneficiary(beneficiary);
        autoPay.setMandateReference(mandateRef);
        autoPay.setAmount(request.getAmount());
        autoPay.setFrequency(request.getFrequency());
        autoPay.setStatus(AutoPayStatus.ACTIVE);
        autoPay.setStartDate(request.getStartDate());
        autoPay.setEndDate(request.getEndDate());
        autoPay.setNextExecutionTime(nextExecution);
        autoPay.setRetryCount(0);
        autoPay.setActive(true);

        AutoPay saved = autoPayRepository.save(autoPay);
        AutoPayResponse response = mapToResponse(saved);

        redisCacheService.save(CACHE_ID_PREFIX + saved.getId(), response, 1, TimeUnit.HOURS);

        String correlationId = UUID.randomUUID().toString();
        AutoPayCreatedEvent event = AutoPayCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("AUTOPAY_CREATED")
                .correlationId(correlationId)
                .autoPayId(saved.getId())
                .mandateReference(saved.getMandateReference())
                .ownerUpiId(owner.getUpiId())
                .beneficiaryUpiId(beneficiary.getBeneficiaryUpiId())
                .amount(saved.getAmount())
                .frequency(saved.getFrequency().name())
                .startDate(saved.getStartDate())
                .endDate(saved.getEndDate())
                .build();

        outboxService.saveOutboxEvent(event.getEventId(), "AUTOPAY", (long) Math.abs(saved.getId().hashCode()), "AUTOPAY_CREATED", correlationId, event);
        eventPublisher.publishAutoPayCreated(event);

        log.info("Successfully created AutoPay mandate [{}] with reference [{}]", saved.getId(), saved.getMandateReference());
        return response;
    }

    @Override
    @Transactional
    public AutoPayResponse updateAutoPay(UUID id, UpdateAutoPayRequest request) {
        User owner = getCurrentOwner();
        log.info("Updating AutoPay mandate ID [{}] for owner [{}]", id, owner.getUpiId());

        AutoPay autoPay = autoPayRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new AutoPayNotFoundException("AutoPay mandate not found with ID: " + id));

        if (request.getAmount() != null && request.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            autoPay.setAmount(request.getAmount());
        }
        if (request.getFrequency() != null) {
            autoPay.setFrequency(request.getFrequency());
        }
        if (request.getEndDate() != null) {
            if (request.getEndDate().isBefore(autoPay.getStartDate())) {
                throw new AutoPayValidationException("End date cannot be before start date.");
            }
            autoPay.setEndDate(request.getEndDate());
        }

        AutoPay updated = autoPayRepository.save(autoPay);
        AutoPayResponse response = mapToResponse(updated);

        redisCacheService.save(CACHE_ID_PREFIX + id, response, 1, TimeUnit.HOURS);
        return response;
    }

    @Override
    @Transactional
    public void cancelAutoPay(UUID id) {
        User owner = getCurrentOwner();
        log.info("Cancelling AutoPay mandate ID [{}] for owner [{}]", id, owner.getUpiId());

        AutoPay autoPay = autoPayRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new AutoPayNotFoundException("AutoPay mandate not found with ID: " + id));

        autoPay.setStatus(AutoPayStatus.CANCELLED);
        autoPay.setActive(false);
        autoPayRepository.save(autoPay);

        redisCacheService.delete(CACHE_ID_PREFIX + id);

        String correlationId = UUID.randomUUID().toString();
        AutoPayCancelledEvent event = AutoPayCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("AUTOPAY_CANCELLED")
                .correlationId(correlationId)
                .autoPayId(id)
                .mandateReference(autoPay.getMandateReference())
                .ownerUpiId(owner.getUpiId())
                .beneficiaryUpiId(autoPay.getBeneficiary().getBeneficiaryUpiId())
                .build();

        outboxService.saveOutboxEvent(event.getEventId(), "AUTOPAY", (long) Math.abs(id.hashCode()), "AUTOPAY_CANCELLED", correlationId, event);
        eventPublisher.publishAutoPayCancelled(event);
    }

    @Override
    @Transactional
    public AutoPayResponse pauseAutoPay(UUID id) {
        User owner = getCurrentOwner();
        AutoPay autoPay = autoPayRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new AutoPayNotFoundException("AutoPay mandate not found with ID: " + id));

        autoPay.setStatus(AutoPayStatus.PAUSED);
        AutoPay updated = autoPayRepository.save(autoPay);
        AutoPayResponse response = mapToResponse(updated);

        redisCacheService.save(CACHE_ID_PREFIX + id, response, 1, TimeUnit.HOURS);
        return response;
    }

    @Override
    @Transactional
    public AutoPayResponse resumeAutoPay(UUID id) {
        User owner = getCurrentOwner();
        AutoPay autoPay = autoPayRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new AutoPayNotFoundException("AutoPay mandate not found with ID: " + id));

        autoPay.setStatus(AutoPayStatus.ACTIVE);
        AutoPay updated = autoPayRepository.save(autoPay);
        AutoPayResponse response = mapToResponse(updated);

        redisCacheService.save(CACHE_ID_PREFIX + id, response, 1, TimeUnit.HOURS);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public AutoPayResponse getAutoPayById(UUID id) {
        User owner = getCurrentOwner();
        String cacheKey = CACHE_ID_PREFIX + id;
        AutoPayResponse cached = redisCacheService.find(cacheKey, AutoPayResponse.class);
        if (cached != null) {
            return cached;
        }

        AutoPay autoPay = autoPayRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> new AutoPayNotFoundException("AutoPay mandate not found with ID: " + id));

        AutoPayResponse response = mapToResponse(autoPay);
        redisCacheService.save(cacheKey, response, 1, TimeUnit.HOURS);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutoPayResponse> getAllAutoPays() {
        User owner = getCurrentOwner();
        return autoPayRepository.findByOwner(owner).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutoPayHistoryResponse> getAutoPayHistory() {
        User owner = getCurrentOwner();
        List<BankAccount> userAccounts = bankAccountRepository.findAllByUser(owner);

        return userAccounts.stream()
                .flatMap(acc -> transactionRepository.findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc(acc, acc).stream())
                .filter(t -> t.getRemarks() != null && t.getRemarks().startsWith("AutoPay Mandate"))
                .distinct()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .map(t -> new AutoPayHistoryResponse(
                        null,
                        extractMandateRef(t.getRemarks()),
                        t.getTransactionReference(),
                        "UTR" + t.getTransactionReference().replaceAll("[^0-9]", ""),
                        (t.getReceiverBankAccount() != null && t.getReceiverBankAccount().getUser() != null) ? t.getReceiverBankAccount().getUser().getName() : "Beneficiary",
                        t.getReceiverUpiId() != null ? t.getReceiverUpiId().getUpiId() : "",
                        t.getAmount(),
                        t.getStatus().name(),
                        t.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void executeDueAutoPay(AutoPay autoPay) {
        log.info("Executing due AutoPay mandate ID [{}] with reference [{}]", autoPay.getId(), autoPay.getMandateReference());

        try {
            User owner = autoPay.getOwner();
            Beneficiary beneficiary = autoPay.getBeneficiary();

            BankAccount ownerAccount = bankAccountRepository.findAllByUser(owner).stream()
                    .filter(acc -> acc.getStatus() == AccountStatus.ACTIVE)
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Owner has no active bank account"));

            UpiId beneficiaryUpi = upiIdRepository.findByUpiId(beneficiary.getBeneficiaryUpiId())
                    .orElseThrow(() -> new IllegalStateException("Beneficiary UPI ID not registered: " + beneficiary.getBeneficiaryUpiId()));

            BankAccount beneficiaryAccount = beneficiaryUpi.getBankAccount();
            if (beneficiaryAccount.getStatus() != AccountStatus.ACTIVE) {
                throw new IllegalStateException("Beneficiary bank account is inactive");
            }

            if (ownerAccount.getBalance().compareTo(autoPay.getAmount()) < 0) {
                throw new IllegalStateException("Insufficient balance for AutoPay execution");
            }

            // Transfer money
            ownerAccount.setBalance(ownerAccount.getBalance().subtract(autoPay.getAmount()));
            beneficiaryAccount.setBalance(beneficiaryAccount.getBalance().add(autoPay.getAmount()));

            bankAccountRepository.save(ownerAccount);
            bankAccountRepository.save(beneficiaryAccount);

            // Save Transaction
            String txnRef = generateTransactionReference();
            String utrNo = generateUtrNumber();

            Transaction transaction = new Transaction();
            transaction.setTransactionReference(txnRef);
            transaction.setSenderBankAccount(ownerAccount);
            transaction.setReceiverBankAccount(beneficiaryAccount);
            transaction.setSenderUpiId(upiIdRepository.findByBankAccountAndIsPrimaryTrue(ownerAccount).orElse(null));
            transaction.setReceiverUpiId(beneficiaryUpi);
            transaction.setAmount(autoPay.getAmount());
            transaction.setRemarks("AutoPay Mandate: " + autoPay.getMandateReference());
            transaction.setStatus(TransactionStatus.SUCCESS);

            Transaction savedTxn = transactionRepository.save(transaction);

            // Update AutoPay mandate schedule
            LocalDateTime nextExec = calculateNextExecutionTime(autoPay.getFrequency(), LocalDateTime.now());
            autoPay.setNextExecutionTime(nextExec);
            autoPay.setRetryCount(0);

            if (nextExec.toLocalDate().isAfter(autoPay.getEndDate())) {
                autoPay.setStatus(AutoPayStatus.COMPLETED);
                autoPay.setActive(false);
            }

            autoPayRepository.save(autoPay);

            // Outbox & Kafka Event
            String correlationId = UUID.randomUUID().toString();
            AutoPayExecutedEvent event = AutoPayExecutedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .eventTime(LocalDateTime.now())
                    .eventType("AUTOPAY_EXECUTED")
                    .correlationId(correlationId)
                    .autoPayId(autoPay.getId())
                    .mandateReference(autoPay.getMandateReference())
                    .transactionReference(txnRef)
                    .utrNumber(utrNo)
                    .ownerUpiId(owner.getUpiId())
                    .beneficiaryUpiId(beneficiary.getBeneficiaryUpiId())
                    .amount(autoPay.getAmount())
                    .nextExecutionTime(autoPay.getNextExecutionTime())
                    .build();

            outboxService.saveOutboxEvent(event.getEventId(), "AUTOPAY", (long) Math.abs(autoPay.getId().hashCode()), "AUTOPAY_EXECUTED", correlationId, event);
            eventPublisher.publishAutoPayExecuted(event);

            redisCacheService.delete(CACHE_ID_PREFIX + autoPay.getId());
            businessMetricsService.recordTransactionSuccess("AUTOPAY");

            log.info("Successfully executed AutoPay mandate [{}] | Transaction [{}]", autoPay.getMandateReference(), txnRef);
        } catch (Exception e) {
            log.error("Failed to execute AutoPay mandate [{}]", autoPay.getMandateReference(), e);
            handleExecutionFailure(autoPay, e.getMessage());
        }
    }

    private void handleExecutionFailure(AutoPay autoPay, String reason) {
        int retries = autoPay.getRetryCount() + 1;
        autoPay.setRetryCount(retries);

        int effectiveMax = maxRetries > 0 ? maxRetries : 3;
        if (retries >= effectiveMax) {
            autoPay.setStatus(AutoPayStatus.FAILED);
            autoPay.setActive(false);
        } else {
            long backoffMinutes = (long) Math.pow(2, retries) * 15;
            autoPay.setNextExecutionTime(LocalDateTime.now().plusMinutes(backoffMinutes));
        }

        autoPayRepository.save(autoPay);

        String correlationId = UUID.randomUUID().toString();
        AutoPayFailedEvent event = AutoPayFailedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("AUTOPAY_FAILED")
                .correlationId(correlationId)
                .autoPayId(autoPay.getId())
                .mandateReference(autoPay.getMandateReference())
                .ownerUpiId(autoPay.getOwner().getUpiId())
                .beneficiaryUpiId(autoPay.getBeneficiary().getBeneficiaryUpiId())
                .amount(autoPay.getAmount())
                .retryCount(retries)
                .failureReason(reason)
                .build();

        outboxService.saveOutboxEvent(event.getEventId(), "AUTOPAY", (long) Math.abs(autoPay.getId().hashCode()), "AUTOPAY_FAILED", correlationId, event);
        eventPublisher.publishAutoPayFailed(event);
        businessMetricsService.recordTransactionFailure("AUTOPAY", reason);
    }

    private LocalDateTime calculateFirstExecutionTime(LocalDate startDate) {
        if (startDate.isAfter(LocalDate.now())) {
            return startDate.atTime(9, 0);
        }
        return LocalDateTime.now().plusMinutes(1);
    }

    private LocalDateTime calculateNextExecutionTime(AutoPayFrequency frequency, LocalDateTime fromTime) {
        switch (frequency) {
            case DAILY:
                return fromTime.plusDays(1);
            case WEEKLY:
                return fromTime.plusWeeks(1);
            case MONTHLY:
                return fromTime.plusMonths(1);
            case YEARLY:
                return fromTime.plusYears(1);
            default:
                return fromTime.plusDays(1);
        }
    }

    private String generateMandateReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = 1;
        String ref;
        do {
            ref = String.format("MN%s%04d", datePart, seq++);
        } while (autoPayRepository.existsByMandateReference(ref));
        return ref;
    }

    private String generateTransactionReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int seq = 1;
        String ref;
        do {
            ref = String.format("TXN%s%04d", datePart, seq++);
        } while (transactionRepository.existsByTransactionReference(ref));
        return ref;
    }

    private String generateUtrNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long randomPart = (long) (Math.random() * 90000000L) + 10000000L;
        return "UTR" + datePart + randomPart;
    }

    private String extractMandateRef(String remarks) {
        if (remarks != null && remarks.contains("MN")) {
            return remarks.substring(remarks.indexOf("MN"));
        }
        return "N/A";
    }

    private AutoPayResponse mapToResponse(AutoPay autoPay) {
        return new AutoPayResponse(
                autoPay.getId(),
                autoPay.getOwner().getUpiId(),
                autoPay.getBeneficiary().getId(),
                autoPay.getBeneficiary().getBeneficiaryName(),
                autoPay.getBeneficiary().getBeneficiaryUpiId(),
                autoPay.getMandateReference(),
                autoPay.getAmount(),
                autoPay.getFrequency(),
                autoPay.getStatus(),
                autoPay.getStartDate(),
                autoPay.getEndDate(),
                autoPay.getNextExecutionTime(),
                autoPay.getRetryCount(),
                autoPay.getActive(),
                autoPay.getCreatedAt(),
                autoPay.getUpdatedAt()
        );
    }
}
