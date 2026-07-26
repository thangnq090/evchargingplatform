package com.evcharging.vehicle.domain.event;

import java.time.Instant;
import java.util.UUID;

/** Published when an RFID tag is associated with a vehicle. */
public record RfidAssociatedEvent(UUID vehicleId, String rfidNumber, Instant associatedAt) {}
