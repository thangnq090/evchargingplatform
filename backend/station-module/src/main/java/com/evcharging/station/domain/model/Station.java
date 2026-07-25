package com.evcharging.station.domain.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.evcharging.shared.kernel.Location;

/**
 * Aggregate root for a charging station (chargepoint). Encapsulates station metadata, location,
 * pricing, availability status, and connectors.
 */
public class Station {

  private final UUID id;
  private final UUID vendorId;
  private String name;
  private String groupLabel;
  private int unitPriceTenthCents;
  private StationStatus status;
  private final Location location;
  private final List<Connector> connectors;
  private final Instant createdAt;
  private Instant updatedAt;
  private Instant deletedAt;

  private Station(
      UUID id,
      UUID vendorId,
      String name,
      String groupLabel,
      int unitPriceTenthCents,
      StationStatus status,
      Location location,
      List<Connector> connectors,
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt) {
    this.id = id;
    this.vendorId = vendorId;
    this.name = name;
    this.groupLabel = groupLabel;
    this.unitPriceTenthCents = unitPriceTenthCents;
    this.status = status;
    this.location = location;
    this.connectors = new ArrayList<>(connectors);
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.deletedAt = deletedAt;
  }

  /**
   * Creates a new charging station.
   *
   * @param vendorId owning vendor
   * @param name station display name (unique within vendor)
   * @param groupLabel optional grouping label
   * @param unitPriceTenthCents price per kWh in tenths of cents (integer)
   * @param location geographic coordinates
   * @param connectors initial connector configurations
   */
  public static Station create(
      UUID vendorId,
      String name,
      String groupLabel,
      int unitPriceTenthCents,
      Location location,
      List<Connector> connectors) {
    Objects.requireNonNull(vendorId, "vendorId must not be null");
    Objects.requireNonNull(name, "Station name must not be null");
    if (name.isBlank()) {
      throw new IllegalArgumentException("Station name must not be blank");
    }
    Objects.requireNonNull(location, "Location must not be null");
    Objects.requireNonNull(connectors, "Connectors list must not be null");
    if (connectors.isEmpty()) {
      throw new IllegalArgumentException("Station must have at least one connector");
    }
    if (unitPriceTenthCents < 0) {
      throw new IllegalArgumentException("Unit price must be non-negative");
    }

    Instant now = Instant.now();
    Station station =
        new Station(
            UUID.randomUUID(),
            vendorId,
            name.trim(),
            groupLabel != null ? groupLabel.trim() : null,
            unitPriceTenthCents,
            StationStatus.AVAILABLE,
            location,
            connectors,
            now,
            now,
            null);

    // Set stationId on all connectors
    for (Connector connector : connectors) {
      // Connectors are already created with stationId via factory
    }

    return station;
  }

  /** Reconstitutes a station from persistence. */
  public static Station reconstitute(
      UUID id,
      UUID vendorId,
      String name,
      String groupLabel,
      int unitPriceTenthCents,
      StationStatus status,
      Location location,
      List<Connector> connectors,
      Instant createdAt,
      Instant updatedAt,
      Instant deletedAt) {
    return new Station(
        id,
        vendorId,
        name,
        groupLabel,
        unitPriceTenthCents,
        status,
        location,
        new ArrayList<>(connectors),
        createdAt,
        updatedAt,
        deletedAt);
  }

  /** Updates the station's name and group label. */
  public void update(
      String name, String groupLabel, Integer unitPriceTenthCents, Location location) {
    checkNotDeleted();
    if (name != null && !name.isBlank()) {
      this.name = name.trim();
    }
    if (groupLabel != null) {
      this.groupLabel = groupLabel.trim();
    }
    if (unitPriceTenthCents != null) {
      if (unitPriceTenthCents < 0) {
        throw new IllegalArgumentException("Unit price must be non-negative");
      }
      this.unitPriceTenthCents = unitPriceTenthCents;
    }
    // Location is immutable after creation for MVP
    this.updatedAt = Instant.now();
  }

  /** Changes the station's availability status. */
  public void changeStatus(StationStatus newStatus) {
    checkNotDeleted();
    Objects.requireNonNull(newStatus, "Status must not be null");
    if (this.status == newStatus) {
      return;
    }
    this.status = newStatus;
    this.updatedAt = Instant.now();
  }

  /** Soft-deletes the station. */
  public void delete() {
    if (this.deletedAt != null) {
      throw new IllegalStateException("Station is already deleted");
    }
    this.deletedAt = Instant.now();
    this.status = StationStatus.UNAVAILABLE;
    this.updatedAt = Instant.now();
  }

  /** Returns true if the station is soft-deleted. */
  public boolean isDeleted() {
    return deletedAt != null;
  }

  /** Returns true if the station is operational (not deleted, available status). */
  public boolean isOperational() {
    return !isDeleted() && status == StationStatus.AVAILABLE;
  }

  private void checkNotDeleted() {
    if (isDeleted()) {
      throw new IllegalStateException("Cannot modify deleted station");
    }
  }

  // Getters
  public UUID getId() {
    return id;
  }

  public UUID getVendorId() {
    return vendorId;
  }

  public String getName() {
    return name;
  }

  public String getGroupLabel() {
    return groupLabel;
  }

  public int getUnitPriceTenthCents() {
    return unitPriceTenthCents;
  }

  public StationStatus getStatus() {
    return status;
  }

  public Location getLocation() {
    return location;
  }

  public List<Connector> getConnectors() {
    return Collections.unmodifiableList(connectors);
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Station that = (Station) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
