package com.evcharging.session.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
}
