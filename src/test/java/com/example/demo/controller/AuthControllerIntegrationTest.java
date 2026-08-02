package com.example.demo.controller;

import com.example.demo.config.AbstractIntegrationTest;
import com.example.demo.dto.LoginRequestDto;
import com.example.demo.dto.UserCreateDto;
import com.example.demo.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Enterprise Integration Test for AuthController & UserController registration endpoints.
 * Runs against real PostgreSQL container via AbstractIntegrationTest.
 */
@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a new user successfully and persist in PostgreSQL")
    void shouldRegisterUserSuccessfully() throws Exception {
        UserCreateDto request = new UserCreateDto(
                "John Doe",
                "9876543210",
                "john@upi",
                "1234",
                new BigDecimal("1000.00")
        );

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.upiId").value("john@upi"))
                .andExpect(jsonPath("$.phoneNumber").value("9876543210"));

        assertTrue(userRepository.existsByUpiId("john@upi"));
    }

    @Test
    @DisplayName("Should register a new user successfully using snake_case JSON payload")
    void shouldRegisterUserSuccessfullyWithSnakeCaseJson() throws Exception {
        String jsonPayload = """
                {
                  "name": "Jane Snake",
                  "phone_number": "9988776655",
                  "upi_id": "jane@upi",
                  "pin": "1234",
                  "initial_balance": 2000.00
                }
                """;

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.upiId").value("jane@upi"))
                .andExpect(jsonPath("$.phoneNumber").value("9988776655"));

        assertTrue(userRepository.existsByUpiId("jane@upi"));
    }

    @Test
    @DisplayName("Should return HTTP 200 OK for GET /v3/api-docs")
    void shouldReturnOkForSwaggerApiDocs() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi", notNullValue()));
    }

    @Test
    @DisplayName("Should login successfully with valid credentials and return JWT token")
    void shouldLoginSuccessfullyAndReturnJwt() throws Exception {
        // Register user first
        UserCreateDto registerReq = new UserCreateDto(
                "Alice Smith",
                "9123456789",
                "alice@upi",
                "4321",
                new BigDecimal("500.00")
        );

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated());

        // Login attempt
        LoginRequestDto loginReq = new LoginRequestDto();
        loginReq.setUpiId("alice@upi");
        loginReq.setPin("4321");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("Should return 401 Unauthorized for invalid login credentials")
    void shouldReturnUnauthorizedWhenLoginWithInvalidCredentials() throws Exception {
        LoginRequestDto loginReq = new LoginRequestDto();
        loginReq.setUpiId("nonexistent@upi");
        loginReq.setPin("9999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
}
