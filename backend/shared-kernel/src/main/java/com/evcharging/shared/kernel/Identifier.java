package com.evcharging.shared.kernel;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.MappedSuperclass;

/**
 * Base class for strongly-typed identifier value objects.
 *
 * <p>Provides type-safe IDs using UUIDs with compile-time type checking. Subclasses represent
 * specific domain identifiers (e.g., {@link UserId}, {@link StationId}).
 *
 * <p>Uses {@link EmbeddedId} for JPA persistence - each ID is stored as a UUID column.
 *
 * @param <T> The concrete ID type (self-referencing for type safety)
 */
@MappedSuperclass
public abstract class Identifier<T extends Identifier<T>> implements Serializable, Comparable<T> {

  protected UUID value;

  protected Identifier() {
    // JPA no-arg constructor
  }

  protected Identifier(UUID value) {
    this.value = Objects.requireNonNull(value, "Identifier value cannot be null");
  }

  /** Creates a new instance with a randomly generated UUID. */
  public static <T extends Identifier<T>> T generate(Class<T> clazz) {
    try {
      return clazz.getDeclaredConstructor(UUID.class).newInstance(UUID.randomUUID());
    } catch (Exception e) {
      throw new IllegalStateException("Failed to generate identifier", e);
    }
  }

  /** Creates an instance from an existing UUID string. */
  public static <T extends Identifier<T>> T fromString(Class<T> clazz, String uuidString) {
    try {
      return clazz.getDeclaredConstructor(UUID.class).newInstance(UUID.fromString(uuidString));
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid UUID format: " + uuidString, e);
    }
  }

  /** Creates an instance from a UUID. */
  public static <T extends Identifier<T>> T fromUUID(Class<T> clazz, UUID uuid) {
    try {
      return clazz.getDeclaredConstructor(UUID.class).newInstance(uuid);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create identifier from UUID", e);
    }
  }

  public UUID getValue() {
    return value;
  }

  @Override
  public int compareTo(T other) {
    return this.value.compareTo(other.value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Identifier<?> that = (Identifier<?>) o;
    return Objects.equals(value, that.value);
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
