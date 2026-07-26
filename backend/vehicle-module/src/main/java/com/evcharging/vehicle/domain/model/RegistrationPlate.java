package com.evcharging.vehicle.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value object representing a normalised vehicle registration plate.
 *
 * <p>Input is upper-cased and trimmed. Only alphanumeric characters and hyphens are permitted (1–20
 * characters).
 */
public final class RegistrationPlate {

  private static final Pattern VALID_PLATE = Pattern.compile("^[A-Z0-9\\-]{1,20}$");

  private final String value;

  private RegistrationPlate(String value) {
    this.value = value;
  }

  /**
   * Constructs a {@link RegistrationPlate} from raw user input.
   *
   * @param raw the raw plate string (case-insensitive, trimmed internally)
   * @return normalised value object
   * @throws IllegalArgumentException if the plate is null, blank, or fails pattern validation
   */
  public static RegistrationPlate of(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("Registration plate must not be blank");
    }
    String normalised = raw.trim().toUpperCase();
    if (!VALID_PLATE.matcher(normalised).matches()) {
      throw new IllegalArgumentException(
          "Invalid registration plate format: '"
              + raw
              + "'. Must be 1-20 alphanumeric characters or hyphens.");
    }
    return new RegistrationPlate(normalised);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RegistrationPlate that = (RegistrationPlate) o;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
