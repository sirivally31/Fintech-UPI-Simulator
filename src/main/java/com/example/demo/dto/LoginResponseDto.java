package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response payload containing the JWT Bearer authentication token")
public class LoginResponseDto {

    @Schema(description = "JWT Bearer token to be passed in Authorization header (Bearer <token>)", example = "eyJhbGciOiJIUzI1NiJ9...")
    @JsonProperty("token")
    private String token;

    public LoginResponseDto() {}

    public LoginResponseDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
