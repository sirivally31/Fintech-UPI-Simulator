package com.example.demo.controller;

import com.example.demo.entity.Role;
import com.example.demo.service.RoleManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/roles")
@Tag(name = "Role Management", description = "APIs for managing roles and permissions (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class RoleManagementController {

    private final RoleManagementService roleManagementService;

    public RoleManagementController(RoleManagementService roleManagementService) {
        this.roleManagementService = roleManagementService;
    }

    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleManagementService.getAllRoles());
    }

    @PostMapping
    @Operation(summary = "Create role")
    public ResponseEntity<Role> createRole(@RequestParam String roleName, @RequestParam(required = false) List<String> permissions, Authentication auth) {
        return ResponseEntity.ok(roleManagementService.createRole(roleName, permissions, auth.getName()));
    }

    @PostMapping("/assign")
    @Operation(summary = "Assign role to user")
    public ResponseEntity<Void> assignRoleToUser(@RequestParam Long userId, @RequestParam Long roleId, Authentication auth) {
        roleManagementService.assignRoleToUser(userId, roleId, auth.getName());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove")
    @Operation(summary = "Remove role from user")
    public ResponseEntity<Void> removeRoleFromUser(@RequestParam Long userId, @RequestParam Long roleId, Authentication auth) {
        roleManagementService.removeRoleFromUser(userId, roleId, auth.getName());
        return ResponseEntity.ok().build();
    }
}
