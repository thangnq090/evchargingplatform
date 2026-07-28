package com.evcharging.station.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.shared.kernel.StationId;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.ConnectorType;

@DisplayName("ConnectorRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class ConnectorRepositoryAdapterTest {

  @Mock private SpringDataConnectorRepository jpa;

  private ConnectorRepositoryAdapter adapter;

  private static final UUID CONNECTOR_UUID = UUID.randomUUID();
  private static final UUID STATION_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    adapter = new ConnectorRepositoryAdapter(jpa);
  }

  private ConnectorJpaEntity createConnectorEntity() {
    StationJpaEntity stationEntity = new StationJpaEntity();
    stationEntity.setId(STATION_UUID);

    ConnectorJpaEntity entity = new ConnectorJpaEntity();
    entity.setId(CONNECTOR_UUID);
    entity.setStation(stationEntity);
    entity.setType("CCS");
    entity.setMaxPowerKw(150);
    entity.setStatus("AVAILABLE");
    entity.setCreatedAt(Instant.now());
    return entity;
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("returns connector as-is (cascade save)")
    void shouldReturnConnectorAsIs() {
      Connector connector = Connector.create(STATION_UUID, ConnectorType.CCS, 150);
      Connector result = adapter.save(connector);
      assertThat(result).isSameAs(connector);
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns connector when found")
    void shouldReturnConnector() {
      given(jpa.findById(CONNECTOR_UUID)).willReturn(Optional.of(createConnectorEntity()));

      Optional<Connector> result = adapter.findById(CONNECTOR_UUID);
      assertThat(result).isPresent();
      assertThat(result.get().getType()).isEqualTo(ConnectorType.CCS);
      assertThat(result.get().getMaxPowerKw()).isEqualTo(150);
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findById(CONNECTOR_UUID)).willReturn(Optional.empty());

      assertThat(adapter.findById(CONNECTOR_UUID)).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByStationId")
  class FindByStationId {

    @Test
    @DisplayName("returns connectors for station")
    void shouldReturnConnectors() {
      given(jpa.findByStationId(STATION_UUID)).willReturn(List.of(createConnectorEntity()));

      List<Connector> result = adapter.findByStationId(StationId.of(STATION_UUID));
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(CONNECTOR_UUID);
    }
  }
}
