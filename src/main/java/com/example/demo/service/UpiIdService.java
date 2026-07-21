package com.example.demo.service;

import com.example.demo.dto.CreateUpiIdRequest;
import com.example.demo.dto.UpdateUpiIdRequest;
import com.example.demo.dto.UpiIdResponse;

import java.util.List;

/**
 * Service Interface for managing UPI IDs.
 * 
 * Why Spring applications prefer service interfaces:
 * 
 * 1. Loose Coupling: By programming to an interface rather than a concrete class, the controller 
 *    is decoupled from the actual business logic implementation. This reduces the risk of breaking 
 *    changes cascading across the application.
 * 
 * 2. Dependency Injection (DI): Spring's IoC container naturally injects the required implementation 
 *    at runtime. This promotes inversion of control and cleaner architecture.
 * 
 * 3. Unit Testing with Mockito: Interfaces make unit testing controllers extremely easy. We can use Mockito 
 *    to mock this interface and define its behavior without worrying about database connections or complex 
 *    internal logic present in the concrete implementation.
 * 
 * 4. Spring AOP and Proxy-based Architecture: Spring heavily relies on Aspect-Oriented Programming (AOP) 
 *    for features like @Transactional and @PreAuthorize. Spring creates JDK Dynamic Proxies at runtime 
 *    by wrapping the interface. Implementing interfaces ensures seamless AOP functionality.
 * 
 * 5. Swappable Implementations: If the underlying business logic changes (e.g., migrating from a local DB 
 *    to a third-party UPI provider API), we can simply create a new implementation of this interface and 
 *    swap it in via configuration, without modifying a single line of code in the Controller.
 */
public interface UpiIdService {

    /**
     * Purpose: Creates a new UPI ID for the currently authenticated user based on their bank account.
     * 
     * @param request the DTO containing the bank account ID and preferred UPI handle.
     * @return UpiIdResponse containing the created UPI ID details safely mapped from the entity.
     * @throws RuntimeException (or specific business exception like ResourceNotFoundException) if the bank account is not found or not owned by the user.
     * @throws IllegalArgumentException (or DataIntegrityViolationException) if the UPI ID already exists.
     */
    UpiIdResponse createUpiId(CreateUpiIdRequest request);

    /**
     * Purpose: Retrieves all UPI IDs associated with the currently authenticated user.
     * 
     * @return a List of UpiIdResponse objects representing the user's UPI IDs.
     *         Returns an empty list if no UPI IDs are found.
     * @throws RuntimeException if user authentication context is missing.
     */
    List<UpiIdResponse> getAllUpiIds();

    /**
     * Purpose: Retrieves a specific UPI ID by its database identifier, ensuring it belongs to the authenticated user.
     * 
     * @param id the primary key of the UPI ID.
     * @return UpiIdResponse containing the details of the requested UPI ID.
     * @throws RuntimeException (or specific business exception) if the UPI ID is not found or access is denied.
     */
    UpiIdResponse getUpiId(Long id);

    /**
     * Purpose: Updates the status or primary flag of an existing UPI ID.
     * 
     * @param id the primary key of the UPI ID to update.
     * @param request the DTO containing the update parameters (e.g., status, isPrimary).
     * @return UpiIdResponse containing the updated details.
     * @throws RuntimeException (or specific business exception) if the UPI ID is not found or access is denied.
     */
    UpiIdResponse updateUpiId(Long id, UpdateUpiIdRequest request);

    /**
     * Purpose: Deletes a specific UPI ID from the system.
     * 
     * @param id the primary key of the UPI ID to be deleted.
     * @throws RuntimeException (or specific business exception) if the UPI ID is not found or access is denied.
     */
    void deleteUpiId(Long id);

    /**
     * Purpose: Sets a specific UPI ID as the primary/default choice for the user's transactions,
     * simultaneously unsetting any previously marked primary UPI ID for that bank account.
     * 
     * @param id the primary key of the UPI ID to be marked as primary.
     * @return UpiIdResponse containing the updated UPI ID details.
     * @throws RuntimeException (or specific business exception) if the UPI ID is not found or access is denied.
     */
    UpiIdResponse setPrimaryUpiId(Long id);
}
