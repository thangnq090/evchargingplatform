package com.evcharging.session.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

/** Request payload for starting a new charging session. */
public record StartSessionRequest(
    @NotNull(message = "Station ID is required") UUID stationId,
    @NotNull(message = "Connector ID is required") Integer connectorId,
    @NotNull(message = "Customer ID is required") UUID customerId,
    UUID vehicleId) {}
