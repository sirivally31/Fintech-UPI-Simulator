package com.example.demo.controller;

import com.example.demo.dto.BankAccountResponse;
import com.example.demo.dto.CreateBankAccountRequest;
import com.example.demo.dto.UpdateBankAccountRequest;
import com.example.demo.service.BankAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST Controller for managing Bank Accounts.
 * 
 * Why controllers should remain thin:
 * The controller's primary responsibility is HTTP request routing, reading inputs, 
 * delegating the heavy lifting (business logic) to the Service layer, and returning the 
 * appropriate HTTP response. Thin controllers make the application significantly easier 
 * to test, read, and maintain by upholding a strict separation of concerns.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Bank Account APIs", description = "Endpoints for managing user bank accounts")
public class BankAccountController {

    private final BankAccountService bankAccountService;

    /**
     * Constructor Injection for BankAccountService.
     * This guarantees the controller is never instantiated without its required service dependency.
     */
    public BankAccountController(BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    /**
     * Creates a new bank account for the authenticated user.
     * 
     * @param request the DTO containing the details for the new bank account
     * @return a ResponseEntity containing the created BankAccountResponse and a 201 Created status
     */
    @Operation(summary = "Create a new bank account", description = "Links a new bank account to the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Bank account created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error (e.g., invalid account number)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PostMapping
    public ResponseEntity<BankAccountResponse> createAccount(
            // Why @Valid is used:
            // @Valid triggers the Jakarta Bean Validation framework to validate the incoming JSON 
            // against the constraints defined in our DTO (like @NotBlank, @Size) before the method 
            // even begins executing. This immediately blocks invalid data from reaching the service layer.
            @Valid @RequestBody CreateBankAccountRequest request) {
        
        BankAccountResponse response = bankAccountService.createAccount(request);
        
        // Why ResponseEntity is used:
        // ResponseEntity provides full programmatic control over the HTTP response (body, headers, and status code). 
        // Here we explicitly return HTTP 201 (Created), which aligns with REST principles when a new resource is generated.
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Retrieves all bank accounts belonging to the currently authenticated user.
     * 
     * @return a ResponseEntity containing a list of BankAccountResponse and a 200 OK status
     */
    @Operation(summary = "Get all bank accounts", description = "Retrieves all bank accounts linked to the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of bank accounts retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @GetMapping
    public ResponseEntity<List<BankAccountResponse>> getAllAccounts() {
        // We delegate entirely to the service. The controller does not retrieve the SecurityContext 
        // or query the database; the service handles all of those business rules.
        List<BankAccountResponse> responses = bankAccountService.getAllAccounts();
        return ResponseEntity.ok(responses);
    }

    /**
     * Retrieves a specific bank account by its ID.
     * 
     * @param id the unique identifier of the bank account
     * @return a ResponseEntity containing the BankAccountResponse and a 200 OK status
     */
    @Operation(summary = "Get a bank account by ID", description = "Retrieves details of a specific bank account if it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank account retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found or does not belong to user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BankAccountResponse> getAccountById(@PathVariable Long id) {
        // REST principles followed: The URL path '/api/accounts/{id}' explicitly identifies 
        // a specific resource using standard path variables.
        BankAccountResponse response = bankAccountService.getAccountById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates specific fields of an existing bank account.
     * 
     * @param id the unique identifier of the bank account to update
     * @param request the DTO containing the fields to update
     * @return a ResponseEntity containing the updated BankAccountResponse and a 200 OK status
     */
    @Operation(summary = "Update a bank account", description = "Updates specific fields of an existing bank account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bank account updated successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PutMapping("/{id}")
    public ResponseEntity<BankAccountResponse> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody UpdateBankAccountRequest request) {
        
        BankAccountResponse response = bankAccountService.updateAccount(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a specific bank account.
     * 
     * @param id the unique identifier of the bank account to delete
     * @return a ResponseEntity with no body and a 204 No Content status
     */
    @Operation(summary = "Delete a bank account", description = "Removes a specific bank account linked to the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Bank account deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long id) {
        bankAccountService.deleteAccount(id);
        
        // Returning HTTP 204 (No Content) is the widely accepted RESTful standard for a successful 
        // DELETE operation, signaling to the client that the action succeeded but there is no payload to parse.
        return ResponseEntity.noContent().build();
    }
}
