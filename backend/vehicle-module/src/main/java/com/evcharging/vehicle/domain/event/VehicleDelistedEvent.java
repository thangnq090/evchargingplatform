package com.evcharging.vehicle.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a vehicle is de-listed (soft-deleted) by its owner. */
public record VehicleDelistedEvent(
    UUID vehicleId, UUID customerId, String registrationPlate, Instant delistedAt) {}
