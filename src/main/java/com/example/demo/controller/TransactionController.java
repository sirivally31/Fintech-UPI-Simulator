package com.example.demo.controller;

import com.example.demo.dto.SendMoneyRequest;
import com.example.demo.dto.TransactionHistoryResponse;
import com.example.demo.dto.TransactionResponse;
import com.example.demo.dto.TransactionSummaryResponse;
import com.example.demo.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * Controller for handling financial transaction HTTP requests.
 * 
 * <h3>Architecture and Design Principles:</h3>
 * 
 * <p><b>Separation of Concerns & Why Controllers should remain thin:</b></p>
 * <p>In a well-architected Spring Boot application, layers have distinct, non-overlapping responsibilities. 
 * The Controller's ONLY jobs are to receive HTTP requests, parse path variables or body payloads, trigger 
 * input validation, delegate execution to the Service layer, and return an HTTP response. If a controller 
 * starts checking database balances or validating PINs, it violates the Single Responsibility Principle, 
 * creating tight coupling and making the code extremely difficult to unit test or reuse.</p>
 * 
 * <p><b>Request validation flow:</b></p>
 * <p>The {@code @Valid} annotation triggers Jakarta Bean Validation constraints (like {@code @NotNull} 
 * or {@code @DecimalMin}) defined on the incoming DTO before the controller method executes. If validation fails, 
 * Spring intercepts the request and throws a {@code MethodArgumentNotValidException}, allowing a GlobalExceptionHandler 
 * to return a structured 400 Bad Request. This acts as a security and performance shield, ensuring the Service 
 * layer only ever receives mathematically and structurally sound data.</p>
 * 
 * <p><b>ResponseEntity & REST API principles:</b></p>
 * <p>RESTful APIs use standard HTTP verbs (GET, POST, PUT, DELETE) and communicate application state 
 * via standard HTTP status codes. {@code ResponseEntity} is a wrapper that gives developers absolute control 
 * over the HTTP response body, headers, and status code (e.g., 200 OK for success, 400 Bad Request for 
 * validation failure, 404 Not Found if a resource is missing). Returning raw objects instead of ResponseEntity 
 * limits our ability to manipulate these crucial HTTP-level metadata.</p>
 */
@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transaction APIs", description = "Endpoints for handling financial transactions and history")
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * Constructor Injection ensures that this controller cannot be instantiated without its 
     * required TransactionService dependency. It promotes immutability (the field is final) 
     * and makes unit testing cleaner.
     */
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Initiates a money transfer from the authenticated user to a target UPI ID.
     * 
     * @param request The validated payload containing transfer details.
     * @return 200 OK with the completed transaction details.
     */
    @Operation(summary = "Send money to a UPI ID", description = "Initiates an atomic money transfer from the authenticated user to a target UPI ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction completed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error, insufficient balance, or invalid PIN"),
            @ApiResponse(responseCode = "404", description = "Target UPI ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PostMapping("/send")
    public ResponseEntity<TransactionResponse> sendMoney(@Valid @RequestBody SendMoneyRequest request) {
        // The controller delegates entirely to the service. No business logic here.
        TransactionResponse response = transactionService.sendMoney(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves the transaction history for the authenticated user.
     * 
     * @return 200 OK with a list of transaction histories.
     */
    @Operation(summary = "Get transaction history", description = "Retrieves the transaction history for the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @GetMapping("/history")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory() {
        // The controller delegates entirely to the service. No business logic here.
        List<TransactionHistoryResponse> history = transactionService.getTransactionHistory();
        return ResponseEntity.ok(history);
    }

    /**
     * Retrieves an aggregated summary of the authenticated user's transactions.
     * 
     * @return 200 OK with the transaction summary.
     */
    @Operation(summary = "Get transaction summary", description = "Retrieves an aggregated summary of the authenticated user's transactions.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> getTransactionSummary() {
        // The controller delegates entirely to the service. No business logic here.
        TransactionSummaryResponse summary = transactionService.getTransactionSummary();
        return ResponseEntity.ok(summary);
    }

    /**
     * Retrieves a specific transaction by its unique reference number.
     * 
     * @param transactionReference The unique reference ID of the transaction.
     * @return 200 OK with the transaction details.
     */
    @Operation(summary = "Get transaction by reference", description = "Retrieves a specific transaction by its unique reference number.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transaction retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Transaction not found or does not belong to user"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @GetMapping("/{transactionReference}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @PathVariable String transactionReference) {
        // The controller delegates entirely to the service. No business logic here.
        TransactionResponse response = transactionService.getTransactionByReference(transactionReference);
        return ResponseEntity.ok(response);
    }
}
