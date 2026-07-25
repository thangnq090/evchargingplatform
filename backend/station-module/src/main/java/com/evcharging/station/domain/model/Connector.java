package com.evcharging.station.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity representing a physical charging connector on a station. Each connector has a plug type,
 * maximum power output, and operational status.
 */
public class Connector {

  private final UUID id;
  private final UUID stationId;
  private final ConnectorType type;
  private final int maxPowerKw;
  private ConnectorStatus status;
  private final Instant createdAt;

  private Connector(
      UUID id,
      UUID stationId,
      ConnectorType type,
      int maxPowerKw,
      ConnectorStatus status,
      Instant createdAt) {
    this.id = id;
    this.stationId = stationId;
    this.type = type;
    this.maxPowerKw = maxPowerKw;
    this.status = status;
    this.createdAt = createdAt;
  }

  /** Creates a new connector for a station. */
  public static Connector create(UUID stationId, ConnectorType type, int maxPowerKw) {
    Objects.requireNonNull(stationId, "stationId must not be null");
    Objects.requireNonNull(type, "ConnectorType must not be null");
    if (maxPowerKw <= 0 || maxPowerKw > 500) {
      throw new IllegalArgumentException("Max power must be between 1 and 500 kW");
    }
    return new Connector(
        UUID.randomUUID(), stationId, type, maxPowerKw, ConnectorStatus.AVAILABLE, Instant.now());
  }

  /** Reconstitutes a connector from persistence. */
  public static Connector reconstitute(
      UUID id,
      UUID stationId,
      ConnectorType type,
      int maxPowerKw,
      ConnectorStatus status,
      Instant createdAt) {
    return new Connector(id, stationId, type, maxPowerKw, status, createdAt);
  }

  /** Mark connector as in use (reserved by a charging session). */
  public void markInUse() {
    if (this.status != ConnectorStatus.AVAILABLE) {
      throw new IllegalStateException(
          "Connector must be AVAILABLE to mark in use, but was " + this.status);
    }
    this.status = ConnectorStatus.IN_USE;
  }

  /** Mark connector as unavailable (fault or maintenance). */
  public void markUnavailable() {
    this.status = ConnectorStatus.UNAVAILABLE;
  }

  /** Mark connector as available. */
  public void markAvailable() {
    this.status = ConnectorStatus.AVAILABLE;
  }

  /** Returns true if the connector is available for a new session. */
  public boolean isAvailable() {
    return this.status == ConnectorStatus.AVAILABLE;
  }

  public UUID getId() {
    return id;
  }

  public UUID getStationId() {
    return stationId;
  }

  public ConnectorType getType() {
    return type;
  }

  public int getMaxPowerKw() {
    return maxPowerKw;
  }

  public ConnectorStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Connector that = (Connector) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
