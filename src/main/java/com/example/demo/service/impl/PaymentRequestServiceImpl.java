package com.example.demo.service.impl;

import com.example.demo.dto.*;
import com.example.demo.entity.*;
import com.example.demo.events.*;
import com.example.demo.repository.PaymentRequestRepository;
import com.example.demo.repository.UpiIdRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OutboxService;
import com.example.demo.service.PaymentRequestService;
import com.example.demo.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentRequestServiceImpl implements PaymentRequestService {

    private static final Logger log = LoggerFactory.getLogger(PaymentRequestServiceImpl.class);

    private final PaymentRequestRepository paymentRequestRepository;
    private final UpiIdRepository upiIdRepository;
    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final OutboxService outboxService;

    public PaymentRequestServiceImpl(PaymentRequestRepository paymentRequestRepository,
                                     UpiIdRepository upiIdRepository,
                                     UserRepository userRepository,
                                     TransactionService transactionService,
                                     OutboxService outboxService) {
        this.paymentRequestRepository = paymentRequestRepository;
        this.upiIdRepository = upiIdRepository;
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.outboxService = outboxService;
    }

    private User getCurrentUser() {
        String upiIdStr = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUpiId(upiIdStr)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
    }

    private UpiId getPrimaryUpiIdForUser(User user) {
        // Assume user has a bank account and a primary UPI ID. We must query through upiIdRepository.
        // We find by user. Wait, UpiId has a BankAccount, BankAccount has a User.
        return upiIdRepository.findAll().stream()
                .filter(upi -> upi.getBankAccount().getUser().getId().equals(user.getId()) && upi.isPrimary())
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No primary UPI ID found for current user"));
    }

    private String generateReference() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int sequence = 1;
        String ref;
        do {
            ref = String.format("REQ%s%04d", datePart, sequence++);
        } while (paymentRequestRepository.existsByRequestReference(ref));
        return ref;
    }

    private void validatePendingRequest(PaymentRequest req) {
        if (req.getStatus() != PaymentRequestStatus.PENDING) {
            throw new IllegalStateException("Payment request is not in PENDING state");
        }
        if (req.getExpiresAt().isBefore(LocalDateTime.now())) {
            req.setStatus(PaymentRequestStatus.EXPIRED);
            paymentRequestRepository.save(req);
            throw new IllegalStateException("Payment request has expired");
        }
    }

    private void validateReceiver(PaymentRequest req, User currentUser) {
        // In a payment request, the 'senderUpiId' is the person who is asked to pay.
        // Therefore, the current user (the person accepting/rejecting) MUST own the 'senderUpiId'.
        if (!req.getSenderUpiId().getBankAccount().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You are not authorized to respond to this request");
        }
    }

    private PaymentRequestResponse mapToResponse(PaymentRequest req) {
        PaymentRequestResponse resp = new PaymentRequestResponse();
        resp.setId(req.getId());
        resp.setRequestReference(req.getRequestReference());
        resp.setSenderUpiId(req.getSenderUpiId().getUpiId());
        resp.setReceiverUpiId(req.getReceiverUpiId().getUpiId());
        resp.setAmount(req.getAmount());
        resp.setNote(req.getNote());
        resp.setStatus(req.getStatus());
        resp.setCreatedAt(req.getCreatedAt());
        resp.setExpiresAt(req.getExpiresAt());
        resp.setRespondedAt(req.getRespondedAt());
        return resp;
    }

    @Override
    @Transactional
    public PaymentRequestResponse createRequest(CreatePaymentRequestRequest request) {
        User currentUser = getCurrentUser();
        UpiId receiverUpiId = getPrimaryUpiIdForUser(currentUser); // the one requesting money

        UpiId senderUpiId = upiIdRepository.findByUpiId(request.getReceiverUpiId())
                .orElseThrow(() -> new IllegalArgumentException("Target UPI ID not found"));

        if (senderUpiId.getStatus() != UpiStatus.ACTIVE) {
            throw new IllegalStateException("Target UPI ID is not active");
        }

        if (receiverUpiId.getId().equals(senderUpiId.getId())) {
            throw new IllegalArgumentException("Cannot request money from yourself");
        }

        PaymentRequest req = new PaymentRequest();
        req.setRequestReference(generateReference());
        req.setSenderUpiId(senderUpiId); // person who will pay
        req.setReceiverUpiId(receiverUpiId); // person requesting
        req.setAmount(request.getAmount());
        req.setNote(request.getNote());
        req.setStatus(PaymentRequestStatus.PENDING);

        req = paymentRequestRepository.save(req);
        publishCreatedEvent(req);
        return mapToResponse(req);
    }

    @Override
    public List<PaymentRequestResponse> getMySentRequests() {
        User currentUser = getCurrentUser();
        UpiId myUpiId = getPrimaryUpiIdForUser(currentUser);
        // I sent the request, so I am the receiver of the money
        return paymentRequestRepository.findByReceiverUpiIdOrderByCreatedAtDesc(myUpiId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    public List<PaymentRequestResponse> getMyReceivedRequests() {
        User currentUser = getCurrentUser();
        UpiId myUpiId = getPrimaryUpiIdForUser(currentUser);
        // I received the request, so I am asked to be the sender of the money
        return paymentRequestRepository.findBySenderUpiIdOrderByCreatedAtDesc(myUpiId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PaymentRequestResponse acceptRequest(Long id, AcceptPaymentRequestRequest request) {
        PaymentRequest req = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment request not found"));

        User currentUser = getCurrentUser();
        validateReceiver(req, currentUser);
        validatePendingRequest(req);

        // Delegate to transaction service to perform the actual money transfer securely.
        // It validates PIN, balances, account statuses, and saves the Transaction record.
        SendMoneyRequest sendReq = new SendMoneyRequest();
        sendReq.setSenderBankAccountId(req.getSenderUpiId().getBankAccount().getId());
        sendReq.setReceiverUpiId(req.getReceiverUpiId().getUpiId());
        sendReq.setAmount(req.getAmount());
        sendReq.setRemarks("Payment for Request: " + req.getRequestReference());
        sendReq.setUpiPin(request.getUpiPin());

        TransactionResponse txnResp = transactionService.sendMoney(sendReq);

        req.setStatus(PaymentRequestStatus.ACCEPTED);
        req.setRespondedAt(LocalDateTime.now());
        req = paymentRequestRepository.save(req);
        publishAcceptedEvent(req, txnResp != null ? txnResp.getTransactionReference() : null);
        
        return mapToResponse(req);
    }

    @Override
    @Transactional
    public PaymentRequestResponse rejectRequest(Long id, RejectPaymentRequestRequest request) {
        PaymentRequest req = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment request not found"));

        User currentUser = getCurrentUser();
        validateReceiver(req, currentUser);
        validatePendingRequest(req);

        req.setStatus(PaymentRequestStatus.REJECTED);
        req.setRespondedAt(LocalDateTime.now());
        req = paymentRequestRepository.save(req);
        publishRejectedEvent(req);

        return mapToResponse(req);
    }

    @Override
    @Transactional
    public PaymentRequestResponse cancelRequest(Long id, CancelPaymentRequestRequest request) {
        PaymentRequest req = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payment request not found"));

        User currentUser = getCurrentUser();
        // Only the person who created the request (the receiver of money) can cancel it
        if (!req.getReceiverUpiId().getBankAccount().getUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("You are not authorized to cancel this request");
        }

        validatePendingRequest(req);

        req.setStatus(PaymentRequestStatus.CANCELLED);
        req.setRespondedAt(LocalDateTime.now());
        req = paymentRequestRepository.save(req);
        publishCancelledEvent(req);

        return mapToResponse(req);
    }

    @Override
    public PaymentRequestResponse getRequestByReference(String requestReference) {
        PaymentRequest req = paymentRequestRepository.findByRequestReference(requestReference)
                .orElseThrow(() -> new IllegalArgumentException("Payment request not found"));

        User currentUser = getCurrentUser();
        Long senderUserId = req.getSenderUpiId().getBankAccount().getUser().getId();
        Long receiverUserId = req.getReceiverUpiId().getBankAccount().getUser().getId();

        if (!currentUser.getId().equals(senderUserId) && !currentUser.getId().equals(receiverUserId)) {
            throw new SecurityException("You are not authorized to view this request");
        }

        return mapToResponse(req);
    }

    private void publishCreatedEvent(PaymentRequest req) {
        String correlationId = UUID.randomUUID().toString();
        PaymentRequestCreatedEvent event = PaymentRequestCreatedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("PaymentRequestCreated")
                .correlationId(correlationId)
                .requestReference(req.getRequestReference())
                .senderUpiId(req.getSenderUpiId().getUpiId())
                .receiverUpiId(req.getReceiverUpiId().getUpiId())
                .amount(req.getAmount())
                .note(req.getNote())
                .status(req.getStatus().name())
                .expiresAt(req.getExpiresAt())
                .build();

        outboxService.saveOutboxEvent(
                event.getEventId(),
                "PAYMENT_REQUEST",
                req.getId(),
                "PAYMENT_REQUEST_CREATED",
                correlationId,
                event
        );
    }

    private void publishAcceptedEvent(PaymentRequest req, String transactionRef) {
        String correlationId = UUID.randomUUID().toString();
        PaymentRequestAcceptedEvent event = PaymentRequestAcceptedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("PaymentRequestAccepted")
                .correlationId(correlationId)
                .requestReference(req.getRequestReference())
                .senderUpiId(req.getSenderUpiId().getUpiId())
                .receiverUpiId(req.getReceiverUpiId().getUpiId())
                .amount(req.getAmount())
                .respondedAt(req.getRespondedAt())
                .transactionReference(transactionRef)
                .build();

        outboxService.saveOutboxEvent(
                event.getEventId(),
                "PAYMENT_REQUEST",
                req.getId(),
                "PAYMENT_REQUEST_ACCEPTED",
                correlationId,
                event
        );
    }

    private void publishRejectedEvent(PaymentRequest req) {
        String correlationId = UUID.randomUUID().toString();
        PaymentRequestRejectedEvent event = PaymentRequestRejectedEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("PaymentRequestRejected")
                .correlationId(correlationId)
                .requestReference(req.getRequestReference())
                .senderUpiId(req.getSenderUpiId().getUpiId())
                .receiverUpiId(req.getReceiverUpiId().getUpiId())
                .amount(req.getAmount())
                .respondedAt(req.getRespondedAt())
                .reason("Rejected by payer")
                .build();

        outboxService.saveOutboxEvent(
                event.getEventId(),
                "PAYMENT_REQUEST",
                req.getId(),
                "PAYMENT_REQUEST_REJECTED",
                correlationId,
                event
        );
    }

    private void publishCancelledEvent(PaymentRequest req) {
        String correlationId = UUID.randomUUID().toString();
        PaymentRequestCancelledEvent event = PaymentRequestCancelledEvent.builder()
                .eventId(UUID.randomUUID())
                .eventTime(LocalDateTime.now())
                .eventType("PaymentRequestCancelled")
                .correlationId(correlationId)
                .requestReference(req.getRequestReference())
                .senderUpiId(req.getSenderUpiId().getUpiId())
                .receiverUpiId(req.getReceiverUpiId().getUpiId())
                .amount(req.getAmount())
                .cancelledAt(req.getRespondedAt())
                .build();

        outboxService.saveOutboxEvent(
                event.getEventId(),
                "PAYMENT_REQUEST",
                req.getId(),
                "PAYMENT_REQUEST_CANCELLED",
                correlationId,
                event
        );
    }
}
