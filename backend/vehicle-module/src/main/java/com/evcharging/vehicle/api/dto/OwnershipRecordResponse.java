package com.evcharging.vehicle.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.evcharging.vehicle.domain.model.OwnershipRecord;

/** API response representing a vehicle ownership history entry. */
public record OwnershipRecordResponse(
    UUID id,
    UUID vehicleId,
    UUID customerId,
    Instant startDate,
    Instant endDate // nullable — null means current owner
    ) {

  public static OwnershipRecordResponse from(OwnershipRecord record) {
    return new OwnershipRecordResponse(
        record.getId(),
        record.getVehicleId(),
        record.getCustomerId(),
        record.getStartDate(),
        record.getEndDate());
  }
}
