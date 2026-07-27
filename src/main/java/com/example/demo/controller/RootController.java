package com.example.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class RootController {

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> rootInfo() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "UP");
        response.put("service", "Fintech UPI Payment Simulator API");
        response.put("message", "Welcome! The UPI Backend API is running successfully.");
        response.put("swaggerUI", "/swagger-ui/index.html");
        response.put("actuatorHealth", "/actuator/health");
        return ResponseEntity.ok(response);
    }
}
