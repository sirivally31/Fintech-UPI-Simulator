package com.example.demo.controller;

import com.example.demo.entity.SystemConfig;
import com.example.demo.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@Tag(name = "System Configuration Management", description = "APIs for managing system configurations (Admin only)")
@PreAuthorize("hasRole('ADMIN')")
public class SystemConfigController {

    private final SystemConfigService systemConfigService;

    public SystemConfigController(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    @GetMapping
    @Operation(summary = "Get all configurations")
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        return ResponseEntity.ok(systemConfigService.getAllConfigs());
    }

    @GetMapping("/{configKey}")
    @Operation(summary = "Get configuration by key")
    public ResponseEntity<SystemConfig> getConfigByKey(@PathVariable String configKey) {
        return ResponseEntity.ok(systemConfigService.getConfigByKey(configKey));
    }

    @PostMapping
    @Operation(summary = "Create configuration")
    public ResponseEntity<SystemConfig> createConfig(@RequestParam String configKey, @RequestParam String configValue, @RequestParam(required = false) String description, Authentication auth) {
        return ResponseEntity.ok(systemConfigService.createConfig(configKey, configValue, description, auth.getName()));
    }

    @PutMapping("/{configKey}")
    @Operation(summary = "Update configuration")
    public ResponseEntity<SystemConfig> updateConfig(@PathVariable String configKey, @RequestParam String configValue, @RequestParam(required = false) String description, Authentication auth) {
        return ResponseEntity.ok(systemConfigService.updateConfig(configKey, configValue, description, auth.getName()));
    }

    @DeleteMapping("/{configKey}")
    @Operation(summary = "Delete configuration")
    public ResponseEntity<Void> deleteConfig(@PathVariable String configKey, Authentication auth) {
        systemConfigService.deleteConfig(configKey, auth.getName());
        return ResponseEntity.noContent().build();
    }
}
