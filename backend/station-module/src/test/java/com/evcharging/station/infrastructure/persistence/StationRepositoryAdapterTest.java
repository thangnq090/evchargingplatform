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

import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.ConnectorType;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;

@DisplayName("StationRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class StationRepositoryAdapterTest {

  @Mock private SpringDataStationRepository jpa;

  private StationRepositoryAdapter adapter;

  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final Location LOCATION = Location.of(52.52, 13.405);

  @BeforeEach
  void setUp() {
    adapter = new StationRepositoryAdapter(jpa);
  }

  private StationJpaEntity createStationEntity() {
    StationJpaEntity entity = new StationJpaEntity();
    entity.setId(STATION_UUID);
    entity.setVendorId(VENDOR_UUID);
    entity.setName("Test Station");
    entity.setUnitPriceTenthCents(350);
    entity.setStatus("AVAILABLE");
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());
    entity.setConnectors(new java.util.ArrayList<>());
    return entity;
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves station entity")
    void shouldSave() {
      Station station = Station.reconstitute(
          STATION_UUID, VENDOR_UUID, "Test", null, 350,
          StationStatus.AVAILABLE, LOCATION, List.of(),
          Instant.now(), Instant.now(), null);

      StationJpaEntity savedEntity = createStationEntity();
      given(jpa.save(any())).willReturn(savedEntity);

      Station result = adapter.save(station);
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(STATION_UUID);
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns station when found")
    void shouldReturnStation() {
      given(jpa.findByIdNotDeleted(STATION_UUID)).willReturn(Optional.of(createStationEntity()));

      Optional<Station> result = adapter.findById(StationId.of(STATION_UUID));
      assertThat(result).isPresent();
      assertThat(result.get().getName()).isEqualTo("Test Station");
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findByIdNotDeleted(STATION_UUID)).willReturn(Optional.empty());

      Optional<Station> result = adapter.findById(StationId.of(STATION_UUID));
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByIdIncludingDeleted")
  class FindByIdIncludingDeleted {

    @Test
    @DisplayName("returns station including deleted")
    void shouldReturnStationIncludingDeleted() {
      given(jpa.findByIdIncludingDeleted(STATION_UUID)).willReturn(Optional.of(createStationEntity()));

      Optional<Station> result = adapter.findByIdIncludingDeleted(StationId.of(STATION_UUID));
      assertThat(result).isPresent();
    }
  }

  @Nested
  @DisplayName("findByVendorId")
  class FindByVendorId {

    @Test
    @DisplayName("returns stations for vendor")
    void shouldReturnStations() {
      given(jpa.findByVendorIdNotDeleted(VENDOR_UUID)).willReturn(List.of(createStationEntity()));

      List<Station> result = adapter.findByVendorId(VENDOR_UUID);
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("existsByVendorIdAndName")
  class ExistsByVendorIdAndName {

    @Test
    @DisplayName("delegates to JPA")
    void shouldDelegateToJpa() {
      given(jpa.existsByVendorIdAndNameNotDeleted(VENDOR_UUID, "Test")).willReturn(true);

      boolean result = adapter.existsByVendorIdAndName(VENDOR_UUID, "Test");
      assertThat(result).isTrue();
    }
  }

  @Nested
  @DisplayName("findByVendorId (paginated)")
  class FindByVendorIdPaginated {

    @Test
    @DisplayName("returns paginated stations for vendor without status filter")
    void shouldReturnPaginatedStations() {
      StationJpaEntity entity = createStationEntity();
      entity.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
      given(jpa.findByVendorIdPaginated(
          eq(VENDOR_UUID), any(), any()))
          .willReturn(List.of(entity));

      com.evcharging.shared.pagination.PaginatedList<Station> result =
          adapter.findByVendorId(VENDOR_UUID, null, 20, null);
      assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("returns paginated stations with status filter")
    void shouldReturnPaginatedStationsWithStatus() {
      StationJpaEntity entity = createStationEntity();
      entity.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
      given(jpa.findByVendorIdAndStatusPaginated(
          eq(VENDOR_UUID), eq("AVAILABLE"), any(), any()))
          .willReturn(List.of(entity));

      com.evcharging.shared.pagination.PaginatedList<Station> result =
          adapter.findByVendorId(VENDOR_UUID, StationStatus.AVAILABLE, 20, null);
      assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("resolves cursor to createdAt")
    void shouldResolveCursor() {
      UUID cursorId = UUID.randomUUID();
      StationJpaEntity cursorEntity = createStationEntity();
      cursorEntity.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
      given(jpa.findByIdIncludingDeleted(cursorId)).willReturn(Optional.of(cursorEntity));
      given(jpa.findByVendorIdPaginated(
          eq(VENDOR_UUID), any(), any()))
          .willReturn(List.of());

      com.evcharging.shared.pagination.PaginatedList<Station> result =
          adapter.findByVendorId(VENDOR_UUID, null, 20, cursorId);
      assertThat(result.items()).isEmpty();
    }

    @Test
    @DisplayName("returns admin paginated (no vendor)")
    void shouldReturnAdminPaginated() {
      given(jpa.findAllPaginated(any(), any()))
          .willReturn(List.of(createStationEntity()));

      com.evcharging.shared.pagination.PaginatedList<Station> result =
          adapter.findByVendorId(null, null, 20, null);
      assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("returns admin paginated with status")
    void shouldReturnAdminPaginatedWithStatus() {
      given(jpa.findAllByStatusPaginated(eq("AVAILABLE"), any(), any()))
          .willReturn(List.of(createStationEntity()));

      com.evcharging.shared.pagination.PaginatedList<Station> result =
          adapter.findByVendorId(null, StationStatus.AVAILABLE, 20, null);
      assertThat(result.items()).hasSize(1);
    }

    @Test
    @DisplayName("sets hasMore when results exceed limit")
    void shouldSetHasMore() {
      StationJpaEntity entity = createStationEntity();
      entity.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z"));
      List<StationJpaEntity> many = java.util.stream.IntStream.range(0, 21)
          .mapToObj(i -> {
            StationJpaEntity e = createStationEntity();
            e.setCreatedAt(Instant.parse("2025-01-01T00:00:00Z").plusSeconds(i));
            return e;
          }).toList();
      given(jpa.findByVendorIdPaginated(eq(VENDOR_UUID), any(), any()))
          .willReturn(many);

      com.evcharging.shared.pagination.PaginatedList<Station> result =
          adapter.findByVendorId(VENDOR_UUID, null, 20, null);
      assertThat(result.pagination().hasMore()).isTrue();
      assertThat(result.items()).hasSize(20);
    }
  }

  @Nested
  @DisplayName("findNearby")
  class FindNearby {

    @Test
    @DisplayName("returns nearby stations")
    void shouldReturnNearbyStations() {
      given(jpa.findNearby(52.52, 13.405, 10000.0))
          .willReturn(List.of(createStationEntity()));

      List<Station> result = adapter.findNearby(LOCATION, 10.0);
      assertThat(result).hasSize(1);
    }
  }
}
