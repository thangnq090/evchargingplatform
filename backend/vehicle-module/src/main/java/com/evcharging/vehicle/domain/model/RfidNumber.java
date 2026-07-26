package com.evcharging.vehicle.domain.model;

import java.util.Objects;

/**
 * Value object representing an RFID (Radio-Frequency Identification) tag number.
 *
 * <p>RFID values are stored as-is (case-preserved) and compared case-insensitively. Max 50
 * characters.
 */
public final class RfidNumber {

  private static final int MAX_LENGTH = 50;

  private final String value;

  private RfidNumber(String value) {
    this.value = value;
  }

  /**
   * Constructs an {@link RfidNumber} from a raw string.
   *
   * @param raw the RFID tag value (trimmed internally)
   * @return value object
   * @throws IllegalArgumentException if null, blank, or exceeds max length
   */
  public static RfidNumber of(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("RFID number must not be blank");
    }
    String trimmed = raw.trim();
    if (trimmed.length() > MAX_LENGTH) {
      throw new IllegalArgumentException(
          "RFID number exceeds max length of " + MAX_LENGTH + " characters");
    }
    return new RfidNumber(trimmed);
  }

  public String getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    RfidNumber that = (RfidNumber) o;
    return value.equalsIgnoreCase(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value.toUpperCase());
  }

  @Override
  public String toString() {
    return value;
  }
}
