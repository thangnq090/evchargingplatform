package com.evcharging.session.api.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

import io.swagger.v3.oas.annotations.media.Schema;

/** Request payload for starting a new charging session. */
@Schema(description = "Request payload to initiate an EV charging session")
public record StartSessionRequest(
    @Schema(
            description = "UUID of the target charging station",
            example = "a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Station ID is required")
        UUID stationId,
    @Schema(
            description = "Target connector ID on the station (e.g. 1 or 2)",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Connector ID is required")
        Integer connectorId,
    @Schema(
            description = "UUID of the customer driver",
            example = "b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e",
            requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Customer ID is required")
        UUID customerId,
    @Schema(
            description = "Optional UUID of the EV vehicle being charged",
            example = "c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f")
        UUID vehicleId) {}
