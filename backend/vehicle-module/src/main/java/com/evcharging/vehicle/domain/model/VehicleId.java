package com.evcharging.vehicle.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Value object representing a unique vehicle identifier. */
public final class VehicleId implements Serializable {

  private final UUID value;

  private VehicleId(UUID value) {
    this.value = Objects.requireNonNull(value, "VehicleId value cannot be null");
  }

  public static VehicleId of(UUID value) {
    return new VehicleId(value);
  }

  public static VehicleId generate() {
    return new VehicleId(UUID.randomUUID());
  }

  public UUID getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    VehicleId vehicleId = (VehicleId) o;
    return Objects.equals(value, vehicleId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
