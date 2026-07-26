package com.evcharging.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request DTO for registering a new platform administrator. */
@Schema(description = "Request payload for registering a new platform administrator")
public record RegisterAdminRequest(
    @Schema(
            description = "Full name of the admin user",
            example = "Jane Doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required")
        String name,
    @Schema(
            description = "Email address of the admin user",
            example = "admin@evcharging.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
    @Schema(
            description = "Password (minimum 8 characters)",
            example = "SuperSecret123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password) {}
