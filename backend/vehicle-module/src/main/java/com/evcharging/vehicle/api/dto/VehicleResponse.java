package com.evcharging.vehicle.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.evcharging.vehicle.domain.model.Vehicle;

/** API response representing a vehicle. */
public record VehicleResponse(
    UUID id,
    String registrationPlate,
    String rfidNumber, // nullable
    String status,
    UUID ownerId,
    Instant createdAt,
    Instant delistedAt // nullable
    ) {

  /** Maps a domain Vehicle to its API representation. */
  public static VehicleResponse from(Vehicle vehicle) {
    return new VehicleResponse(
        vehicle.getId().getValue(),
        vehicle.getRegistrationPlate().getValue(),
        vehicle.getRfidNumber() != null ? vehicle.getRfidNumber().getValue() : null,
        vehicle.getStatus().name(),
        vehicle.getCurrentOwnerId(),
        vehicle.getCreatedAt(),
        vehicle.getDelistedAt());
  }
}
