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
import com.example.demo.exception.BankAccountNotFoundException;
import com.example.demo.exception.TransactionNotFoundException;
import com.example.demo.exception.UnauthorizedAccountAccessException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.BankAccountRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.events.TransactionCompletedEvent;
import com.example.demo.service.OutboxService;
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
import java.util.stream.Collectors;

/**
 * Implementation of the TransactionService.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);

    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final UpiIdRepository upiIdRepository;
    private final UserRepository userRepository;
    private final UpiPinService upiPinService;
    private final OutboxService outboxService;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  BankAccountRepository bankAccountRepository,
                                  UpiIdRepository upiIdRepository,
                                  UserRepository userRepository,
                                  UpiPinService upiPinService,
                                  OutboxService outboxService) {
        this.transactionRepository = transactionRepository;
        this.bankAccountRepository = bankAccountRepository;
        this.upiIdRepository = upiIdRepository;
        this.userRepository = userRepository;
        this.upiPinService = upiPinService;
        this.outboxService = outboxService;
    }

    private TransactionCompletedEvent buildTransactionCompletedEvent(Transaction transaction, String correlationId) {
        return TransactionCompletedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("TransactionCompleted")
                .correlationId(correlationId)
                .transactionReference(transaction.getTransactionReference())
                .senderUpiId(transaction.getSenderUpiId().getUpiId())
                .receiverUpiId(transaction.getReceiverUpiId().getUpiId())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .remarks(transaction.getRemarks())
                .build();
    }

    private void publishTransactionEvent(Transaction transaction) {
        String correlationId = UUID.randomUUID().toString();
        TransactionCompletedEvent event = buildTransactionCompletedEvent(transaction, correlationId);
        outboxService.saveOutboxEvent(
                event.getEventId(),
                "TRANSACTION",
                transaction.getId(),
                "TRANSACTION_COMPLETED",
                correlationId,
                event
        );
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUpiId(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private BankAccount getOwnedBankAccount(Long accountId) {
        User currentUser = getCurrentUser();
        // findByIdAndUser verifies ownership in one database query
        return bankAccountRepository.findByIdAndUser(accountId, currentUser)
                .orElseThrow(() -> new UnauthorizedAccountAccessException("You do not own this bank account or it does not exist."));
    }

    private String generateTransactionReference() {
        String baseRef;
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int sequence = 1;
        do {
            baseRef = String.format("TXN%s%04d", datePart, sequence++);
        } while (transactionRepository.existsByTransactionReference(baseRef));
        return baseRef;
    }

    private TransactionResponse convertToTransactionResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setTransactionReference(transaction.getTransactionReference());
        response.setSenderUpiId(transaction.getSenderUpiId().getUpiId());
        response.setReceiverUpiId(transaction.getReceiverUpiId().getUpiId());
        response.setAmount(transaction.getAmount());
        response.setRemarks(transaction.getRemarks());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    private TransactionHistoryResponse convertToHistoryResponse(Transaction transaction) {
        TransactionHistoryResponse response = new TransactionHistoryResponse();
        response.setTransactionReference(transaction.getTransactionReference());
        response.setSenderUpiId(transaction.getSenderUpiId().getUpiId());
        response.setReceiverUpiId(transaction.getReceiverUpiId().getUpiId());
        response.setAmount(transaction.getAmount());
        response.setStatus(transaction.getStatus());
        response.setRemarks(transaction.getRemarks());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    @Override
    @Transactional
    public TransactionResponse sendMoney(SendMoneyRequest request) {
        // 1 & 2: Validate authenticated user and sender account ownership
        BankAccount senderAccount = getOwnedBankAccount(request.getSenderBankAccountId());

        // 3 & 4: Find receiver UPI ID and ensure it exists
        UpiId receiverUpi = upiIdRepository.findByUpiId(request.getReceiverUpiId())
                .orElseThrow(() -> new IllegalArgumentException("Receiver UPI ID not found"));

        BankAccount receiverAccount = receiverUpi.getBankAccount();

        // 5: Receiver UPI status must be ACTIVE
        if (receiverUpi.getStatus() != UpiStatus.ACTIVE) {
            throw new IllegalStateException("Receiver UPI ID is not active");
        }

        // 6: Sender account status must be ACTIVE
        if (senderAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Sender bank account is not active");
        }

        // 7: Receiver account status must be ACTIVE
        if (receiverAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Receiver bank account is not active");
        }

        // 8: Prevent self-transfer
        if (senderAccount.getId().equals(receiverAccount.getId())) {
            throw new IllegalArgumentException("Cannot transfer money to the same bank account");
        }

        // 9: Verify UPI PIN securely via upiPinService
        VerifyUpiPinRequest verifyReq = new VerifyUpiPinRequest();
        verifyReq.setBankAccountId(senderAccount.getId());
        verifyReq.setPin(request.getUpiPin());
        
        if (!upiPinService.verifyUpiPin(verifyReq)) {
            throw new SecurityException("Invalid UPI PIN");
        }

        // 10: Amount must be greater than zero
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero");
        }

        // 11: Sender balance must be sufficient
        if (senderAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalStateException("Insufficient balance");
        }

        // 12: Generate unique transaction reference
        String txnRef = generateTransactionReference();

        // 13: Debit sender account
        senderAccount.setBalance(senderAccount.getBalance().subtract(request.getAmount()));

        // 14: Credit receiver account
        receiverAccount.setBalance(receiverAccount.getBalance().add(request.getAmount()));

        // 15: Create Transaction entity
        Transaction transaction = new Transaction();
        
        // 16: Set fields
        transaction.setTransactionReference(txnRef);
        transaction.setSenderBankAccount(senderAccount);
        transaction.setReceiverBankAccount(receiverAccount);
        
        // Find sender's primary UPI ID to attach to the record
        UpiId senderUpi = upiIdRepository.findByBankAccountAndIsPrimaryTrue(senderAccount)
                .orElseGet(() -> upiIdRepository.findByBankAccount(senderAccount).stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("Sender has no linked UPI ID")));

        transaction.setSenderUpiId(senderUpi);
        transaction.setReceiverUpiId(receiverUpi);
        transaction.setAmount(request.getAmount());
        transaction.setRemarks(request.getRemarks());
        transaction.setStatus(TransactionStatus.SUCCESS);

        // 17 & 18: Save accounts
        bankAccountRepository.save(senderAccount);
        bankAccountRepository.save(receiverAccount);
        
        // 19: Save transaction
        transaction = transactionRepository.save(transaction);

        // Publish event to Kafka after successful database commit
        publishTransactionEvent(transaction);

        // 20: Return TransactionResponse
        return convertToTransactionResponse(transaction);
    }

    @Override
    public List<TransactionHistoryResponse> getTransactionHistory() {
        User currentUser = getCurrentUser();
        List<BankAccount> userAccounts = bankAccountRepository.findAllByUser(currentUser);

        // Aggregate transactions from all of the user's bank accounts, remove duplicates (for internal transfers), and sort by latest
        return userAccounts.stream()
                .flatMap(acc -> transactionRepository.findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc(acc, acc).stream())
                .distinct()
                .sorted((t1, t2) -> t2.getCreatedAt().compareTo(t1.getCreatedAt()))
                .map(this::convertToHistoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponse getTransactionByReference(String transactionReference) {
        Transaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found for reference: " + transactionReference));
        
        User currentUser = getCurrentUser();
        Long senderUserId = transaction.getSenderBankAccount().getUser().getId();
        Long receiverUserId = transaction.getReceiverBankAccount().getUser().getId();
        
        // IDOR Protection: Ensure the user was a party in this transaction
        if (!currentUser.getId().equals(senderUserId) && !currentUser.getId().equals(receiverUserId)) {
            throw new UnauthorizedAccountAccessException("You are not authorized to view this transaction.");
        }
        
        return convertToTransactionResponse(transaction);
    }

    @Override
    public TransactionSummaryResponse getTransactionSummary() {
        User currentUser = getCurrentUser();
        List<BankAccount> userAccounts = bankAccountRepository.findAllByUser(currentUser);

        // Retrieve all unique transactions for all user's accounts
        List<Transaction> allTransactions = userAccounts.stream()
                .flatMap(acc -> transactionRepository.findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc(acc, acc).stream())
                .distinct()
                .collect(Collectors.toList());

        BigDecimal totalSent = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;

        for (Transaction txn : allTransactions) {
            if (txn.getStatus() == TransactionStatus.SUCCESS) {
                Long senderUserId = txn.getSenderBankAccount().getUser().getId();
                Long receiverUserId = txn.getReceiverBankAccount().getUser().getId();

                if (currentUser.getId().equals(senderUserId)) {
                    totalSent = totalSent.add(txn.getAmount());
                }
                
                if (currentUser.getId().equals(receiverUserId)) {
                    totalReceived = totalReceived.add(txn.getAmount());
                }
            }
        }

        TransactionSummaryResponse response = new TransactionSummaryResponse();
        response.setTotalSent(totalSent);
        response.setTotalReceived(totalReceived);
        response.setTotalTransactions((long) allTransactions.size());
        
        return response;
    }
}
