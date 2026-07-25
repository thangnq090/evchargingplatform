package com.evcharging.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.evcharging.identity.domain.model.Role;

/** Request DTO for a VENDOR_ADMIN to add a new VENDOR_USER to their organisation. */
public record AddVendorUserRequest(
    @NotBlank(message = "Name is required") String name,
    @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
    @NotNull(message = "Role is required") Role role) {}
