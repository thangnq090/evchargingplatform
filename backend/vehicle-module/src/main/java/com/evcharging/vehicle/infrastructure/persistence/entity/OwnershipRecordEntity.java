package com.evcharging.vehicle.infrastructure.persistence.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.evcharging.vehicle.domain.model.OwnershipRecord;

@Entity
@Table(name = "ownership_records", schema = "vehicle")
public class OwnershipRecordEntity {

  @Id private UUID id;

  @Column(name = "vehicle_id", nullable = false)
  private UUID vehicleId;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "start_date", nullable = false)
  private Instant startDate;

  @Column(name = "end_date")
  private Instant endDate; // nullable — null = active

  public OwnershipRecordEntity() {}

  public static OwnershipRecordEntity fromDomain(OwnershipRecord record) {
    OwnershipRecordEntity entity = new OwnershipRecordEntity();
    entity.id = record.getId();
    entity.vehicleId = record.getVehicleId();
    entity.customerId = record.getCustomerId();
    entity.startDate = record.getStartDate();
    entity.endDate = record.getEndDate();
    return entity;
  }

  public OwnershipRecord toDomain() {
    return new OwnershipRecord(id, vehicleId, customerId, startDate, endDate);
  }

  // ── Getters & Setters ─────────────────────────────────────────────────────

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public void setVehicleId(UUID vehicleId) {
    this.vehicleId = vehicleId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public Instant getStartDate() {
    return startDate;
  }

  public void setStartDate(Instant startDate) {
    this.startDate = startDate;
  }

  public Instant getEndDate() {
    return endDate;
  }

  public void setEndDate(Instant endDate) {
    this.endDate = endDate;
  }
}
