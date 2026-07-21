package com.example.demo.repository;

import com.example.demo.entity.BankAccount;
import com.example.demo.entity.UpiId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing UpiId entities.
 * 
 * Why repositories should only handle persistence and not business rules:
 * The repository layer acts as a facade over the data access technology (JPA/Hibernate).
 * Keeping business logic out of the repository ensures a clear separation of concerns. 
 * The business rules belong in the Service layer, which keeps the repository layer simple,
 * focused solely on data retrieval and storage, and easily mockable for unit testing.
 * 
 * How Spring Data JPA derives SQL queries from method names:
 * Spring Data JPA parses the method names (e.g., findBy..., existsBy...) to automatically generate
 * the corresponding SQL queries at runtime. By combining entity property names (like UpiId, BankAccount)
 * and keywords (like And, True), developers can avoid writing explicit SQL queries for standard operations.
 */
@Repository
public interface UpiIdRepository extends JpaRepository<UpiId, Long> {

    /**
     * Why Optional is preferred over returning null:
     * Returning an Optional explicitly signals to the caller that the result might not exist.
     * It forces the developer to handle the absent case (e.g., throwing an exception or providing a default value),
     * effectively preventing unexpected NullPointerExceptions and making the API contract clearer.
     */
    Optional<UpiId> findByUpiId(String upiId);

    boolean existsByUpiId(String upiId);

    List<UpiId> findByBankAccount(BankAccount bankAccount);

    /**
     * Why findByIdAndBankAccount() improves security by preventing unauthorized access:
     * This method retrieves a UpiId only if it matches both the specific 'id' and the 'bankAccount'.
     * By requiring the 'bankAccount' (which is derived from the authenticated user), we prevent 
     * Insecure Direct Object Reference (IDOR) vulnerabilities, where a malicious user could otherwise fetch
     * another user's UPI ID simply by guessing its primary key 'id'.
     */
    Optional<UpiId> findByIdAndBankAccount(Long id, BankAccount bankAccount);

    Optional<UpiId> findByBankAccountAndIsPrimaryTrue(BankAccount bankAccount);
}
