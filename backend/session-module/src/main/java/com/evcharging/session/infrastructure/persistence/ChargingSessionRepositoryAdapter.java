package com.evcharging.session.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.SessionId;
import com.evcharging.session.domain.repository.ChargingSessionRepository;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

@Repository("sessionChargingSessionRepositoryAdapter")
public class ChargingSessionRepositoryAdapter implements ChargingSessionRepository {

  private final SpringDataChargingSessionRepository springDataRepository;

  public ChargingSessionRepositoryAdapter(
      SpringDataChargingSessionRepository springDataRepository) {
    this.springDataRepository = springDataRepository;
  }

  @Override
  public ChargingSession save(ChargingSession session) {
    Optional<ChargingSessionJpaEntity> existing =
        springDataRepository.findById(session.getId().getValue());
    ChargingSessionJpaEntity jpaEntity;
    if (existing.isPresent()) {
      jpaEntity = existing.get();
      jpaEntity.updateFrom(session);
    } else {
      jpaEntity = ChargingSessionJpaEntity.from(session, true);
    }
    ChargingSessionJpaEntity savedEntity = springDataRepository.save(jpaEntity);
    return savedEntity.toDomain();
  }

  @Override
  public Optional<ChargingSession> findById(SessionId id) {
    return springDataRepository.findById(id.getValue()).map(ChargingSessionJpaEntity::toDomain);
  }

  @Override
  public List<ChargingSession> findByCustomerIdAndStartTimeBetween(
      UserId customerId, Instant start, Instant end) {
    return springDataRepository
        .findByCustomerIdAndStartTimeBetween(customerId.getValue(), start, end)
        .stream()
        .map(ChargingSessionJpaEntity::toDomain)
        .toList();
  }

  @Override
  public List<ChargingSession> findByStationIdAndStartTimeBetween(
      StationId stationId, Instant start, Instant end) {
    return springDataRepository
        .findByStationIdAndStartTimeBetween(stationId.getValue(), start, end)
        .stream()
        .map(ChargingSessionJpaEntity::toDomain)
        .toList();
  }

  @Override
  public List<ChargingSessionRepository.SessionSearchResult> searchSessions(String query) {
    List<SpringDataChargingSessionRepository.SessionSearchResultProjection> projections =
        springDataRepository.searchSessions(query);
    return projections.stream()
        .map(
            p ->
                new ChargingSessionRepository.SessionSearchResult(
                    p.getId(),
                    p.getStationId(),
                    p.getConnectorId(),
                    p.getCustomerId(),
                    p.getCustomerAccountNumber(),
                    p.getVehicleId(),
                    p.getRegistrationPlate(),
                    p.getStatus(),
                    p.getStartTime(),
                    p.getEndTime(),
                    p.getUnitRateAmount() != null ? p.getUnitRateAmount() : BigDecimal.ZERO,
                    p.getUnitRateCurrency() != null ? p.getUnitRateCurrency() : "EUR",
                    p.getTotalEnergyKwh(),
                    p.getTotalAmountAmount() != null ? p.getTotalAmountAmount() : BigDecimal.ZERO,
                    p.getTotalAmountCurrency() != null ? p.getTotalAmountCurrency() : "EUR",
                    p.getErrorCode(),
                    p.getCreatedAt()))
        .toList();
  }
}
