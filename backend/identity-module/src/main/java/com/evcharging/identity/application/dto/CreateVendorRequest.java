package com.evcharging.identity.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Request DTO for creating a new vendor and issuing an initial VENDOR_ADMIN invitation. */
public record CreateVendorRequest(
    @NotBlank(message = "Vendor name is required") String vendorName,
    @NotBlank(message = "Admin name is required") String adminName,
    @NotBlank(message = "Admin email is required") @Email(message = "Invalid email format")
        String adminEmail) {}
