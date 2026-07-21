package com.example.demo.repository;

import com.example.demo.entity.BankAccount;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * BankAccountRepository provides database access for the BankAccount entity.
 * 
 * Why JpaRepository is used instead of implementing DAO manually:
 * Spring Data JPA provides the JpaRepository interface, which comes with out-of-the-box
 * implementations for standard CRUD (Create, Read, Update, Delete) and pagination operations.
 * This completely eliminates the need to write boilerplate DAO (Data Access Object) 
 * implementations, connection management, and basic SQL queries manually, keeping the code clean
 * and adhering to production-level standards.
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    /**
     * How Spring Data JPA generates SQL from method names:
     * Spring Data JPA parses the method name by identifying prefixes like 'findBy', 'existsBy'.
     * It then maps the remainder of the method name to properties of the entity (e.g., 'AccountNumber').
     * Behind the scenes, it automatically generates and executes the corresponding SQL query
     * (e.g., SELECT * FROM bank_accounts WHERE account_number = ?).
     *
     * The purpose of Optional:
     * Returning Optional<T> instead of a direct object reference is a best practice that helps 
     * prevent NullPointerExceptions. It explicitly tells the caller that the result might be null
     * (if no record is found) and forces them to handle that case gracefully.
     */
    Optional<BankAccount> findByAccountNumber(String accountNumber);

    /**
     * Returns true if a bank account with the given account number already exists.
     * Automatically translated to an efficient SQL EXISTS or COUNT query based on the accountNumber.
     */
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Finds all bank accounts that belong to a specific User.
     * The query is automatically mapped against the 'user' field (and its foreign key user_id) in BankAccount.
     */
    List<BankAccount> findAllByUser(User user);

    /**
     * Finds a specific bank account by its primary key ID, but only if it belongs to the provided User.
     * 
     * Security benefits of findByIdAndUser():
     * This method intrinsically improves security by preventing Insecure Direct Object Reference (IDOR) 
     * vulnerabilities. It ensures a user can only access or act upon their own bank accounts. 
     * Even if a malicious user guesses another person's valid BankAccount ID, this query will simply 
     * return an empty Optional because the 'User' parameter (representing the currently authenticated user) 
     * won't match the account's actual owner.
     */
    Optional<BankAccount> findByIdAndUser(Long id, User user);
}
