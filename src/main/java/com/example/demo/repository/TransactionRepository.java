package com.example.demo.repository;

import com.example.demo.entity.BankAccount;
import com.example.demo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Transaction persistence.
 * 
 * <h3>Architecture and Persistence Principles:</h3>
 * 
 * <p><b>Why repositories should only perform persistence operations & never contain business logic:</b></p>
 * <p>The Repository layer's single responsibility is to interact with the database (CRUD operations and queries). 
 * By keeping business logic (like checking if a user has sufficient balance) out of the repository, we ensure 
 * separation of concerns. Business rules belong in the Service layer, which orchestrates calls to multiple 
 * repositories. This keeps repositories highly reusable, focused, and testable.</p>
 * 
 * <p><b>How Spring Data JPA derives queries from method names:</b></p>
 * <p>Spring Data JPA uses a Query Builder mechanism. By adhering to a specific naming convention 
 * (e.g., {@code findBy} + PropertyName), Spring automatically parses the method signature at startup and generates 
 * the corresponding SQL query. For complex logic, keywords like {@code Or}, {@code And}, and {@code OrderBy} 
 * can be chained seamlessly without writing manual SQL or JPQL.</p>
 * 
 * <p><b>Why Optional is preferred over returning null:</b></p>
 * <p>Returning {@code Optional<Transaction>} explicitly communicates to the caller (Service layer) that 
 * a result might not exist. This forces the developer to handle the "not found" scenario explicitly 
 * (e.g., using {@code .orElseThrow()}), effectively eliminating the risk of unexpected {@code NullPointerException}s.</p>
 * 
 * <p><b>Why unique transaction references are important:</b></p>
 * <p>Queries like {@code existsByTransactionReference} are crucial for idempotency. If a client attempts 
 * a transaction multiple times due to a network timeout, the Service layer can query the repository 
 * using the unique reference to see if it was already processed, thereby preventing duplicate debits.</p>
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Finds a specific transaction by its unique reference string.
     */
    Optional<Transaction> findByTransactionReference(String transactionReference);

    /**
     * Checks if a transaction reference already exists in the database.
     */
    boolean existsByTransactionReference(String transactionReference);

    /**
     * Retrieves all transactions where the specified bank account was the sender.
     */
    List<Transaction> findBySenderBankAccount(BankAccount senderBankAccount);

    /**
     * Retrieves all transactions where the specified bank account was the receiver.
     */
    List<Transaction> findByReceiverBankAccount(BankAccount receiverBankAccount);

    /**
     * Retrieves the complete transaction history for a bank account (both sent and received),
     * ordered by the creation timestamp in descending order.
     * 
     * <p><b>Why transaction history should be sorted by newest first:</b></p>
     * <p>In financial and dashboard applications, users are almost always looking for their most 
     * recent activity. Sorting by {@code CreatedAtDesc} at the database level is highly efficient 
     * (especially when indexed) and ensures the UI immediately displays the most relevant information 
     * at the top of the feed without needing to sort lists in Java memory.</p>
     */
    List<Transaction> findBySenderBankAccountOrReceiverBankAccountOrderByCreatedAtDesc(
            BankAccount senderBankAccount,
            BankAccount receiverBankAccount
    );
}
