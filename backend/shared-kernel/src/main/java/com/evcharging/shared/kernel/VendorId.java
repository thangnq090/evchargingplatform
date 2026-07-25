package com.evcharging.shared.kernel;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for Vendor entities. Prevents accidental mixing of different entity
 * IDs.
 */
public final class VendorId {

  private final UUID value;

  private VendorId(UUID value) {
    this.value = Objects.requireNonNull(value, "VendorId value cannot be null");
  }

  /** Creates a new VendorId from a UUID. */
  public static VendorId of(UUID value) {
    return new VendorId(value);
  }

  /** Creates a new VendorId from a string representation. */
  public static VendorId of(String value) {
    return new VendorId(UUID.fromString(value));
  }

  /** Generates a new random VendorId. */
  public static VendorId generate() {
    return new VendorId(UUID.randomUUID());
  }

  /** Returns the underlying UUID value. */
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
    VendorId vendorId = (VendorId) o;
    return Objects.equals(value, vendorId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
