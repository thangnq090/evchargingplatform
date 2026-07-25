package com.evcharging.session.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import com.evcharging.session.domain.model.MeterReading;
import com.evcharging.session.domain.model.SessionId;

@Entity
@Table(name = "meter_readings", schema = "session")
public class MeterReadingJpaEntity {

  @Id
  @Column(name = "id", columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "session_id", nullable = false)
  private ChargingSessionJpaEntity session;

  @Column(name = "timestamp", nullable = false)
  private Instant timestamp;

  @Column(name = "energy_delivered_kwh", nullable = false, precision = 19, scale = 4)
  private BigDecimal energyDeliveredKwh;

  @Column(name = "power_kw", nullable = false, precision = 19, scale = 4)
  private BigDecimal powerKw;

  protected MeterReadingJpaEntity() {}

  public static MeterReadingJpaEntity from(
      MeterReading reading, ChargingSessionJpaEntity sessionEntity, boolean isNew) {
    MeterReadingJpaEntity entity = new MeterReadingJpaEntity();
    entity.id = reading.getId();
    entity.session = sessionEntity;
    entity.timestamp = reading.getTimestamp();
    entity.energyDeliveredKwh = reading.getEnergyDeliveredKwh();
    entity.powerKw = reading.getPowerKw();
    return entity;
  }

  public MeterReading toDomain() {
    return MeterReading.reconstitute(
        id, SessionId.of(session.getId()), timestamp, energyDeliveredKwh, powerKw);
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public ChargingSessionJpaEntity getSession() {
    return session;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public BigDecimal getEnergyDeliveredKwh() {
    return energyDeliveredKwh;
  }

  public BigDecimal getPowerKw() {
    return powerKw;
  }
}
