package com.evcharging.station.infrastructure.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.pagination.PaginatedList;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;
import com.evcharging.station.domain.repository.StationRepository;

/** Infrastructure adapter implementing the domain StationRepository port. */
@Repository
@Transactional
public class StationRepositoryAdapter implements StationRepository {

  private final SpringDataStationRepository jpa;

  public StationRepositoryAdapter(SpringDataStationRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Station save(Station station) {
    StationJpaEntity entity = StationJpaEntity.from(station, true);
    StationJpaEntity saved = jpa.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<Station> findById(StationId stationId) {
    return jpa.findByIdNotDeleted(stationId.getValue()).map(StationJpaEntity::toDomain);
  }

  @Override
  public Optional<Station> findByIdIncludingDeleted(StationId stationId) {
    return jpa.findByIdIncludingDeleted(stationId.getValue()).map(StationJpaEntity::toDomain);
  }

  @Override
  public List<Station> findByVendorId(UUID vendorId) {
    return jpa.findByVendorIdNotDeleted(vendorId).stream().map(StationJpaEntity::toDomain).toList();
  }

  @Override
  public PaginatedList<Station> findByVendorId(
      UUID vendorId, StationStatus status, int limit, UUID cursor) {
    int clamped = Math.min(Math.max(limit, 1), 100);
    Instant cursorCreatedAt =
        cursor != null
            ? jpa.findByIdIncludingDeleted(cursor).map(StationJpaEntity::getCreatedAt).orElse(null)
            : null;

    List<StationJpaEntity> page;
    if (vendorId != null) {
      page =
          status != null
              ? jpa.findByVendorIdAndStatusPaginated(
                  vendorId, status.name(), cursorCreatedAt, PageRequest.of(0, clamped + 1))
              : jpa.findByVendorIdPaginated(
                  vendorId, cursorCreatedAt, PageRequest.of(0, clamped + 1));
    } else {
      page =
          status != null
              ? jpa.findAllByStatusPaginated(
                  status.name(), cursorCreatedAt, PageRequest.of(0, clamped + 1))
              : jpa.findAllPaginated(cursorCreatedAt, PageRequest.of(0, clamped + 1));
    }

    List<Station> items = page.stream().map(StationJpaEntity::toDomain).toList();
    boolean hasMore = items.size() > clamped;
    List<Station> result = hasMore ? items.subList(0, clamped) : items;
    UUID nextCursor = result.isEmpty() ? null : result.get(result.size() - 1).getId();
    return PaginatedList.of(result, clamped, nextCursor, hasMore);
  }

  @Override
  public List<Station> findNearby(Location location, double radiusKm) {
    double radiusMeters = radiusKm * 1000;
    return jpa
        .findNearby(
            location.getLatitude().doubleValue(),
            location.getLongitude().doubleValue(),
            radiusMeters)
        .stream()
        .map(StationJpaEntity::toDomain)
        .toList();
  }

  @Override
  public boolean existsByVendorIdAndName(UUID vendorId, String name) {
    return jpa.existsByVendorIdAndNameNotDeleted(vendorId, name);
  }
}
