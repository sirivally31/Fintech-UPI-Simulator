package com.example.demo.controller;

import com.example.demo.dto.ChangeUpiPinRequest;
import com.example.demo.dto.SetUpiPinRequest;
import com.example.demo.dto.VerifyUpiPinRequest;
import com.example.demo.service.UpiPinService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller for handling UPI PIN related HTTP requests.
 * 
 * <h3>Architecture and Design Principles:</h3>
 * 
 * <p><b>Separation of Concerns & Why Controllers should remain thin:</b></p>
 * <p>In a well-structured application, each layer has a distinct responsibility. 
 * The Controller's ONLY jobs are to receive HTTP requests, trigger input validation, 
 * delegate the actual processing to the Service layer, and return the appropriate HTTP response. 
 * By keeping business logic out of the controller ("thin controllers"), the code becomes 
 * much easier to test, read, and maintain. Business rules (like hashing or validation of old PINs) 
 * belong exclusively in the Service layer.</p>
 * 
 * <p><b>Request validation flow:</b></p>
 * <p>The {@code @Valid} annotation triggers Jakarta Bean Validation before the method body even executes. 
 * If a request DTO (like {@code SetUpiPinRequest}) fails validation (e.g., PIN is not 4 digits), 
 * Spring intercepts this, aborts the request, and automatically throws a {@code MethodArgumentNotValidException}.
 * This exception is then caught by a {@code @ControllerAdvice} (GlobalExceptionHandler) to return a structured 
 * 400 Bad Request response. This guarantees that our Service layer only ever processes mathematically and structurally sound data.</p>
 * 
 * <p><b>ResponseEntity & REST API principles:</b></p>
 * <p>RESTful APIs must communicate state via standard HTTP status codes.
 * {@code ResponseEntity} gives us complete control over the HTTP response, including headers, body, and status code.
 * - 200 OK: Request succeeded.
 * - 400 Bad Request: Malformed request or validation failure.
 * - 401 Unauthorized: User is not authenticated.
 * - 404 Not Found: The requested resource (e.g., Bank Account) does not exist.
 * The GlobalExceptionHandler works in tandem with this controller to map domain exceptions to these precise HTTP status codes.</p>
 */
@RestController
@RequestMapping("/api/upi-pin")
@Tag(name = "UPI PIN APIs", description = "Endpoints for managing and verifying UPI PINs")
public class UpiPinController {

    private final UpiPinService upiPinService;

    /**
     * Constructor Injection ensures that the controller cannot be instantiated without its required dependencies.
     */
    public UpiPinController(UpiPinService upiPinService) {
        this.upiPinService = upiPinService;
    }

    /**
     * Endpoint to set a new UPI PIN.
     * 
     * @param request The validated request containing bank account ID, new PIN, and confirmation PIN.
     * @return 200 OK on success.
     */
    @Operation(summary = "Set UPI PIN", description = "Sets a new UPI PIN for a bank account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UPI PIN set successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or PINs do not match"),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PostMapping("/set")
    public ResponseEntity<String> setUpiPin(@Valid @RequestBody SetUpiPinRequest request) {
        // The controller delegates entirely to the service. No business logic here.
        upiPinService.setUpiPin(request);
        
        // Return 200 OK using ResponseEntity
        return ResponseEntity.ok("UPI PIN set successfully");
    }

    /**
     * Endpoint to change an existing UPI PIN.
     * 
     * @param request The validated request containing bank account ID, old PIN, new PIN, and confirmation PIN.
     * @return 200 OK on success.
     */
    @Operation(summary = "Change UPI PIN", description = "Changes an existing UPI PIN after verifying the old PIN.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UPI PIN changed successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error, invalid old PIN, or PINs do not match"),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PutMapping("/change")
    public ResponseEntity<String> changeUpiPin(@Valid @RequestBody ChangeUpiPinRequest request) {
        // The controller delegates entirely to the service. No business logic here.
        upiPinService.changeUpiPin(request);
        
        // Return 200 OK using ResponseEntity
        return ResponseEntity.ok("UPI PIN changed successfully");
    }

    /**
     * Endpoint to verify a UPI PIN.
     * 
     * @param request The validated request containing bank account ID and PIN to verify.
     * @return 200 OK with a boolean indicating if the PIN is correct.
     */
    @Operation(summary = "Verify UPI PIN", description = "Verifies if the provided UPI PIN is correct for the bank account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UPI PIN verification successful (returns true/false)"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Bank account not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PostMapping("/verify")
    public ResponseEntity<Boolean> verifyUpiPin(@Valid @RequestBody VerifyUpiPinRequest request) {
        // The controller delegates entirely to the service. No business logic here.
        boolean isValid = upiPinService.verifyUpiPin(request);
        
        // Return 200 OK using ResponseEntity
        return ResponseEntity.ok(isValid);
    }
}
