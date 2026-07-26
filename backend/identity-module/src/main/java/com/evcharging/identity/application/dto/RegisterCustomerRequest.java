package com.evcharging.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request payload for customer self-registration. */
@Schema(description = "Request payload for customer self-registration")
public record RegisterCustomerRequest(
    @Schema(
            description = "Full name of the customer",
            example = "John Smith",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
    @Schema(
            description = "Email address for login & notifications",
            example = "driver@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,
    @Schema(
            description = "Account password (min 6 characters)",
            example = "SecretPass123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Password is required")
        @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
        String password,
    @Schema(
            description = "Contact phone number",
            example = "+12025550143",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Phone number is required")
        String phone) {}
