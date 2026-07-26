package com.evcharging.station.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.pagination.PaginatedList;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;

/** Port for Station persistence. */
public interface StationRepository {

  /** Saves a station. */
  Station save(Station station);

  /** Finds a station by ID (non-deleted only). */
  Optional<Station> findById(StationId stationId);

  /** Finds all stations for a vendor (non-deleted only). */
  List<Station> findByVendorId(UUID vendorId);

  /**
   * Cursor-paginated station list. When vendorId is null returns all stations (admin view),
   * otherwise filters by vendor.
   */
  PaginatedList<Station> findByVendorId(
      UUID vendorId, StationStatus status, int limit, UUID cursor);

  /** Finds stations near a location within radius (kilometers). */
  List<Station> findNearby(Location location, double radiusKm);

  /** Checks if a station with the given name exists for the vendor (non-deleted). */
  boolean existsByVendorIdAndName(UUID vendorId, String name);

  /** Finds a station by ID including deleted ones. */
  Optional<Station> findByIdIncludingDeleted(StationId stationId);
}
