package com.evcharging.shared.kernel;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for User entities. Prevents accidental mixing of different entity IDs.
 */
public final class UserId {

  private final UUID value;

  private UserId(UUID value) {
    this.value = Objects.requireNonNull(value, "UserId value cannot be null");
  }

  /** Creates a new UserId from a UUID. */
  public static UserId of(UUID value) {
    return new UserId(value);
  }

  /** Creates a new UserId from a string representation. */
  public static UserId of(String value) {
    return new UserId(UUID.fromString(value));
  }

  /** Generates a new random UserId. */
  public static UserId generate() {
    return new UserId(UUID.randomUUID());
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
    UserId userId = (UserId) o;
    return Objects.equals(value, userId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
