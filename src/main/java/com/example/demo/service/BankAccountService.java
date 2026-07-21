package com.example.demo.service;

import com.example.demo.dto.BankAccountResponse;
import com.example.demo.dto.CreateBankAccountRequest;
import com.example.demo.dto.UpdateBankAccountRequest;
import java.util.List;

/**
 * Interface defining the business operations for Bank Accounts.
 * 
 * Why Spring Projects Use Interfaces for Services:
 * 1. Loose Coupling & Abstraction: The Presentation layer (Controller) depends only on this interface contract, 
 *    not on how the actual business logic is implemented. This makes the system modular and easy to refactor.
 * 2. Ease of Unit Testing: Interfaces allow for easy creation of mock implementations (using libraries like Mockito) 
 *    during unit testing, without having to instantiate complex real service classes and their dependencies.
 * 3. Spring AOP & Proxies: Spring heavily relies on JDK Dynamic Proxies for features like @Transactional, @Cacheable, 
 *    and method-level security. These proxies wrap around interfaces to intercept calls seamlessly.
 * 4. Multiple Implementations: An interface provides the flexibility to swap out implementations 
 *    (e.g., a real DB implementation vs. a caching implementation) without changing any Controller code.
 */
public interface BankAccountService {

    /**
     * Creates a new bank account based on the provided validation-checked request data.
     * 
     * @param request the DTO containing necessary fields (account number, IFSC, balance, etc.) to create a bank account
     * @return a DTO containing the newly created bank account details, safe for the presentation layer
     */
    BankAccountResponse createAccount(CreateBankAccountRequest request);

    /**
     * Retrieves a list of bank accounts.
     * 
     * @return a list of DTOs representing the bank accounts
     */
    List<BankAccountResponse> getAllAccounts();

    /**
     * Retrieves a specific bank account by its unique identifier.
     * 
     * @param accountId the unique database ID of the bank account
     * @return a DTO containing the details of the requested bank account
     */
    BankAccountResponse getAccountById(Long accountId);

    /**
     * Updates an existing bank account using the provided request data.
     * 
     * @param accountId the unique database ID of the bank account to be updated
     * @param request the DTO containing the fields allowed to be updated
     * @return a DTO containing the newly updated bank account details
     */
    BankAccountResponse updateAccount(Long accountId, UpdateBankAccountRequest request);

    /**
     * Removes a specific bank account from the system by its unique identifier.
     * 
     * @param accountId the unique database ID of the bank account to delete
     */
    void deleteAccount(Long accountId);
}
