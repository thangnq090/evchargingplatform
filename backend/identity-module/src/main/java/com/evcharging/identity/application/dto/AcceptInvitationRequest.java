package com.evcharging.identity.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request DTO for accepting a vendor invitation and completing registration. */
public record AcceptInvitationRequest(
    @NotBlank(message = "Invitation token is required") String token,
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password) {}
