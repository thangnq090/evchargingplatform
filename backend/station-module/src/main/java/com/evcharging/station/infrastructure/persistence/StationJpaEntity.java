package com.evcharging.station.infrastructure.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import com.evcharging.shared.kernel.Location;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;

/** JPA entity for Station. */
@Entity
@Table(
    name = "stations",
    schema = "station",
    indexes = {
      @Index(name = "idx_stations_vendor_id", columnList = "vendor_id"),
      @Index(name = "idx_stations_status", columnList = "status"),
    })
public class StationJpaEntity {

  private static final GeometryFactory GEOMETRY_FACTORY =
      new GeometryFactory(new PrecisionModel(), 4326);

  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "vendor_id", nullable = false, columnDefinition = "uuid")
  private UUID vendorId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "group_label", length = 50)
  private String groupLabel;

  @Column(name = "unit_price_tenth_cents", nullable = false)
  private int unitPriceTenthCents;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326)")
  private Point location;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @Column(name = "deleted_at")
  private Instant deletedAt;

  @OneToMany(
      mappedBy = "station",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<ConnectorJpaEntity> connectors = new ArrayList<>();

  protected StationJpaEntity() {}

  public static StationJpaEntity from(Station station, boolean isNew) {
    StationJpaEntity entity = new StationJpaEntity();
    entity.id = station.getId();
    entity.vendorId = station.getVendorId();
    entity.name = station.getName();
    entity.groupLabel = station.getGroupLabel();
    entity.unitPriceTenthCents = station.getUnitPriceTenthCents();
    entity.status = station.getStatus().name();
    entity.location = toPoint(station.getLocation());
    entity.createdAt = station.getCreatedAt();
    entity.updatedAt = station.getUpdatedAt();
    entity.deletedAt = station.getDeletedAt();

    if (station.getConnectors() != null) {
      for (Connector connector : station.getConnectors()) {
        entity.connectors.add(ConnectorJpaEntity.from(connector, entity, isNew));
      }
    }

    return entity;
  }

  public Station toDomain() {
    List<Connector> domainConnectors =
        connectors.stream().map(ConnectorJpaEntity::toDomain).toList();

    return Station.reconstitute(
        id,
        vendorId,
        name,
        groupLabel,
        unitPriceTenthCents,
        StationStatus.valueOf(status),
        toLocation(location),
        domainConnectors,
        createdAt,
        updatedAt,
        deletedAt);
  }

  private static Point toPoint(Location location) {
    if (location == null) return null;
    return GEOMETRY_FACTORY.createPoint(
        new Coordinate(
            location.getLongitude().doubleValue(), location.getLatitude().doubleValue()));
  }

  private static Location toLocation(Point point) {
    if (point == null) return null;
    return Location.of(point.getY(), point.getX());
  }

  // Getters/Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getVendorId() {
    return vendorId;
  }

  public void setVendorId(UUID vendorId) {
    this.vendorId = vendorId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getGroupLabel() {
    return groupLabel;
  }

  public void setGroupLabel(String groupLabel) {
    this.groupLabel = groupLabel;
  }

  public int getUnitPriceTenthCents() {
    return unitPriceTenthCents;
  }

  public void setUnitPriceTenthCents(int unitPriceTenthCents) {
    this.unitPriceTenthCents = unitPriceTenthCents;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Point getLocation() {
    return location;
  }

  public void setLocation(Point location) {
    this.location = location;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }

  public void setDeletedAt(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  public List<ConnectorJpaEntity> getConnectors() {
    return connectors;
  }

  public void setConnectors(List<ConnectorJpaEntity> connectors) {
    this.connectors = connectors;
  }
}
