package com.evcharging.session.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.SessionId;
import com.evcharging.session.domain.model.SessionStatus;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

@Entity
@Table(
    name = "charging_sessions",
    schema = "session",
    indexes = {
      @Index(name = "idx_sessions_customer_time", columnList = "customer_id, start_time DESC"),
      @Index(name = "idx_sessions_station_time", columnList = "station_id, start_time DESC"),
    })
public class ChargingSessionJpaEntity {

  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @Column(name = "station_id", nullable = false, columnDefinition = "uuid")
  private UUID stationId;

  @Column(name = "connector_id", nullable = false)
  private Integer connectorId;

  @Column(name = "customer_id", nullable = false, columnDefinition = "uuid")
  private UUID customerId;

  @Column(name = "vehicle_id", columnDefinition = "uuid")
  private UUID vehicleId;

  @Column(name = "status", nullable = false, length = 20)
  private String status;

  @Column(name = "start_time", nullable = false)
  private Instant startTime;

  @Column(name = "end_time")
  private Instant endTime;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "amount",
        column = @Column(name = "unit_rate_amount", nullable = false, precision = 19, scale = 4)),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "unit_rate_currency", nullable = false, length = 3))
  })
  private Money unitRate;

  @Column(name = "total_energy_kwh", nullable = false, precision = 19, scale = 4)
  private BigDecimal totalEnergyKwh;

  @Embedded
  @AttributeOverrides({
    @AttributeOverride(
        name = "amount",
        column =
            @Column(name = "total_amount_amount", nullable = false, precision = 19, scale = 4)),
    @AttributeOverride(
        name = "currency",
        column = @Column(name = "total_amount_currency", nullable = false, length = 3))
  })
  private Money totalAmount;

  @Column(name = "error_code", length = 50)
  private String errorCode;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @OneToMany(
      mappedBy = "session",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<MeterReadingJpaEntity> meterReadings = new ArrayList<>();

  @Version
  @Column(name = "version")
  private int version;

  protected ChargingSessionJpaEntity() {}

  public void updateFrom(ChargingSession session) {
    this.status = session.getStatus().name();
    this.endTime = session.getEndTime();
    this.totalEnergyKwh = session.getTotalEnergyKwh();
    this.totalAmount = session.getTotalAmount();
    this.errorCode = session.getErrorCode();

    this.meterReadings.clear();
    if (session.getMeterReadings() != null) {
      session
          .getMeterReadings()
          .forEach(r -> this.meterReadings.add(MeterReadingJpaEntity.from(r, this, false)));
    }
  }

  public static ChargingSessionJpaEntity from(ChargingSession session, boolean isNew) {
    ChargingSessionJpaEntity entity = new ChargingSessionJpaEntity();
    entity.id = session.getId().getValue();
    entity.stationId = session.getStationId().getValue();
    entity.connectorId = session.getConnectorId();
    entity.customerId = session.getCustomerId().getValue();
    entity.vehicleId = session.getVehicleId();
    entity.status = session.getStatus().name();
    entity.startTime = session.getStartTime();
    entity.endTime = session.getEndTime();
    entity.unitRate = session.getUnitRate();
    entity.totalEnergyKwh = session.getTotalEnergyKwh();
    entity.totalAmount = session.getTotalAmount();
    entity.errorCode = session.getErrorCode();
    entity.createdAt = session.getCreatedAt();

    if (session.getMeterReadings() != null) {
      session
          .getMeterReadings()
          .forEach(r -> entity.meterReadings.add(MeterReadingJpaEntity.from(r, entity, isNew)));
    }
    return entity;
  }

  public ChargingSession toDomain() {
    return ChargingSession.reconstitute(
        SessionId.of(id),
        StationId.of(stationId),
        connectorId,
        UserId.of(customerId),
        vehicleId,
        SessionStatus.valueOf(status),
        startTime,
        endTime,
        unitRate,
        totalEnergyKwh,
        totalAmount,
        errorCode,
        createdAt,
        meterReadings.stream().map(MeterReadingJpaEntity::toDomain).toList());
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public UUID getStationId() {
    return stationId;
  }

  public Integer getConnectorId() {
    return connectorId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public String getStatus() {
    return status;
  }

  public Instant getStartTime() {
    return startTime;
  }

  public Instant getEndTime() {
    return endTime;
  }

  public Money getUnitRate() {
    return unitRate;
  }

  public BigDecimal getTotalEnergyKwh() {
    return totalEnergyKwh;
  }

  public Money getTotalAmount() {
    return totalAmount;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public List<MeterReadingJpaEntity> getMeterReadings() {
    return meterReadings;
  }

  public int getVersion() {
    return version;
  }
}
