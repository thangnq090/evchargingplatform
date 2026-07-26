package com.evcharging.vehicle.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Ownership history record linking a customer to a vehicle for a specific time period. */
public class OwnershipRecord {

  private final UUID id;
  private final UUID vehicleId;
  private final UUID customerId;
  private final Instant startDate;
  private Instant endDate; // null = active ownership

  public OwnershipRecord(
      UUID id, UUID vehicleId, UUID customerId, Instant startDate, Instant endDate) {
    this.id = Objects.requireNonNull(id, "OwnershipRecord ID cannot be null");
    this.vehicleId = Objects.requireNonNull(vehicleId, "Vehicle ID cannot be null");
    this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
    this.startDate = Objects.requireNonNull(startDate, "Start date cannot be null");
    this.endDate = endDate; // nullable
  }

  /** Factory: create a new active ownership record. */
  public static OwnershipRecord createActive(UUID vehicleId, UUID customerId, Instant startDate) {
    return new OwnershipRecord(UUID.randomUUID(), vehicleId, customerId, startDate, null);
  }

  /** Closes this ownership record by setting an end date. */
  public void close(Instant endDate) {
    if (this.endDate != null) {
      throw new IllegalStateException("Ownership record " + id + " is already closed");
    }
    this.endDate = Objects.requireNonNull(endDate, "End date cannot be null");
  }

  public boolean isActive() {
    return endDate == null;
  }

  public UUID getId() {
    return id;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public Instant getStartDate() {
    return startDate;
  }

  public Instant getEndDate() {
    return endDate;
  }
}
