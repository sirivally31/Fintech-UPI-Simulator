package com.example.demo.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.LoginResponseDto;
import com.example.demo.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.metrics.BusinessMetricsService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication APIs", description = "Endpoints for user login and JWT token generation")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final BusinessMetricsService businessMetricsService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtService jwtService,
                          BusinessMetricsService businessMetricsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.businessMetricsService = businessMetricsService;
    }

    @Operation(summary = "Login to the simulator", description = "Authenticates a user using UPI ID and PIN. Returns a JWT Bearer token on success.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully authenticated and JWT returned"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials (wrong UPI ID or PIN)"),
            @ApiResponse(responseCode = "400", description = "Validation error in request body")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUpiId(), request.getPin())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(userDetails);

            businessMetricsService.recordAuthenticationSuccess();
            return ResponseEntity.ok(new LoginResponseDto(jwtToken));
        } catch (Exception ex) {
            businessMetricsService.recordAuthenticationFailure(ex.getClass().getSimpleName());
            throw ex;
        }
    }
}
