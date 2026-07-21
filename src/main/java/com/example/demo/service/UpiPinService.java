package com.example.demo.service;

import com.example.demo.dto.ChangeUpiPinRequest;
import com.example.demo.dto.SetUpiPinRequest;
import com.example.demo.dto.VerifyUpiPinRequest;

/**
 * Service Interface for managing UPI PIN operations.
 * 
 * <h3>Architecture and Design Principles:</h3>
 * 
 * <p><b>Why Spring applications prefer Service Interfaces:</b></p>
 * <p>Using interfaces for services is a core best practice in Spring applications. It establishes a contract
 * for the business operations without exposing the underlying implementation details. This abstraction allows
 * developers to easily switch out implementations or mock them during testing.</p>
 * 
 * <p><b>Loose Coupling:</b></p>
 * <p>Interfaces promote loose coupling by ensuring that dependent components (like Controllers) only know 
 * about the API (the interface) and not the concrete implementation. This separation of concerns makes the 
 * system more modular, easier to maintain, and less prone to cascading changes.</p>
 * 
 * <p><b>Dependency Injection:</b></p>
 * <p>Spring's Inversion of Control (IoC) container easily injects the correct implementation of an interface 
 * at runtime. This avoids hardcoding dependencies and makes the components easily configurable and flexible.</p>
 * 
 * <p><b>Unit testing with Mockito:</b></p>
 * <p>Interfaces make unit testing highly efficient. When testing a Controller that depends on this service, 
 * Mockito can seamlessly create a mock implementation of this interface. This isolates the Controller's logic 
 * and ensures tests are fast and reliable without needing to load actual database or complex service logic.</p>
 * 
 * <p><b>Spring AOP and proxy-based architecture:</b></p>
 * <p>Spring relies heavily on Aspect-Oriented Programming (AOP) for features like @Transactional and caching. 
 * Spring creates dynamic proxies around beans to intercept method calls and apply these behaviors. Standard 
 * Java dynamic proxies require interfaces. By using a Service Interface, Spring can easily generate these proxies 
 * without relying on class-based proxies (like CGLIB), resulting in a more standard and performant architecture.</p>
 * 
 * <p><b>Why business logic belongs in the Service layer:</b></p>
 * <p>The Service layer acts as the orchestrator of business rules. 
 * Controllers should only handle HTTP request/response routing and initial validation. 
 * Repositories should only handle database interactions. 
 * By keeping business logic in the Service layer, we ensure it remains reusable, decoupled from presentation 
 * or persistence details, and highly testable.</p>
 */
public interface UpiPinService {

    /**
     * Sets a new UPI PIN for a bank account that currently does not have one.
     *
     * @param request The data transfer object containing the necessary information to set the UPI PIN, 
     *                such as the bank account identifier, debit card details (for validation), and the new PIN.
     * @throws IllegalArgumentException If the provided request data is invalid or missing required fields.
     * @throws IllegalStateException If a UPI PIN is already set for the account.
     * @throws RuntimeException (or specific business exceptions) If validation of account or debit card fails.
     */
    void setUpiPin(SetUpiPinRequest request);

    /**
     * Changes an existing UPI PIN to a new one.
     *
     * @param request The data transfer object containing the necessary information to change the UPI PIN, 
     *                such as the bank account identifier, the old PIN (for verification), and the new PIN.
     * @throws IllegalArgumentException If the provided request data is invalid.
     * @throws SecurityException (or similar business exception) If the provided old PIN does not match the currently stored PIN.
     * @throws IllegalStateException If no UPI PIN is currently set for the account.
     */
    void changeUpiPin(ChangeUpiPinRequest request);

    /**
     * Verifies if a provided UPI PIN is correct for a given bank account.
     * This is typically used to authorize financial transactions.
     *
     * @param request The data transfer object containing the bank account identifier and the PIN to be verified.
     * @return {@code true} if the provided PIN matches the stored encrypted PIN for the account; {@code false} otherwise.
     * @throws IllegalArgumentException If the request data is incomplete or invalid.
     * @throws IllegalStateException If no UPI PIN is currently configured for the account.
     */
    boolean verifyUpiPin(VerifyUpiPinRequest request);
}
