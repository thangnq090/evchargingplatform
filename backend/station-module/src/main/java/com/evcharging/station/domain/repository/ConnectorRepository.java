package com.evcharging.station.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.shared.kernel.StationId;
import com.evcharging.station.domain.model.Connector;

/** Port for Connector persistence. */
public interface ConnectorRepository {

  /** Saves a connector. */
  Connector save(Connector connector);

  /** Finds a connector by ID. */
  Optional<Connector> findById(UUID id);

  /** Finds all connectors for a station. */
  List<Connector> findByStationId(StationId stationId);
}
