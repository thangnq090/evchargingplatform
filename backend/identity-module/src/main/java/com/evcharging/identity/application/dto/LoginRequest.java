package com.evcharging.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request payload for authentication login. */
@Schema(description = "Request payload for user login authentication")
public record LoginRequest(
    @Schema(
            description = "Registered email address",
            example = "admin@evcharging.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        @Email
        String email,
    @Schema(
            description = "User password",
            example = "SuperSecret123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank
        String password) {}
