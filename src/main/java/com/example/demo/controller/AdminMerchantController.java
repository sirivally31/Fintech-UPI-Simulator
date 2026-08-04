package com.example.demo.controller;

import com.example.demo.entity.Merchant;
import com.example.demo.service.AdminMerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/merchants")
@Tag(name = "Admin Merchant Management", description = "APIs for managing merchants (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMerchantController {

    private final AdminMerchantService adminMerchantService;

    public AdminMerchantController(AdminMerchantService adminMerchantService) {
        this.adminMerchantService = adminMerchantService;
    }

    @GetMapping
    @Operation(summary = "Get all merchants")
    public ResponseEntity<Page<Merchant>> getAllMerchants(Pageable pageable) {
        return ResponseEntity.ok(adminMerchantService.getAllMerchants(pageable));
    }

    @PutMapping("/{merchantId}/approve")
    @Operation(summary = "Approve merchant")
    public ResponseEntity<Merchant> approveMerchant(@PathVariable UUID merchantId, Authentication auth) {
        return ResponseEntity.ok(adminMerchantService.approveMerchant(merchantId, auth.getName()));
    }

    @PutMapping("/{merchantId}/reject")
    @Operation(summary = "Reject merchant")
    public ResponseEntity<Merchant> rejectMerchant(@PathVariable UUID merchantId, @RequestParam String reason, Authentication auth) {
        return ResponseEntity.ok(adminMerchantService.rejectMerchant(merchantId, reason, auth.getName()));
    }

    @PutMapping("/{merchantId}/suspend")
    @Operation(summary = "Suspend merchant")
    public ResponseEntity<Merchant> suspendMerchant(@PathVariable UUID merchantId, @RequestParam String reason, Authentication auth) {
        return ResponseEntity.ok(adminMerchantService.suspendMerchant(merchantId, reason, auth.getName()));
    }

    @PutMapping("/{merchantId}/activate")
    @Operation(summary = "Activate merchant")
    public ResponseEntity<Merchant> activateMerchant(@PathVariable UUID merchantId, Authentication auth) {
        return ResponseEntity.ok(adminMerchantService.activateMerchant(merchantId, auth.getName()));
    }
}
