package com.evcharging.station.infrastructure.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/** Spring Data JPA repository for ConnectorJpaEntity. */
@Repository
public interface SpringDataConnectorRepository extends JpaRepository<ConnectorJpaEntity, UUID> {

  List<ConnectorJpaEntity> findByStationId(UUID stationId);
}
