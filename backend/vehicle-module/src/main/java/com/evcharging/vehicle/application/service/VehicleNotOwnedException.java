package com.evcharging.vehicle.application.service;

import java.util.UUID;

/** Thrown when the requesting customer does not own the target vehicle. */
public class VehicleNotOwnedException extends RuntimeException {

  public VehicleNotOwnedException(UUID vehicleId, UUID customerId) {
    super("Customer " + customerId + " does not own vehicle " + vehicleId);
  }
}
