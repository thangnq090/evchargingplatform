package com.evcharging.vehicle.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when a vehicle is successfully registered by a customer. */
public record VehicleRegisteredEvent(
    UUID vehicleId,
    UUID customerId,
    String registrationPlate,
    String rfidNumber, // nullable
    Instant registeredAt) {}
