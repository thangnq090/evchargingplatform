package com.evcharging.station.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.ConnectorStatus;
import com.evcharging.station.domain.model.ConnectorType;

@DisplayName("ConnectorJpaEntity")
class ConnectorJpaEntityTest {

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("converts domain connector to JPA entity")
    void shouldConvertToEntity() {
      UUID stationId = UUID.randomUUID();
      Connector connector = Connector.create(stationId, ConnectorType.CCS, 150);

      StationJpaEntity stationEntity = new StationJpaEntity();
      stationEntity.setId(stationId);

      ConnectorJpaEntity entity = ConnectorJpaEntity.from(connector, stationEntity, true);

      assertThat(entity.getStation()).isSameAs(stationEntity);
      assertThat(entity.getType()).isEqualTo("CCS");
      assertThat(entity.getMaxPowerKw()).isEqualTo(150);
      assertThat(entity.getStatus()).isEqualTo("AVAILABLE");
    }

    @Test
    @DisplayName("preserves connector ID when not new")
    void shouldPreserveIdWhenNotNew() {
      UUID connectorId = UUID.randomUUID();
      Connector connector = Connector.reconstitute(
          connectorId, UUID.randomUUID(), ConnectorType.CHADEMO, 50,
          ConnectorStatus.IN_USE, Instant.now());

      StationJpaEntity stationEntity = new StationJpaEntity();
      stationEntity.setId(UUID.randomUUID());

      ConnectorJpaEntity entity = ConnectorJpaEntity.from(connector, stationEntity, false);
      assertThat(entity.getId()).isEqualTo(connectorId);
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("converts JPA entity to domain connector")
    void shouldConvertToDomain() {
      UUID connectorId = UUID.randomUUID();
      UUID stationId = UUID.randomUUID();
      Instant now = Instant.now();

      StationJpaEntity stationEntity = new StationJpaEntity();
      stationEntity.setId(stationId);

      ConnectorJpaEntity entity = new ConnectorJpaEntity(
          connectorId, stationEntity, "TYPE_2", 22, "IN_USE", now);

      Connector connector = entity.toDomain();

      assertThat(connector.getId()).isEqualTo(connectorId);
      assertThat(connector.getStationId()).isEqualTo(stationId);
      assertThat(connector.getType()).isEqualTo(ConnectorType.TYPE_2);
      assertThat(connector.getMaxPowerKw()).isEqualTo(22);
      assertThat(connector.getStatus()).isEqualTo(ConnectorStatus.IN_USE);
    }
  }

  @Nested
  @DisplayName("constructor and getters")
  class ConstructorAndGetters {

    @Test
    @DisplayName("all-args constructor works")
    void shouldWorkWithAllArgsConstructor() {
      StationJpaEntity station = new StationJpaEntity();
      station.setId(UUID.randomUUID());
      Instant now = Instant.now();

      ConnectorJpaEntity entity = new ConnectorJpaEntity(
          UUID.randomUUID(), station, "CCS", 150, "AVAILABLE", now);

      assertThat(entity.getId()).isNotNull();
      assertThat(entity.getStation()).isSameAs(station);
      assertThat(entity.getType()).isEqualTo("CCS");
      assertThat(entity.getMaxPowerKw()).isEqualTo(150);
      assertThat(entity.getStatus()).isEqualTo("AVAILABLE");
      assertThat(entity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("setters work correctly")
    void shouldSetAndGetAllFields() {
      ConnectorJpaEntity entity = new ConnectorJpaEntity();
      UUID id = UUID.randomUUID();
      entity.setId(id);
      entity.setType("CHADEMO");
      entity.setMaxPowerKw(50);
      entity.setStatus("IN_USE");
      entity.setCreatedAt(Instant.now());

      assertThat(entity.getId()).isEqualTo(id);
      assertThat(entity.getType()).isEqualTo("CHADEMO");
      assertThat(entity.getMaxPowerKw()).isEqualTo(50);
      assertThat(entity.getStatus()).isEqualTo("IN_USE");
    }
  }
}
