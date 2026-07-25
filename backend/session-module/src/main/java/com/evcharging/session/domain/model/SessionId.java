package com.evcharging.session.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Strongly-typed identifier for ChargingSession entities. Prevents accidental mixing of different
 * entity IDs.
 */
public final class SessionId {

  private final UUID value;

  private SessionId(UUID value) {
    this.value = Objects.requireNonNull(value, "SessionId value cannot be null");
  }

  public static SessionId of(UUID value) {
    return new SessionId(value);
  }

  public static SessionId of(String value) {
    return new SessionId(UUID.fromString(value));
  }

  public static SessionId generate() {
    return new SessionId(UUID.randomUUID());
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
    SessionId sessionId = (SessionId) o;
    return Objects.equals(value, sessionId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
