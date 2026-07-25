package com.evcharging.session.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.shared.kernel.Money;

/** API response representing details of a charging session. */
public record SessionResponse(
    UUID id,
    UUID stationId,
    Integer connectorId,
    UUID customerId,
    UUID vehicleId,
    String status,
    Instant startTime,
    Instant endTime,
    Money unitRate,
    BigDecimal totalEnergyKwh,
    Money totalAmount,
    String errorCode,
    Instant createdAt) {

  public static SessionResponse from(ChargingSession session) {
    return new SessionResponse(
        session.getId().getValue(),
        session.getStationId().getValue(),
        session.getConnectorId(),
        session.getCustomerId().getValue(),
        session.getVehicleId(),
        session.getStatus().name(),
        session.getStartTime(),
        session.getEndTime(),
        session.getUnitRate(),
        session.getTotalEnergyKwh(),
        session.getTotalAmount(),
        session.getErrorCode(),
        session.getCreatedAt());
  }
}
