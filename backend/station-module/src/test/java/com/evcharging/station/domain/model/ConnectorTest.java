package com.evcharging.station.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Connector Domain Tests")
class ConnectorTest {

  private static final UUID STATION_ID = UUID.randomUUID();

  @Nested
  @DisplayName("create")
  class Create {

    @Test
    @DisplayName("creates connector with AVAILABLE status")
    void shouldCreateConnector() {
      Connector connector = Connector.create(STATION_ID, ConnectorType.CCS, 150);

      assertThat(connector.getId()).isNotNull();
      assertThat(connector.getStationId()).isEqualTo(STATION_ID);
      assertThat(connector.getType()).isEqualTo(ConnectorType.CCS);
      assertThat(connector.getMaxPowerKw()).isEqualTo(150);
      assertThat(connector.getStatus()).isEqualTo(ConnectorStatus.AVAILABLE);
      assertThat(connector.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("rejects power outside valid range")
    void shouldRejectInvalidPower() {
      assertThatThrownBy(() -> Connector.create(STATION_ID, ConnectorType.CCS, 0))
          .isInstanceOf(IllegalArgumentException.class);

      assertThatThrownBy(() -> Connector.create(STATION_ID, ConnectorType.CCS, 501))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects null type")
    void shouldRejectNullType() {
      assertThatThrownBy(() -> Connector.create(STATION_ID, null, 150))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("status transitions")
  class StatusTransitions {

    @Test
    @DisplayName("marks connector in use")
    void shouldMarkInUse() {
      Connector connector = Connector.create(STATION_ID, ConnectorType.CCS, 150);
      connector.markInUse();
      assertThat(connector.getStatus()).isEqualTo(ConnectorStatus.IN_USE);
      assertThat(connector.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("only AVAILABLE connectors can be marked in use")
    void shouldRejectMarkInUseWhenNotAvailable() {
      Connector connector = Connector.create(STATION_ID, ConnectorType.CCS, 150);
      connector.markInUse();

      assertThatThrownBy(connector::markInUse).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("marks unavailable")
    void shouldMarkUnavailable() {
      Connector connector = Connector.create(STATION_ID, ConnectorType.CCS, 150);
      connector.markUnavailable();
      assertThat(connector.getStatus()).isEqualTo(ConnectorStatus.UNAVAILABLE);
    }

    @Test
    @DisplayName("marks available")
    void shouldMarkAvailable() {
      Connector connector = Connector.create(STATION_ID, ConnectorType.CCS, 150);
      connector.markInUse();
      connector.markAvailable();
      assertThat(connector.getStatus()).isEqualTo(ConnectorStatus.AVAILABLE);
    }
  }
}
