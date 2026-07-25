package com.evcharging.station.domain.service;

import java.util.List;

import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;
import com.evcharging.station.domain.repository.ConnectorRepository;
import com.evcharging.station.domain.repository.StationRepository;
import com.evcharging.station.domain.repository.VendorRepository;

/**
 * Domain service for station operations that involve multiple aggregates or external dependencies.
 *
 * <p>Pure Java — no Spring annotations. Instantiated via configuration.
 */
public class StationDomainService {

  private final StationRepository stationRepository;
  private final ConnectorRepository connectorRepository;
  private final VendorRepository vendorRepository;

  public StationDomainService(
      StationRepository stationRepository,
      ConnectorRepository connectorRepository,
      VendorRepository vendorRepository) {
    this.stationRepository = stationRepository;
    this.connectorRepository = connectorRepository;
    this.vendorRepository = vendorRepository;
  }

  /**
   * Creates a new station with validation.
   *
   * @throws IllegalArgumentException if vendor doesn't exist or station name is duplicate
   */
  public Station createStation(
      VendorId vendorId,
      String name,
      String groupLabel,
      int unitPriceTenthCents,
      Location location,
      List<Connector> connectors) {

    // Validate vendor exists
    if (vendorRepository.findById(vendorId.getValue()).isEmpty()) {
      throw new IllegalArgumentException("Vendor not found: " + vendorId);
    }

    // Validate unique name within vendor scope
    if (stationRepository.existsByVendorIdAndName(vendorId.getValue(), name)) {
      throw new IllegalArgumentException(
          "Station with name '" + name + "' already exists for this vendor");
    }

    return Station.create(
        vendorId.getValue(), name, groupLabel, unitPriceTenthCents, location, connectors);
  }

  /** Updates a station with validation. */
  public Station updateStation(
      StationId stationId, String name, String groupLabel, int unitPriceTenthCents) {

    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

    // Validate unique name if changing
    if (name != null && !name.isBlank() && !name.equals(station.getName())) {
      if (stationRepository.existsByVendorIdAndName(station.getVendorId(), name)) {
        throw new IllegalArgumentException(
            "Station with name '" + name + "' already exists for this vendor");
      }
    }

    station.update(name, groupLabel, unitPriceTenthCents, null);
    return stationRepository.save(station);
  }

  /** Changes a station's availability status. */
  public Station changeStatus(StationId stationId, StationStatus newStatus) {
    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

    station.changeStatus(newStatus);
    return stationRepository.save(station);
  }

  /** Soft-deletes a station. */
  public void deleteStation(StationId stationId) {
    Station station =
        stationRepository
            .findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("Station not found: " + stationId));

    station.delete();
    stationRepository.save(station);
  }

  /** Finds stations near a location. */
  public List<Station> findNearby(Location location, double radiusKm) {
    return stationRepository.findNearby(location, radiusKm);
  }
}
