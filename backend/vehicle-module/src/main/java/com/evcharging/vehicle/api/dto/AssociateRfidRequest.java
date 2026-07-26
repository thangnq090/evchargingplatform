package com.evcharging.vehicle.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for associating an RFID tag with a vehicle. */
public record AssociateRfidRequest(
    @NotBlank(message = "RFID number is required")
        @Size(max = 50, message = "RFID number must not exceed 50 characters")
        String rfidNumber) {}
