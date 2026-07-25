package com.evcharging.station.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.ConnectorStatus;
import com.evcharging.station.domain.model.ConnectorType;

/** JPA entity for Connector. */
@Entity
@Table(
    name = "connectors",
    schema = "station",
    indexes = @Index(name = "idx_connectors_station_id", columnList = "station_id"))
public class ConnectorJpaEntity {

  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "station_id", nullable = false)
  private StationJpaEntity station;

  @Column(name = "type", nullable = false, length = 20)
  private String type;

  @Column(name = "max_power_kw", nullable = false)
  private int maxPowerKw;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected ConnectorJpaEntity() {}

  public ConnectorJpaEntity(
      UUID id,
      StationJpaEntity station,
      String type,
      int maxPowerKw,
      String status,
      Instant createdAt) {
    this.id = id;
    this.station = station;
    this.type = type;
    this.maxPowerKw = maxPowerKw;
    this.status = status;
    this.createdAt = createdAt;
  }

  public static ConnectorJpaEntity from(
      Connector connector, StationJpaEntity stationEntity, boolean isNew) {
    ConnectorJpaEntity entity = new ConnectorJpaEntity();
    entity.id = connector.getId();
    entity.station = stationEntity;
    entity.type = connector.getType().name();
    entity.maxPowerKw = connector.getMaxPowerKw();
    entity.status = connector.getStatus().name();
    entity.createdAt = connector.getCreatedAt();
    return entity;
  }

  public Connector toDomain() {
    return Connector.reconstitute(
        id,
        station.getId(),
        ConnectorType.valueOf(type),
        maxPowerKw,
        ConnectorStatus.valueOf(status),
        createdAt);
  }

  // Getters/Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public StationJpaEntity getStation() {
    return station;
  }

  public void setStation(StationJpaEntity station) {
    this.station = station;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public int getMaxPowerKw() {
    return maxPowerKw;
  }

  public void setMaxPowerKw(int maxPowerKw) {
    this.maxPowerKw = maxPowerKw;
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
}
