package com.evcharging.session.domain.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.SessionId;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

/** Domain port interface for ChargingSession persistence. */
public interface ChargingSessionRepository {

  ChargingSession save(ChargingSession session);

  Optional<ChargingSession> findById(SessionId id);

  List<ChargingSession> findByCustomerIdAndStartTimeBetween(
      UserId customerId, Instant start, Instant end);

  List<ChargingSession> findByStationIdAndStartTimeBetween(
      StationId stationId, Instant start, Instant end);

  /** Full-text search across sessions. Returns lightweight results. */
  List<SessionSearchResult> searchSessions(String query);

  /** Lightweight search result that avoids infrastructure type exposure. */
  record SessionSearchResult(
      UUID id,
      UUID stationId,
      Integer connectorId,
      UUID customerId,
      String customerAccountNumber,
      UUID vehicleId,
      String registrationPlate,
      String status,
      Instant startTime,
      Instant endTime,
      BigDecimal unitRateAmount,
      String unitRateCurrency,
      BigDecimal totalEnergyKwh,
      BigDecimal totalAmountAmount,
      String totalAmountCurrency,
      String errorCode,
      Instant createdAt) {}
}
