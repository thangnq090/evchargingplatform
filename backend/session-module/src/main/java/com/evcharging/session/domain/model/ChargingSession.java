package com.evcharging.session.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

/** Aggregate root representing a charging session. */
public class ChargingSession {

  private final SessionId id;
  private final StationId stationId;
  private final Integer connectorId;
  private final UserId customerId;
  private final UUID vehicleId;
  private SessionStatus status;
  private final Instant startTime;
  private Instant endTime;
  private final Money unitRate;
  private BigDecimal totalEnergyKwh;
  private Money totalAmount;
  private String errorCode;
  private final Instant createdAt;
  private final List<MeterReading> meterReadings;

  private ChargingSession(
      SessionId id,
      StationId stationId,
      Integer connectorId,
      UserId customerId,
      UUID vehicleId,
      SessionStatus status,
      Instant startTime,
      Instant endTime,
      Money unitRate,
      BigDecimal totalEnergyKwh,
      Money totalAmount,
      String errorCode,
      Instant createdAt,
      List<MeterReading> meterReadings) {
    this.id = Objects.requireNonNull(id, "id must not be null");
    this.stationId = Objects.requireNonNull(stationId, "stationId must not be null");
    this.connectorId = Objects.requireNonNull(connectorId, "connectorId must not be null");
    this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
    this.vehicleId = vehicleId;
    this.status = Objects.requireNonNull(status, "status must not be null");
    this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
    this.endTime = endTime;
    this.unitRate = Objects.requireNonNull(unitRate, "unitRate must not be null");
    this.totalEnergyKwh = Objects.requireNonNull(totalEnergyKwh, "totalEnergyKwh must not be null");
    this.totalAmount = Objects.requireNonNull(totalAmount, "totalAmount must not be null");
    this.errorCode = errorCode;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
    this.meterReadings = new ArrayList<>(meterReadings);
  }

  /** Starts a new charging session. */
  public static ChargingSession start(
      StationId stationId, Integer connectorId, UserId customerId, UUID vehicleId, Money unitRate) {
    Instant now = Instant.now();
    SessionId id = SessionId.generate();
    return new ChargingSession(
        id,
        stationId,
        connectorId,
        customerId,
        vehicleId,
        SessionStatus.CHARGING,
        now,
        null,
        unitRate,
        BigDecimal.ZERO,
        Money.zero(unitRate.getCurrency()),
        null,
        now,
        new ArrayList<>());
  }

  /** Reconstitutes a charging session from persistence. */
  public static ChargingSession reconstitute(
      SessionId id,
      StationId stationId,
      Integer connectorId,
      UserId customerId,
      UUID vehicleId,
      SessionStatus status,
      Instant startTime,
      Instant endTime,
      Money unitRate,
      BigDecimal totalEnergyKwh,
      Money totalAmount,
      String errorCode,
      Instant createdAt,
      List<MeterReading> meterReadings) {
    return new ChargingSession(
        id,
        stationId,
        connectorId,
        customerId,
        vehicleId,
        status,
        startTime,
        endTime,
        unitRate,
        totalEnergyKwh,
        totalAmount,
        errorCode,
        createdAt,
        meterReadings);
  }

  /** Records a new meter reading. */
  public void recordMeterReading(MeterReading reading) {
    ensureCharging();
    Objects.requireNonNull(reading, "reading must not be null");

    if (reading.getTimestamp().isBefore(startTime)) {
      throw new IllegalArgumentException("Reading timestamp cannot be before session start time");
    }

    if (!meterReadings.isEmpty()) {
      MeterReading lastReading = meterReadings.get(meterReadings.size() - 1);
      if (reading.getTimestamp().isBefore(lastReading.getTimestamp())) {
        throw new IllegalArgumentException(
            "Reading timestamp cannot be before the last reading timestamp");
      }
      if (reading.getEnergyDeliveredKwh().compareTo(lastReading.getEnergyDeliveredKwh()) < 0) {
        throw new IllegalArgumentException("Energy delivered cannot decrease");
      }
    }

    this.meterReadings.add(reading);
    this.totalEnergyKwh = reading.getEnergyDeliveredKwh();
    this.totalAmount = unitRate.multiply(this.totalEnergyKwh);
  }

  /** Completes the charging session. */
  public void complete(Instant endTime, BigDecimal finalEnergy) {
    ensureCharging();
    Objects.requireNonNull(endTime, "endTime must not be null");
    Objects.requireNonNull(finalEnergy, "finalEnergy must not be null");

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
    if (finalEnergy.compareTo(totalEnergyKwh) < 0) {
      throw new IllegalArgumentException("Final energy cannot be less than current total energy");
    }

    this.status = SessionStatus.COMPLETED;
    this.endTime = endTime;
    this.totalEnergyKwh = finalEnergy;
    this.totalAmount = unitRate.multiply(finalEnergy);
  }

  /** Fails the charging session. */
  public void fail(Instant endTime, String errorCode, BigDecimal finalEnergy) {
    ensureCharging();
    Objects.requireNonNull(endTime, "endTime must not be null");
    Objects.requireNonNull(errorCode, "errorCode must not be null");
    Objects.requireNonNull(finalEnergy, "finalEnergy must not be null");

    if (endTime.isBefore(startTime)) {
      throw new IllegalArgumentException("End time cannot be before start time");
    }
    if (finalEnergy.compareTo(totalEnergyKwh) < 0) {
      throw new IllegalArgumentException("Final energy cannot be less than current total energy");
    }

    this.status = SessionStatus.FAILED;
    this.endTime = endTime;
    this.errorCode = errorCode;
    this.totalEnergyKwh = finalEnergy;
    this.totalAmount = unitRate.multiply(finalEnergy);
  }

  private void ensureCharging() {
    if (status != SessionStatus.CHARGING) {
      throw new IllegalStateException("Session is not in CHARGING state");
    }
  }

  // Getters
  public SessionId getId() {
    return id;
  }

  public StationId getStationId() {
    return stationId;
  }

  public Integer getConnectorId() {
    return connectorId;
  }

  public UserId getCustomerId() {
    return customerId;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public SessionStatus getStatus() {
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

  public List<MeterReading> getMeterReadings() {
    return Collections.unmodifiableList(meterReadings);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ChargingSession that = (ChargingSession) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
