package com.evcharging.vehicle.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate root for vehicle lifecycle management.
 *
 * <p>Vehicles belong to platform customers and are independent of vendors. Invariants enforced:
 *
 * <ul>
 *   <li>A DE_LISTED vehicle cannot be mutated.
 *   <li>RFID can only be associated while the vehicle is ACTIVE.
 * </ul>
 */
public class Vehicle {

  private final VehicleId id;
  private final RegistrationPlate registrationPlate;
  private RfidNumber rfidNumber;
  private final UUID currentOwnerId;
  private VehicleStatus status;
  private final Instant createdAt;
  private Instant delistedAt;

  /** Full constructor — used by persistence adapter to reconstitute from database. */
  public Vehicle(
      VehicleId id,
      RegistrationPlate registrationPlate,
      RfidNumber rfidNumber,
      UUID currentOwnerId,
      VehicleStatus status,
      Instant createdAt,
      Instant delistedAt) {
    this.id = Objects.requireNonNull(id, "Vehicle ID cannot be null");
    this.registrationPlate =
        Objects.requireNonNull(registrationPlate, "Registration plate cannot be null");
    this.rfidNumber = rfidNumber; // nullable
    this.currentOwnerId = Objects.requireNonNull(currentOwnerId, "Owner ID cannot be null");
    this.status = Objects.requireNonNull(status, "Status cannot be null");
    this.createdAt = Objects.requireNonNull(createdAt, "Created timestamp cannot be null");
    this.delistedAt = delistedAt; // nullable
  }

  /**
   * Factory method: register a new vehicle for a customer.
   *
   * @param plate normalised registration plate
   * @param rfid optional RFID tag
   * @param ownerId customer who owns the vehicle
   * @param createdAt registration timestamp
   * @return new ACTIVE vehicle
   */
  public static Vehicle register(
      RegistrationPlate plate, RfidNumber rfid, UUID ownerId, Instant createdAt) {
    return new Vehicle(
        VehicleId.generate(), plate, rfid, ownerId, VehicleStatus.ACTIVE, createdAt, null);
  }

  /**
   * Associate an RFID tag with this vehicle.
   *
   * @param rfid RFID to associate
   * @throws IllegalStateException if vehicle is DE_LISTED
   * @throws IllegalStateException if an RFID is already associated
   */
  public void associateRfid(RfidNumber rfid) {
    requireActive("associate RFID");
    if (this.rfidNumber != null) {
      throw new IllegalStateException(
          "Vehicle " + id + " already has an RFID associated. Use admin endpoint to change it.");
    }
    this.rfidNumber = Objects.requireNonNull(rfid, "RFID cannot be null");
  }

  /**
   * De-list (soft-delete) this vehicle.
   *
   * @param delistedAt timestamp of de-listing
   * @throws IllegalStateException if already DE_LISTED
   */
  public void delist(Instant delistedAt) {
    requireActive("delist");
    this.status = VehicleStatus.DE_LISTED;
    this.delistedAt = Objects.requireNonNull(delistedAt, "Delisted timestamp cannot be null");
  }

  // ── Invariant Guards ──────────────────────────────────────────────────────

  private void requireActive(String operation) {
    if (this.status != VehicleStatus.ACTIVE) {
      throw new IllegalStateException("Cannot " + operation + " a DE_LISTED vehicle: " + id);
    }
  }

  // ── Getters ───────────────────────────────────────────────────────────────

  public VehicleId getId() {
    return id;
  }

  public RegistrationPlate getRegistrationPlate() {
    return registrationPlate;
  }

  public RfidNumber getRfidNumber() {
    return rfidNumber;
  }

  public UUID getCurrentOwnerId() {
    return currentOwnerId;
  }

  public VehicleStatus getStatus() {
    return status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getDelistedAt() {
    return delistedAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Vehicle vehicle = (Vehicle) o;
    return Objects.equals(id, vehicle.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
