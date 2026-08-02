package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.entity.Merchant;
import com.example.demo.entity.Transaction;
import com.example.demo.entity.User;
import com.example.demo.repository.MerchantRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.TransactionRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AdminDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin & Operations Dashboard APIs", description = "Internal management endpoints for operations, transaction inspection, user/merchant control, and system metrics aggregation")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;
    private final UserRepository userRepository;
    private final MerchantRepository merchantRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationRepository notificationRepository;

    public AdminDashboardController(AdminDashboardService adminDashboardService,
                                    UserRepository userRepository,
                                    MerchantRepository merchantRepository,
                                    TransactionRepository transactionRepository,
                                    NotificationRepository notificationRepository) {
        this.adminDashboardService = adminDashboardService;
        this.userRepository = userRepository;
        this.merchantRepository = merchantRepository;
        this.transactionRepository = transactionRepository;
        this.notificationRepository = notificationRepository;
    }

    @Operation(summary = "Get Dashboard Summary", description = "Retrieves aggregated system statistics, operational volumes, and component health status.")
    @ApiResponse(responseCode = "200", description = "Summary retrieved successfully",
            content = @Content(schema = @Schema(implementation = DashboardSummaryResponse.class)))
    @GetMapping("/dashboard/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary() {
        DashboardSummaryResponse summary = adminDashboardService.getDashboardSummary();
        return ResponseEntity.ok(summary);
    }

    @Operation(summary = "Get System Health", description = "Retrieves infrastructure component health status (Database, Redis, Kafka).")
    @ApiResponse(responseCode = "200", description = "Health status retrieved successfully",
            content = @Content(schema = @Schema(implementation = SystemHealthResponse.class)))
    @GetMapping("/dashboard/health")
    public ResponseEntity<SystemHealthResponse> getSystemHealth() {
        SystemHealthResponse health = adminDashboardService.getSystemHealth();
        return ResponseEntity.ok(health);
    }

    @Operation(summary = "Get Transaction Analytics", description = "Retrieves transaction volume, success rates, daily/weekly metrics, and average amounts.")
    @ApiResponse(responseCode = "200", description = "Analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = TransactionAnalyticsResponse.class)))
    @GetMapping("/analytics/transactions")
    public ResponseEntity<TransactionAnalyticsResponse> getTransactionAnalytics() {
        TransactionAnalyticsResponse analytics = adminDashboardService.getTransactionAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @Operation(summary = "Get Merchant Analytics", description = "Retrieves merchant active/pending counts and top active merchant name.")
    @ApiResponse(responseCode = "200", description = "Merchant analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = MerchantAnalyticsResponse.class)))
    @GetMapping("/analytics/merchants")
    public ResponseEntity<MerchantAnalyticsResponse> getMerchantAnalytics() {
        MerchantAnalyticsResponse analytics = adminDashboardService.getMerchantAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @Operation(summary = "Get Fraud Analytics", description = "Retrieves risk engine decision evaluation metrics.")
    @ApiResponse(responseCode = "200", description = "Fraud analytics retrieved successfully",
            content = @Content(schema = @Schema(implementation = FraudAnalyticsResponse.class)))
    @GetMapping("/analytics/fraud")
    public ResponseEntity<FraudAnalyticsResponse> getFraudAnalytics() {
        FraudAnalyticsResponse analytics = adminDashboardService.getFraudAnalytics();
        return ResponseEntity.ok(analytics);
    }

    @Operation(summary = "Get All System Transactions", description = "Retrieves complete transaction log history for operations audit.")
    @ApiResponse(responseCode = "200", description = "Transactions retrieved successfully")
    @GetMapping("/transactions")
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        List<TransactionResponse> txns = transactionRepository.findAll().stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(txns);
    }

    @Operation(summary = "Search Transactions", description = "Searches transactions with multi-field filtering.")
    @ApiResponse(responseCode = "200", description = "Matching transactions retrieved successfully")
    @GetMapping("/transactions/search")
    public ResponseEntity<List<TransactionResponse>> searchTransactions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) String upiId,
            @RequestParam(required = false) String reference) {
        List<TransactionResponse> txns = adminDashboardService.searchTransactions(status, minAmount, upiId, reference);
        return ResponseEntity.ok(txns);
    }

    @Operation(summary = "Get All Users", description = "Retrieves all registered users.")
    @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        List<UserDto> users = userRepository.findAll().stream()
                .map(this::mapToUserDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Update User Status", description = "Admin action to disable, enable, lock, or unlock a user profile.")
    @ApiResponse(responseCode = "200", description = "User status updated successfully")
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<UserDto> updateUserStatus(
            @Parameter(description = "User ID", required = true) @PathVariable Long id,
            @RequestParam String action) {
        UserDto updated = adminDashboardService.updateUserStatus(id, action);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Get All Merchants", description = "Retrieves all registered merchant profiles.")
    @ApiResponse(responseCode = "200", description = "Merchants retrieved successfully")
    @GetMapping("/merchants")
    public ResponseEntity<List<MerchantResponse>> getAllMerchants() {
        List<MerchantResponse> merchants = merchantRepository.findAll().stream()
                .map(this::mapToMerchantResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(merchants);
    }

    @Operation(summary = "Update Merchant Status", description = "Admin action to approve, reject, suspend, or reactivate a merchant.")
    @ApiResponse(responseCode = "200", description = "Merchant status updated successfully")
    @PatchMapping("/merchants/{id}/status")
    public ResponseEntity<MerchantResponse> updateMerchantStatus(
            @Parameter(description = "Merchant ID", required = true) @PathVariable Long id,
            @RequestParam String action) {
        MerchantResponse updated = adminDashboardService.updateMerchantStatus(id, action);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Resend Failed Notification", description = "Resends a failed notification to the destination user channel.")
    @ApiResponse(responseCode = "204", description = "Notification resent successfully")
    @PostMapping("/notifications/{id}/resend")
    public ResponseEntity<Void> resendNotification(
            @Parameter(description = "Notification UUID", required = true) @PathVariable UUID id) {
        adminDashboardService.resendNotification(id);
        return ResponseEntity.noContent().build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction t) {
        TransactionResponse dto = new TransactionResponse();
        dto.setTransactionReference(t.getTransactionReference());
        dto.setSenderUpiId(t.getSenderUpiId() != null ? t.getSenderUpiId().getUpiId() : null);
        dto.setReceiverUpiId(t.getReceiverUpiId() != null ? t.getReceiverUpiId().getUpiId() : null);
        dto.setAmount(t.getAmount());
        dto.setRemarks(t.getRemarks());
        dto.setStatus(t.getStatus());
        dto.setCreatedAt(t.getCreatedAt());
        return dto;
    }

    private UserDto mapToUserDto(User u) {
        return new UserDto(
                u.getId(),
                u.getName(),
                u.getPhoneNumber(),
                u.getUpiId(),
                u.getBalance()
        );
    }

    private MerchantResponse mapToMerchantResponse(Merchant m) {
        if (m == null) return null;
        return new MerchantResponse(
                m.getId(),
                m.getMerchantName(),
                m.getBusinessName(),
                m.getMerchantCode(),
                m.getUpiId(),
                m.getCategory(),
                m.getActive(),
                m.getCreatedAt()
        );
    }
}
