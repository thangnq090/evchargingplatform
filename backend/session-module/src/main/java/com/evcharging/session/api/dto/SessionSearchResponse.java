package com.evcharging.session.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.evcharging.shared.kernel.Money;

/** API response representing details of a search result for a charging session. */
public record SessionSearchResponse(
    UUID id,
    UUID stationId,
    Integer connectorId,
    UUID customerId,
    String customerAccountNumber,
    UUID vehicleId,
    String registrationPlate,
    String status,
    Instant startTime,
    Instant endTime,
    Money unitRate,
    BigDecimal totalEnergyKwh,
    Money totalAmount,
    String errorCode,
    Instant createdAt) {}
