package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.PaymentRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment-requests")
@Tag(name = "Payment Request APIs", description = "Endpoints for Collect Requests (Google Pay / PhonePe style)")
public class PaymentRequestController {

    private final PaymentRequestService paymentRequestService;

    public PaymentRequestController(PaymentRequestService paymentRequestService) {
        this.paymentRequestService = paymentRequestService;
    }

    @Operation(summary = "Create a new Payment Request", description = "Initiates a collect request from a specific UPI ID.")
    @ApiResponse(responseCode = "201", description = "Payment request created successfully")
    @ApiResponse(responseCode = "400", description = "Validation error in request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized request")
    @PostMapping("/")
    public ResponseEntity<PaymentRequestResponse> createRequest(@Valid @RequestBody CreatePaymentRequestRequest request) {
        PaymentRequestResponse response = paymentRequestService.createRequest(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get Sent Requests", description = "Retrieves all payment requests initiated by the current user.")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized request")
    @GetMapping("/sent")
    public ResponseEntity<List<PaymentRequestResponse>> getSentRequests() {
        List<PaymentRequestResponse> responses = paymentRequestService.getMySentRequests();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get Received Requests", description = "Retrieves all payment requests received by the current user.")
    @ApiResponse(responseCode = "200", description = "List retrieved successfully")
    @ApiResponse(responseCode = "401", description = "Unauthorized request")
    @GetMapping("/received")
    public ResponseEntity<List<PaymentRequestResponse>> getReceivedRequests() {
        List<PaymentRequestResponse> responses = paymentRequestService.getMyReceivedRequests();
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Get Request By Reference", description = "Fetches a specific payment request by its unique reference.")
    @ApiResponse(responseCode = "200", description = "Request retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Request not found or not owned by user")
    @GetMapping("/{reference}")
    public ResponseEntity<PaymentRequestResponse> getRequestByReference(
            @Parameter(description = "Unique request reference string", required = true) @PathVariable String reference) {
        PaymentRequestResponse response = paymentRequestService.getRequestByReference(reference);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Accept Payment Request", description = "Approves a pending payment request and transfers the funds.")
    @ApiResponse(responseCode = "200", description = "Request accepted and money transferred")
    @ApiResponse(responseCode = "400", description = "Validation error, incorrect PIN, or insufficient balance")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PutMapping("/{reference}/accept")
    public ResponseEntity<PaymentRequestResponse> acceptRequest(
            @Parameter(description = "Unique request reference string", required = true) @PathVariable String reference,
            @Valid @RequestBody AcceptPaymentRequestRequest request) {
        PaymentRequestResponse existingReq = paymentRequestService.getRequestByReference(reference);
        PaymentRequestResponse response = paymentRequestService.acceptRequest(existingReq.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Reject Payment Request", description = "Declines a pending payment request.")
    @ApiResponse(responseCode = "200", description = "Request rejected successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PutMapping("/{reference}/reject")
    public ResponseEntity<PaymentRequestResponse> rejectRequest(
            @Parameter(description = "Unique request reference string", required = true) @PathVariable String reference,
            @Valid @RequestBody RejectPaymentRequestRequest request) {
        PaymentRequestResponse existingReq = paymentRequestService.getRequestByReference(reference);
        PaymentRequestResponse response = paymentRequestService.rejectRequest(existingReq.getId(), request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Cancel Payment Request", description = "Cancels a pending payment request initiated by you.")
    @ApiResponse(responseCode = "200", description = "Request cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Validation error")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PutMapping("/{reference}/cancel")
    public ResponseEntity<PaymentRequestResponse> cancelRequest(
            @Parameter(description = "Unique request reference string", required = true) @PathVariable String reference,
            @Valid @RequestBody CancelPaymentRequestRequest request) {
        PaymentRequestResponse existingReq = paymentRequestService.getRequestByReference(reference);
        PaymentRequestResponse response = paymentRequestService.cancelRequest(existingReq.getId(), request);
        return ResponseEntity.ok(response);
    }
}
