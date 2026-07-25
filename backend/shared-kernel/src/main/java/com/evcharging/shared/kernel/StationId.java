package com.evcharging.shared.kernel;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for Station entities. Prevents accidental mixing of different entity
 * IDs.
 */
public final class StationId {

  private final UUID value;

  private StationId(UUID value) {
    this.value = Objects.requireNonNull(value, "StationId value cannot be null");
  }

  public static StationId of(UUID value) {
    return new StationId(value);
  }

  public static StationId of(String value) {
    return new StationId(UUID.fromString(value));
  }

  public static StationId generate() {
    return new StationId(UUID.randomUUID());
  }

  public UUID getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    StationId stationId = (StationId) o;
    return Objects.equals(value, stationId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
