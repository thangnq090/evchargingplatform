package com.evcharging.vehicle.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Request body for registering a new vehicle. */
public record RegisterVehicleRequest(
    @NotBlank(message = "Registration plate is required")
        @Size(max = 20, message = "Registration plate must not exceed 20 characters")
        String registrationPlate,
    @Size(max = 50, message = "RFID number must not exceed 50 characters")
        String rfidNumber // optional
    ) {}
