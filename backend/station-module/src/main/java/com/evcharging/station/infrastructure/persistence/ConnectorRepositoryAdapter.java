package com.evcharging.station.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.shared.kernel.StationId;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.repository.ConnectorRepository;

/** Infrastructure adapter implementing the domain ConnectorRepository port. */
@Repository
@Transactional
public class ConnectorRepositoryAdapter implements ConnectorRepository {

  private final SpringDataConnectorRepository jpa;

  public ConnectorRepositoryAdapter(SpringDataConnectorRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Connector save(Connector connector) {
    // For simplicity, connectors are saved via Station cascade
    // This is a placeholder for when independent connector persistence is needed
    return connector;
  }

  @Override
  public Optional<Connector> findById(UUID id) {
    return jpa.findById(id).map(ConnectorJpaEntity::toDomain);
  }

  @Override
  public List<Connector> findByStationId(StationId stationId) {
    return jpa.findByStationId(stationId.getValue()).stream()
        .map(ConnectorJpaEntity::toDomain)
        .toList();
  }
}
