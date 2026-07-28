package com.evcharging.station.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.PrecisionModel;

import com.evcharging.shared.kernel.Location;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.ConnectorType;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;

@DisplayName("StationJpaEntity")
class StationJpaEntityTest {

  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final Location LOCATION = Location.of(52.52, 13.405);
  private static final GeometryFactory GF = new GeometryFactory(new PrecisionModel(), 4326);

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("converts domain to JPA entity")
    void shouldConvertToEntity() {
      Connector connector = Connector.create(STATION_UUID, ConnectorType.CCS, 150);
      Station station = Station.reconstitute(
          STATION_UUID, VENDOR_UUID, "Test", "Group", 350,
          StationStatus.AVAILABLE, LOCATION, List.of(connector),
          Instant.now(), Instant.now(), null);

      StationJpaEntity entity = StationJpaEntity.from(station, true);

      assertThat(entity.getId()).isEqualTo(STATION_UUID);
      assertThat(entity.getVendorId()).isEqualTo(VENDOR_UUID);
      assertThat(entity.getName()).isEqualTo("Test");
      assertThat(entity.getGroupLabel()).isEqualTo("Group");
      assertThat(entity.getUnitPriceTenthCents()).isEqualTo(350);
      assertThat(entity.getStatus()).isEqualTo("AVAILABLE");
      assertThat(entity.getConnectors()).hasSize(1);
      assertThat(entity.getDeletedAt()).isNull();
    }

    @Test
    @DisplayName("preserves deletedAt from domain")
    void shouldPreserveDeletedAt() {
      Instant deletedAt = Instant.now();
      Connector connector = Connector.create(STATION_UUID, ConnectorType.CCS, 150);
      Station station = Station.reconstitute(
          STATION_UUID, VENDOR_UUID, "Test", null, 350,
          StationStatus.UNAVAILABLE, LOCATION, List.of(connector),
          Instant.now(), Instant.now(), deletedAt);

      StationJpaEntity entity = StationJpaEntity.from(station, false);
      assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
    }

  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("converts JPA entity to domain")
    void shouldConvertToDomain() {
      StationJpaEntity stationEntity = new StationJpaEntity();
      stationEntity.setId(STATION_UUID);

      ConnectorJpaEntity connectorEntity = new ConnectorJpaEntity();
      connectorEntity.setId(UUID.randomUUID());
      connectorEntity.setStation(stationEntity);
      connectorEntity.setType("CCS");
      connectorEntity.setMaxPowerKw(150);
      connectorEntity.setStatus("AVAILABLE");
      connectorEntity.setCreatedAt(Instant.now());

      StationJpaEntity entity = new StationJpaEntity();
      entity.setId(STATION_UUID);
      entity.setVendorId(VENDOR_UUID);
      entity.setName("Test Station");
      entity.setGroupLabel("Downtown");
      entity.setUnitPriceTenthCents(350);
      entity.setStatus("AVAILABLE");
      entity.setLocation(GF.createPoint(new Coordinate(13.405, 52.52)));
      entity.setCreatedAt(Instant.now());
      entity.setUpdatedAt(Instant.now());
      entity.setDeletedAt(null);
      entity.setConnectors(new java.util.ArrayList<>(List.of(connectorEntity)));

      Station station = entity.toDomain();

      assertThat(station.getId()).isEqualTo(STATION_UUID);
      assertThat(station.getName()).isEqualTo("Test Station");
      assertThat(station.getStatus()).isEqualTo(StationStatus.AVAILABLE);
      assertThat(station.getConnectors()).hasSize(1);
      assertThat(station.getConnectors().get(0).getType()).isEqualTo(ConnectorType.CCS);
    }
  }

  @Nested
  @DisplayName("getters and setters")
  class GettersSetters {

    @Test
    @DisplayName("all getters and setters work")
    void shouldSetAndGetAllFields() {
      StationJpaEntity entity = new StationJpaEntity();
      UUID id = UUID.randomUUID();
      entity.setId(id);
      entity.setVendorId(UUID.randomUUID());
      entity.setName("Station");
      entity.setGroupLabel("Group");
      entity.setUnitPriceTenthCents(100);
      entity.setStatus("MAINTENANCE");
      entity.setCreatedAt(Instant.now());
      entity.setUpdatedAt(Instant.now());

      assertThat(entity.getId()).isEqualTo(id);
      assertThat(entity.getName()).isEqualTo("Station");
      assertThat(entity.getUnitPriceTenthCents()).isEqualTo(100);
      assertThat(entity.getStatus()).isEqualTo("MAINTENANCE");
    }

    @Test
    @DisplayName("setDeletedAt and getDeletedAt work")
    void shouldSetDeletedAt() {
      StationJpaEntity entity = new StationJpaEntity();
      Instant deletedAt = Instant.now();
      entity.setDeletedAt(deletedAt);
      assertThat(entity.getDeletedAt()).isEqualTo(deletedAt);
    }

    @Test
    @DisplayName("setLocation and getLocation work")
    void shouldSetLocation() {
      StationJpaEntity entity = new StationJpaEntity();
      var point = GF.createPoint(new Coordinate(13.405, 52.52));
      entity.setLocation(point);
      assertThat(entity.getLocation()).isEqualTo(point);
    }

    @Test
    @DisplayName("setConnectors and getConnectors work")
    void shouldSetConnectors() {
      StationJpaEntity entity = new StationJpaEntity();
      List<ConnectorJpaEntity> connectors = List.of(new ConnectorJpaEntity());
      entity.setConnectors(connectors);
      assertThat(entity.getConnectors()).hasSize(1);
    }
  }
}
