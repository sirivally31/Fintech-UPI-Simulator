package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.BeneficiaryService;
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
@RequestMapping("/api/beneficiaries")
@Tag(name = "Beneficiary Management APIs", description = "Endpoints for managing saved payees/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService beneficiaryService;

    public BeneficiaryController(BeneficiaryService beneficiaryService) {
        this.beneficiaryService = beneficiaryService;
    }

    @Operation(summary = "Add a new Beneficiary", description = "Saves a new beneficiary to the authenticated user's profile.")
    @ApiResponse(responseCode = "201", description = "Beneficiary added successfully",
            content = @Content(schema = @Schema(implementation = BeneficiaryResponse.class)))
    @ApiResponse(responseCode = "400", description = "Validation error or self-addition attempted")
    @ApiResponse(responseCode = "409", description = "Beneficiary already exists")
    @PostMapping
    public ResponseEntity<BeneficiaryResponse> addBeneficiary(@Valid @RequestBody AddBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.addBeneficiary(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update Beneficiary", description = "Updates details such as name, nickname, or favourite status for an existing beneficiary.")
    @ApiResponse(responseCode = "200", description = "Beneficiary updated successfully",
            content = @Content(schema = @Schema(implementation = BeneficiaryResponse.class)))
    @ApiResponse(responseCode = "404", description = "Beneficiary not found")
    @PutMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> updateBeneficiary(
            @Parameter(description = "Beneficiary UUID", required = true) @PathVariable UUID id,
            @Valid @RequestBody UpdateBeneficiaryRequest request) {
        BeneficiaryResponse response = beneficiaryService.updateBeneficiary(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Delete Beneficiary", description = "Removes a beneficiary from the authenticated user's list.")
    @ApiResponse(responseCode = "204", description = "Beneficiary deleted successfully")
    @ApiResponse(responseCode = "404", description = "Beneficiary not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBeneficiary(
            @Parameter(description = "Beneficiary UUID", required = true) @PathVariable UUID id) {
        beneficiaryService.deleteBeneficiary(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get All Beneficiaries", description = "Retrieves all saved beneficiaries for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "List of beneficiaries retrieved successfully",
            content = @Content(schema = @Schema(implementation = BeneficiaryListResponse.class)))
    @GetMapping
    public ResponseEntity<BeneficiaryListResponse> getAllBeneficiaries() {
        BeneficiaryListResponse response = beneficiaryService.getAllBeneficiaries();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get Beneficiary by ID", description = "Fetches a specific beneficiary profile by UUID.")
    @ApiResponse(responseCode = "200", description = "Beneficiary retrieved successfully",
            content = @Content(schema = @Schema(implementation = BeneficiaryResponse.class)))
    @ApiResponse(responseCode = "404", description = "Beneficiary not found")
    @GetMapping("/{id}")
    public ResponseEntity<BeneficiaryResponse> getBeneficiary(
            @Parameter(description = "Beneficiary UUID", required = true) @PathVariable UUID id) {
        BeneficiaryResponse response = beneficiaryService.getBeneficiary(id);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Search Beneficiaries", description = "Searches beneficiaries by name, nickname, or UPI ID.")
    @ApiResponse(responseCode = "200", description = "Matching beneficiaries retrieved successfully",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = BeneficiaryResponse.class))))
    @GetMapping("/search")
    public ResponseEntity<List<BeneficiaryResponse>> searchBeneficiaries(
            @Parameter(description = "Search query keyword", required = false) @RequestParam(name = "query", required = false) String query) {
        List<BeneficiaryResponse> results = beneficiaryService.searchBeneficiaries(query);
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Toggle Favourite Status", description = "Marks or unmarks a beneficiary as a favourite.")
    @ApiResponse(responseCode = "200", description = "Favourite status updated successfully",
            content = @Content(schema = @Schema(implementation = BeneficiaryResponse.class)))
    @ApiResponse(responseCode = "404", description = "Beneficiary not found")
    @PatchMapping("/{id}/favorite")
    public ResponseEntity<BeneficiaryResponse> markFavourite(
            @Parameter(description = "Beneficiary UUID", required = true) @PathVariable UUID id,
            @Parameter(description = "Favourite flag", required = true) @RequestParam(name = "favourite", defaultValue = "true") boolean favourite) {
        BeneficiaryResponse response = beneficiaryService.markFavourite(id, favourite);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Verify Beneficiary UPI ID", description = "Verifies whether beneficiary UPI ID exists and is active in the UPI ecosystem.")
    @ApiResponse(responseCode = "200", description = "Verification process completed",
            content = @Content(schema = @Schema(implementation = BeneficiaryResponse.class)))
    @ApiResponse(responseCode = "404", description = "Beneficiary not found")
    @PatchMapping("/{id}/verify")
    public ResponseEntity<BeneficiaryResponse> verifyBeneficiary(
            @Parameter(description = "Beneficiary UUID", required = true) @PathVariable UUID id) {
        BeneficiaryResponse response = beneficiaryService.verifyBeneficiary(id);
        return ResponseEntity.ok(response);
    }
}
