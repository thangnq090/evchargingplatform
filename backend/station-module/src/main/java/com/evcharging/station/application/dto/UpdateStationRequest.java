package com.evcharging.station.application.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import com.evcharging.shared.kernel.Location;

/** Request to update station details. All fields optional (partial update). */
public record UpdateStationRequest(
    @Size(max = 100) String name,
    @Size(max = 50) String groupLabel,
    @Min(0) Integer unitPriceTenthCents,
    Location location) {}
