package com.evcharging.shared.kernel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Value object representing a geographic location (WGS 84 coordinates).
 *
 * <p>Stored as PostGIS GEOGRAPHY(Point, 4326) in the database. Latitude: -90 to 90, Longitude: -180
 * to 180 (decimal degrees).
 */
public final class Location implements Serializable {

  private final BigDecimal latitude;
  private final BigDecimal longitude;

  private Location(BigDecimal latitude, BigDecimal longitude) {
    this.latitude = latitude;
    this.longitude = longitude;
  }

  /**
   * Creates a location from decimal degrees.
   *
   * @param latitude -90 to 90
   * @param longitude -180 to 180
   * @throws IllegalArgumentException if coordinates are out of range
   */
  public static Location of(double latitude, double longitude) {
    if (latitude < -90 || latitude > 90) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90");
    }
    if (longitude < -180 || longitude > 180) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180");
    }
    return new Location(BigDecimal.valueOf(latitude), BigDecimal.valueOf(longitude));
  }

  /**
   * Creates a location from BigDecimal values.
   *
   * @param latitude -90 to 90
   * @param longitude -180 to 180
   */
  @JsonCreator
  public static Location of(
      @JsonProperty("latitude") BigDecimal latitude,
      @JsonProperty("longitude") BigDecimal longitude) {
    Objects.requireNonNull(latitude, "latitude must not be null");
    Objects.requireNonNull(longitude, "longitude must not be null");
    if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0
        || latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
      throw new IllegalArgumentException("Latitude must be between -90 and 90");
    }
    if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0
        || longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
      throw new IllegalArgumentException("Longitude must be between -180 and 180");
    }
    return new Location(latitude, longitude);
  }

  /** Reconstitutes from persistence (PostGIS). */
  public static Location reconstitute(BigDecimal latitude, BigDecimal longitude) {
    return new Location(latitude, longitude);
  }

  public BigDecimal getLatitude() {
    return latitude;
  }

  public BigDecimal getLongitude() {
    return longitude;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Location location = (Location) o;
    return Objects.equals(latitude, location.latitude)
        && Objects.equals(longitude, location.longitude);
  }

  @Override
  public int hashCode() {
    return Objects.hash(latitude, longitude);
  }

  @Override
  public String toString() {
    return "Location{lat=" + latitude + ", lng=" + longitude + '}';
  }
}
