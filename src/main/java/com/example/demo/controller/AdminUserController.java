package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.AdminUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "Admin User Management", description = "APIs for managing users (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    @Operation(summary = "Get all users")
    public ResponseEntity<Page<User>> getAllUsers(Pageable pageable) {
        return ResponseEntity.ok(adminUserService.getAllUsers(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users by name")
    public ResponseEntity<Page<User>> searchUsers(@RequestParam String query, Pageable pageable) {
        return ResponseEntity.ok(adminUserService.searchUsers(query, pageable));
    }

    @PutMapping("/{userId}/enable")
    @Operation(summary = "Enable user")
    public ResponseEntity<User> enableUser(@PathVariable Long userId, Authentication auth) {
        return ResponseEntity.ok(adminUserService.enableUser(userId, auth.getName()));
    }

    @PutMapping("/{userId}/disable")
    @Operation(summary = "Disable user")
    public ResponseEntity<User> disableUser(@PathVariable Long userId, Authentication auth) {
        return ResponseEntity.ok(adminUserService.disableUser(userId, auth.getName()));
    }

    @PutMapping("/{userId}/lock")
    @Operation(summary = "Lock user")
    public ResponseEntity<User> lockUser(@PathVariable Long userId, Authentication auth) {
        return ResponseEntity.ok(adminUserService.lockUser(userId, auth.getName()));
    }

    @PutMapping("/{userId}/unlock")
    @Operation(summary = "Unlock user")
    public ResponseEntity<User> unlockUser(@PathVariable Long userId, Authentication auth) {
        return ResponseEntity.ok(adminUserService.unlockUser(userId, auth.getName()));
    }

    @PutMapping("/{userId}/reset-pin")
    @Operation(summary = "Reset user UPI PIN")
    public ResponseEntity<User> resetUserPin(@PathVariable Long userId, @RequestParam String newPin, Authentication auth) {
        return ResponseEntity.ok(adminUserService.resetUserPin(userId, newPin, auth.getName()));
    }

    @GetMapping("/{userId}/profile")
    @Operation(summary = "View user profile")
    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUserProfile(userId));
    }
}
