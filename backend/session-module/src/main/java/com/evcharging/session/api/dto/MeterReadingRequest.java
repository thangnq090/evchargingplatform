package com.evcharging.session.api.dto;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Request payload for appending a meter reading. */
public record MeterReadingRequest(
    @NotNull(message = "Timestamp is required") Instant timestamp,
    @NotNull(message = "Energy delivered is required")
        @PositiveOrZero(message = "Energy must be non-negative")
        BigDecimal energyDeliveredKwh,
    @NotNull(message = "Power rate is required")
        @PositiveOrZero(message = "Power rate must be non-negative")
        BigDecimal powerKw) {}
