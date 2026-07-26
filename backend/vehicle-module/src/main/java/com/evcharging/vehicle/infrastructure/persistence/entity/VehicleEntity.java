package com.evcharging.vehicle.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.evcharging.vehicle.domain.model.RegistrationPlate;
import com.evcharging.vehicle.domain.model.RfidNumber;
import com.evcharging.vehicle.domain.model.Vehicle;
import com.evcharging.vehicle.domain.model.VehicleId;
import com.evcharging.vehicle.domain.model.VehicleStatus;

@Entity
@Table(name = "vehicles", schema = "vehicle")
public class VehicleEntity {

  @Id private UUID id;

  @Column(name = "registration_plate", nullable = false, length = 20)
  private String registrationPlate;

  @Column(name = "rfid_number", length = 50)
  private String rfidNumber; // nullable

  @Column(name = "current_owner_id", nullable = false)
  private UUID currentOwnerId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "delisted_at")
  private Instant delistedAt; // nullable

  @Version private int version;

  public VehicleEntity() {}

  /** Maps a domain Vehicle to its JPA entity representation. */
  public static VehicleEntity fromDomain(Vehicle vehicle) {
    VehicleEntity entity = new VehicleEntity();
    entity.id = vehicle.getId().getValue();
    entity.registrationPlate = vehicle.getRegistrationPlate().getValue();
    entity.rfidNumber = vehicle.getRfidNumber() != null ? vehicle.getRfidNumber().getValue() : null;
    entity.currentOwnerId = vehicle.getCurrentOwnerId();
    entity.status = vehicle.getStatus().name();
    entity.createdAt = vehicle.getCreatedAt();
    entity.delistedAt = vehicle.getDelistedAt();
    return entity;
  }

  /** Reconstitutes a domain Vehicle from this JPA entity. */
  public Vehicle toDomain() {
    return new Vehicle(
        VehicleId.of(id),
        RegistrationPlate.of(registrationPlate),
        rfidNumber != null ? RfidNumber.of(rfidNumber) : null,
        currentOwnerId,
        VehicleStatus.valueOf(status),
        createdAt,
        delistedAt);
  }

  // ── Getters & Setters ─────────────────────────────────────────────────────

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getRegistrationPlate() {
    return registrationPlate;
  }

  public void setRegistrationPlate(String registrationPlate) {
    this.registrationPlate = registrationPlate;
  }

  public String getRfidNumber() {
    return rfidNumber;
  }

  public void setRfidNumber(String rfidNumber) {
    this.rfidNumber = rfidNumber;
  }

  public UUID getCurrentOwnerId() {
    return currentOwnerId;
  }

  public void setCurrentOwnerId(UUID currentOwnerId) {
    this.currentOwnerId = currentOwnerId;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getDelistedAt() {
    return delistedAt;
  }

  public void setDelistedAt(Instant delistedAt) {
    this.delistedAt = delistedAt;
  }

  public int getVersion() {
    return version;
  }
}
