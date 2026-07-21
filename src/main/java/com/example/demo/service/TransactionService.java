package com.example.demo.service;

import com.example.demo.dto.SendMoneyRequest;
import com.example.demo.dto.TransactionHistoryResponse;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.dto.TransactionSummaryResponse;

import java.util.List;

/**
 * Service Interface for managing financial transactions.
 * 
 * <h3>Architecture and Design Principles:</h3>
 * 
 * <p><b>Why Service Interfaces improve loose coupling:</b></p>
 * <p>By coding to an interface rather than a concrete class, dependent components (like Controllers) 
 * remain completely unaware of the underlying implementation details. This makes the system more modular, 
 * allowing developers to replace or upgrade implementations without breaking dependent code.</p>
 * 
 * <p><b>Dependency Injection:</b></p>
 * <p>Spring's Inversion of Control (IoC) container injects the correct implementation of this interface 
 * at runtime. This avoids hardcoded instantiation using the 'new' keyword, making the application 
 * highly configurable and easier to manage.</p>
 * 
 * <p><b>Unit testing with Mockito:</b></p>
 * <p>When testing a Controller that relies on this service, we don't want to hit the database or execute 
 * actual financial logic. Mockito can easily generate a mock proxy of this interface, allowing us to define 
 * expected behaviors (e.g., using {@code when(service.sendMoney(...)).thenReturn(...)}) to isolate and test 
 * the Controller instantly.</p>
 * 
 * <p><b>Spring AOP (Aspect-Oriented Programming):</b></p>
 * <p>Features like {@code @Transactional}, caching, and security checks rely heavily on AOP. Spring uses standard 
 * Java dynamic proxies to intercept method calls and apply these cross-cutting concerns. These proxies 
 * operate optimally when backing a standard Java Interface, preventing the need for complex CGLIB class-based proxying.</p>
 * 
 * <p><b>Why business logic belongs inside Services instead of Controllers:</b></p>
 * <p>Controllers should exclusively handle HTTP protocol parsing, routing, and input validation. 
 * Services handle the actual core domain logic. Mixing them creates massive "god classes" that are 
 * impossible to test and reuse. If another part of the system (or a scheduled background job) needs to initiate 
 * a transaction, it can call this Service directly without fabricating an HTTP request.</p>
 * 
 * <p><b>Why financial systems separate persistence from business rules:</b></p>
 * <p>Repositories only know how to save and fetch data. The Service layer knows the rules (e.g., a user 
 * cannot send money if their balance is insufficient, or if the UPI PIN is wrong). By separating these, 
 * the persistence layer remains agnostic to the business, and the business logic remains agnostic to the 
 * underlying database technology, ensuring high code integrity and maintainability.</p>
 */
public interface TransactionService {

    /**
     * Initiates a financial transfer from the authenticated user's bank account to a receiver's UPI ID.
     *
     * @param request The data transfer object containing the sender's account ID, receiver's UPI ID, 
     *                the amount to transfer, optional remarks, and the sender's UPI PIN for authorization.
     * @return A {@link TransactionResponse} containing the generated transaction reference, status, and details.
     * @throws IllegalArgumentException If the provided request data is structurally invalid.
     * @throws SecurityException If the provided UPI PIN is incorrect.
     * @throws IllegalStateException If the sender has insufficient balance or the account is inactive.
     * @throws com.example.demo.exception.AccountNotFoundException If sender or receiver accounts do not exist.
     */
    TransactionResponse sendMoney(SendMoneyRequest request);

    /**
     * Retrieves the chronological transaction history for the currently authenticated user.
     *
     * @return A list of {@link TransactionHistoryResponse} objects, sorted by most recent first.
     * @throws com.example.demo.exception.UserNotFoundException If the authenticated user cannot be identified in the system.
     */
    List<TransactionHistoryResponse> getTransactionHistory();

    /**
     * Retrieves the specific details of a transaction using its unique reference number.
     *
     * @param transactionReference The unique string reference identifying the transaction.
     * @return A {@link TransactionResponse} containing the detailed state of the transaction.
     * @throws IllegalArgumentException If no transaction matches the reference.
     * @throws com.example.demo.exception.UnauthorizedAccountAccessException If the authenticated user is neither the sender nor the receiver.
     */
    TransactionResponse getTransactionByReference(String transactionReference);

    /**
     * Calculates and aggregates the financial summary for the currently authenticated user, 
     * such as total money sent, total money received, and the total number of transactions.
     *
     * @return A {@link TransactionSummaryResponse} containing the aggregated analytics.
     * @throws com.example.demo.exception.UserNotFoundException If the authenticated user cannot be identified.
     */
    TransactionSummaryResponse getTransactionSummary();
}
