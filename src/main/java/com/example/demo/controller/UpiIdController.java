package com.example.demo.controller;

import com.example.demo.dto.CreateUpiIdRequest;
import com.example.demo.dto.UpdateUpiIdRequest;
import com.example.demo.dto.UpiIdResponse;
import com.example.demo.service.UpiIdService;
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
 * REST Controller for managing UPI IDs.
 * 
 * Separation of Concerns & Why controllers should remain thin:
 * Controllers are part of the presentation layer. Their sole responsibility should be parsing HTTP 
 * requests (JSON to Java objects), routing them to the appropriate Service layer, and formatting the 
 * HTTP response. By keeping business logic out of the controller (a "thin" controller) and delegating 
 * it to the Service layer, we make the code highly reusable, easier to test, and strictly adherent 
 * to the Single Responsibility Principle.
 * 
 * REST Principles:
 * This controller follows Representational State Transfer (REST) principles. It uses standard HTTP 
 * methods (GET for reading, POST for creating, PUT for updating, DELETE for removing) mapped to 
 * predictable URIs (/api/upi).
 * 
 * Validation Flow:
 * We use the @Valid annotation on request bodies (e.g., CreateUpiIdRequest). When a request hits 
 * the endpoint, Spring intercepts it and validates the payload against the Jakarta Bean Validation 
 * annotations defined in the DTO (like @NotBlank, @NotNull). If validation fails, Spring automatically 
 * throws a MethodArgumentNotValidException (usually resulting in a 400 Bad Request) before the controller 
 * method even executes, ensuring invalid data never enters the system.
 */
@RestController
@RequestMapping("/api/upi")
@Tag(name = "UPI APIs", description = "Endpoints for managing UPI IDs")
public class UpiIdController {

    private final UpiIdService upiIdService;

    /**
     * Constructor Injection ensures the controller cannot be instantiated without its required service.
     */
    public UpiIdController(UpiIdService upiIdService) {
        this.upiIdService = upiIdService;
    }

    /**
     * HTTP Status Codes & ResponseEntity:
     * We use ResponseEntity to give us complete control over the HTTP response, including headers 
     * and status codes. For a successful creation, we return 201 (CREATED) rather than 200 (OK), 
     * which is the semantic REST standard for indicating a new resource was successfully generated.
     */
    @Operation(summary = "Create a new UPI ID", description = "Creates a new UPI ID linked to a specific bank account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "UPI ID created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PostMapping
    public ResponseEntity<UpiIdResponse> createUpiId(@Valid @RequestBody CreateUpiIdRequest request) {
        UpiIdResponse response = upiIdService.createUpiId(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED); // 201 Created
    }

    /**
     * 200 (OK) is used for successful GET requests.
     */
    @Operation(summary = "Get all UPI IDs", description = "Retrieves all UPI IDs linked to the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of UPI IDs retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @GetMapping
    public ResponseEntity<List<UpiIdResponse>> getAllUpiIds() {
        List<UpiIdResponse> responses = upiIdService.getAllUpiIds();
        return ResponseEntity.ok(responses); // 200 OK
    }

    /**
     * Note: If the ID is not found, the Service layer throws a UpiIdNotFoundException.
     * The GlobalExceptionHandler catches this and automatically returns a 404 (Not Found).
     */
    @Operation(summary = "Get a UPI ID by ID", description = "Retrieves details of a specific UPI ID if it belongs to the authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UPI ID retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "UPI ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UpiIdResponse> getUpiId(@PathVariable Long id) {
        UpiIdResponse response = upiIdService.getUpiId(id);
        return ResponseEntity.ok(response); // 200 OK
    }

    /**
     * PUT is idempotent and used for updating an existing resource.
     */
    @Operation(summary = "Update a UPI ID", description = "Updates specific fields of an existing UPI ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UPI ID updated successfully"),
            @ApiResponse(responseCode = "404", description = "UPI ID not found"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PutMapping("/{id}")
    public ResponseEntity<UpiIdResponse> updateUpiId(@PathVariable Long id, @Valid @RequestBody UpdateUpiIdRequest request) {
        UpiIdResponse response = upiIdService.updateUpiId(id, request);
        return ResponseEntity.ok(response); // 200 OK
    }

    /**
     * 204 (No Content) is the standard REST response for a successful DELETE operation.
     * It tells the client the action succeeded, but there is no payload to return.
     */
    @Operation(summary = "Delete a UPI ID", description = "Removes a specific UPI ID linked to the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "UPI ID deleted successfully"),
            @ApiResponse(responseCode = "404", description = "UPI ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUpiId(@PathVariable Long id) {
        upiIdService.deleteUpiId(id);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    /**
     * Custom endpoint for setting a specific UPI ID as primary.
     */
    @Operation(summary = "Set primary UPI ID", description = "Sets a specific UPI ID as the primary one for the user's account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "UPI ID set as primary successfully"),
            @ApiResponse(responseCode = "404", description = "UPI ID not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT")
    })
    @PutMapping("/{id}/primary")
    public ResponseEntity<UpiIdResponse> setPrimaryUpiId(@PathVariable Long id) {
        UpiIdResponse response = upiIdService.setPrimaryUpiId(id);
        return ResponseEntity.ok(response); // 200 OK
    }
}
