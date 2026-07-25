package com.evcharging.session.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/** Entity representing a periodic meter/energy reading during a charging session. */
public final class MeterReading {

  private final UUID id;
  private final SessionId sessionId;
  private final Instant timestamp;
  private final BigDecimal energyDeliveredKwh;
  private final BigDecimal powerKw;

  private MeterReading(
      UUID id,
      SessionId sessionId,
      Instant timestamp,
      BigDecimal energyDeliveredKwh,
      BigDecimal powerKw) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.sessionId = Objects.requireNonNull(sessionId, "sessionId must not be null");
    this.timestamp = Objects.requireNonNull(timestamp, "timestamp must not be null");
    this.energyDeliveredKwh =
        Objects.requireNonNull(energyDeliveredKwh, "energyDeliveredKwh must not be null");
    this.powerKw = Objects.requireNonNull(powerKw, "powerKw must not be null");
  }

  public static MeterReading create(
      SessionId sessionId, Instant timestamp, BigDecimal energyDeliveredKwh, BigDecimal powerKw) {
    return new MeterReading(UUID.randomUUID(), sessionId, timestamp, energyDeliveredKwh, powerKw);
  }

  public static MeterReading reconstitute(
      UUID id,
      SessionId sessionId,
      Instant timestamp,
      BigDecimal energyDeliveredKwh,
      BigDecimal powerKw) {
    return new MeterReading(id, sessionId, timestamp, energyDeliveredKwh, powerKw);
  }

  public UUID getId() {
    return id;
  }

  public SessionId getSessionId() {
    return sessionId;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    MeterReading that = (MeterReading) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
