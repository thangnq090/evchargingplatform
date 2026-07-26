package com.evcharging.vehicle.application.service;

import java.util.UUID;

/** Thrown when a requested vehicle does not exist. */
public class VehicleNotFoundException extends RuntimeException {

  private final UUID vehicleId;

  public VehicleNotFoundException(UUID vehicleId) {
    super("Vehicle not found: " + vehicleId);
    this.vehicleId = vehicleId;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }
}
