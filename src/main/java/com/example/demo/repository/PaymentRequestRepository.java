package com.example.demo.repository;

import com.example.demo.entity.PaymentRequest;
import com.example.demo.entity.PaymentRequestStatus;
import com.example.demo.entity.UpiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing PaymentRequest persistence.
 * 
 * WHY WE USE SPRING DATA JPA:
 * By extending JpaRepository, Spring automatically generates the implementation 
 * for standard CRUD operations at runtime. We don't have to write boilerplate 
 * SQL queries. 
 * 
 * HOW METHOD NAME QUERIES WORK:
 * Spring parses method names (e.g., findBySenderUpiIdAndStatus) and automatically 
 * translates them into SQL queries (SELECT * FROM payment_requests WHERE 
 * sender_upi_id = ? AND status = ?).
 */
@Repository
public interface PaymentRequestRepository extends JpaRepository<PaymentRequest, Long> {

    /**
     * Finds a specific payment request by its unique reference string.
     * Returns Optional to prevent NullPointerExceptions.
     */
    Optional<PaymentRequest> findByRequestReference(String requestReference);

    /**
     * Checks if a request reference already exists to ensure uniqueness.
     */
    boolean existsByRequestReference(String requestReference);

    /**
     * Finds all requests initiated BY a specific UPI ID (they are the receiver of the money).
     * Typically used to show "Sent Requests" in the UI.
     */
    List<PaymentRequest> findByReceiverUpiIdOrderByCreatedAtDesc(UpiId receiverUpiId);

    /**
     * Finds all requests sent TO a specific UPI ID (they are the sender of the money).
     * Typically used to show "Received Requests" in the UI.
     */
    List<PaymentRequest> findBySenderUpiIdOrderByCreatedAtDesc(UpiId senderUpiId);

    /**
     * Finds all pending requests for a user so they can accept or reject them.
     */
    List<PaymentRequest> findBySenderUpiIdAndStatusOrderByCreatedAtDesc(UpiId senderUpiId, PaymentRequestStatus status);
}
