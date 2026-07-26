package com.example.demo.controller;

import com.example.demo.dto.MerchantRegisterRequest;
import com.example.demo.dto.MerchantResponse;
import com.example.demo.dto.MerchantUpdateRequest;
import com.example.demo.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/merchant")
@Tag(name = "Merchant Management APIs", description = "Endpoints for registering, viewing, updating, and deactivating merchants")
public class MerchantController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @Operation(summary = "Register a new Merchant", description = "Creates a new merchant account with unique merchantCode and upiId.")
    @ApiResponse(responseCode = "201", description = "Merchant registered successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error in request body")
    @ApiResponse(responseCode = "409", description = "Merchant code or UPI ID already exists")
    @PostMapping("/register")
    public ResponseEntity<MerchantResponse> registerMerchant(@Valid @RequestBody MerchantRegisterRequest request) {
        MerchantResponse response = merchantService.registerMerchant(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Merchant by ID", description = "Retrieves merchant details using the unique merchant UUID.")
    @ApiResponse(responseCode = "200", description = "Merchant retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class)))
    @ApiResponse(responseCode = "404", description = "Merchant not found")
    @GetMapping("/{id}")
    public ResponseEntity<MerchantResponse> getMerchantById(
            @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID id) {
        MerchantResponse response = merchantService.getMerchantById(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update Merchant", description = "Updates business details or status of an existing merchant.")
    @ApiResponse(responseCode = "200", description = "Merchant updated successfully",
            content = @Content(schema = @Schema(implementation = MerchantResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Merchant not found")
    @PutMapping("/{id}")
    public ResponseEntity<MerchantResponse> updateMerchant(
            @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody MerchantUpdateRequest request) {
        MerchantResponse response = merchantService.updateMerchant(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Deactivate Merchant", description = "Soft deletes / deactivates a merchant account.")
    @ApiResponse(responseCode = "204", description = "Merchant deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Merchant not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMerchant(
            @Parameter(description = "Merchant UUID", required = true) @PathVariable UUID id) {
        merchantService.deleteMerchant(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get All Merchants", description = "Retrieves a list of all registered merchants.")
    @ApiResponse(responseCode = "200", description = "List of merchants retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = MerchantResponse.class))))
    @GetMapping("/all")
    public ResponseEntity<List<MerchantResponse>> getAllMerchants() {
        List<MerchantResponse> responseList = merchantService.getAllMerchants();
        return ResponseEntity.ok(responseList);
    }
}
