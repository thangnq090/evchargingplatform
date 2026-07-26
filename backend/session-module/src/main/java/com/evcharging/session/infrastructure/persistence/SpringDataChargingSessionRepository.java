package com.evcharging.session.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataChargingSessionRepository
    extends JpaRepository<ChargingSessionJpaEntity, UUID> {

  List<ChargingSessionJpaEntity> findByCustomerIdAndStartTimeBetween(
      UUID customerId, Instant start, Instant end);

  List<ChargingSessionJpaEntity> findByStationIdAndStartTimeBetween(
      UUID stationId, Instant start, Instant end);

  @Query(
      value =
          """
      SELECT
          s.id AS id,
          s.station_id AS stationId,
          s.connector_id AS connectorId,
          s.customer_id AS customerId,
          u.account_number AS customerAccountNumber,
          s.vehicle_id AS vehicleId,
          v.registration_plate AS registrationPlate,
          s.status AS status,
          s.start_time AS startTime,
          s.end_time AS endTime,
          s.unit_rate_amount AS unitRateAmount,
          s.unit_rate_currency AS unitRateCurrency,
          s.total_energy_kwh AS totalEnergyKwh,
          s.total_amount_amount AS totalAmountAmount,
          s.total_amount_currency AS totalAmountCurrency,
          s.error_code AS errorCode,
          s.created_at AS createdAt
      FROM session.charging_sessions s
      LEFT JOIN vehicle.vehicles v ON s.vehicle_id = v.id
      LEFT JOIN identity.users u ON s.customer_id = u.id
      WHERE (:query IS NULL OR :query = ''
         OR CAST(s.id AS text) ILIKE '%' || :query || '%'
         OR v.registration_plate ILIKE '%' || :query || '%'
         OR u.account_number ILIKE '%' || :query || '%'
         OR s.error_code ILIKE '%' || :query || '%')
      ORDER BY s.start_time DESC
      """,
      nativeQuery = true)
  List<SessionSearchResultProjection> searchSessions(@Param("query") String query);

  interface SessionSearchResultProjection {
    UUID getId();

    UUID getStationId();

    Integer getConnectorId();

    UUID getCustomerId();

    String getCustomerAccountNumber();

    UUID getVehicleId();

    String getRegistrationPlate();

    String getStatus();

    Instant getStartTime();

    Instant getEndTime();

    BigDecimal getUnitRateAmount();

    String getUnitRateCurrency();

    BigDecimal getTotalEnergyKwh();

    BigDecimal getTotalAmountAmount();

    String getTotalAmountCurrency();

    String getErrorCode();

    Instant getCreatedAt();
  }
}
