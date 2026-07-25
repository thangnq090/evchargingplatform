package com.evcharging.session.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataChargingSessionRepository
    extends JpaRepository<ChargingSessionJpaEntity, UUID> {

  List<ChargingSessionJpaEntity> findByCustomerIdAndStartTimeBetween(
      UUID customerId, Instant start, Instant end);

  List<ChargingSessionJpaEntity> findByStationIdAndStartTimeBetween(
      UUID stationId, Instant start, Instant end);
}
