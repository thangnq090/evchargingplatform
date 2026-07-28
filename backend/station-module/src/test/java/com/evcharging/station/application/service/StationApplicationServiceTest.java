package com.evcharging.station.application.service;

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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.shared.pagination.PaginatedList;
import com.evcharging.station.application.dto.ChangeStatusRequest;
import com.evcharging.station.application.dto.CreateStationRequest;
import com.evcharging.station.application.dto.StationResponse;
import com.evcharging.station.application.dto.UpdateStationRequest;
import com.evcharging.station.domain.event.StationCreatedEvent;
import com.evcharging.station.domain.event.StationDeletedEvent;
import com.evcharging.station.domain.event.StationStatusChangedEvent;
import com.evcharging.station.domain.event.StationUpdatedEvent;
import com.evcharging.station.domain.model.Connector;
import com.evcharging.station.domain.model.ConnectorType;
import com.evcharging.station.domain.model.Station;
import com.evcharging.station.domain.model.StationStatus;
import com.evcharging.station.domain.repository.ConnectorRepository;
import com.evcharging.station.domain.repository.StationRepository;
import com.evcharging.station.domain.repository.VendorRepository;
import com.evcharging.station.domain.service.StationDomainService;

@DisplayName("StationApplicationService")
@ExtendWith(MockitoExtension.class)
class StationApplicationServiceTest {

  @Mock private StationDomainService domainService;
  @Mock private StationRepository stationRepository;
  @Mock private ConnectorRepository connectorRepository;
  @Mock private VendorRepository vendorRepository;
  @Mock private ApplicationEventPublisher eventPublisher;

  private StationApplicationService service;

  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final VendorId VENDOR_ID = VendorId.of(VENDOR_UUID);
  private static final StationId STATION_ID = StationId.of(STATION_UUID);
  private static final Location LOCATION = Location.of(52.52, 13.405);

  @BeforeEach
  void setUp() {
    service =
        new StationApplicationService(
            domainService, stationRepository, connectorRepository, vendorRepository, eventPublisher);
  }

  private Station createTestStation() {
    Connector connector = Connector.create(STATION_UUID, ConnectorType.CCS, 150);
    return Station.reconstitute(
        STATION_UUID,
        VENDOR_UUID,
        "Test Station",
        "Downtown",
        350,
        StationStatus.AVAILABLE,
        LOCATION,
        List.of(connector),
        Instant.now(),
        Instant.now(),
        null);
  }

  @Nested
  @DisplayName("createStation")
  class CreateStation {

    @Test
    @DisplayName("creates station and publishes StationCreatedEvent")
    void shouldCreateStation() {
      Station station = createTestStation();
      CreateStationRequest request =
          new CreateStationRequest(
              "Test Station",
              "Downtown",
              350,
              LOCATION,
              List.of(new CreateStationRequest.ConnectorRequest(ConnectorType.CCS, 150)));

      given(domainService.createStation(eq(VENDOR_ID), any(), any(), anyInt(), any(), any()))
          .willReturn(station);
      given(stationRepository.save(any())).willReturn(station);

      StationResponse response = service.createStation(VENDOR_ID, request);

      assertThat(response).isNotNull();
      assertThat(response.name()).isEqualTo("Test Station");
      assertThat(response.status()).isEqualTo("AVAILABLE");

      ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
      then(eventPublisher).should().publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue()).isInstanceOf(StationCreatedEvent.class);
    }
  }

  @Nested
  @DisplayName("getStation")
  class GetStation {

    @Test
    @DisplayName("returns station when found")
    void shouldReturnStation() {
      Station station = createTestStation();
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.of(station));

      StationResponse response = service.getStation(STATION_ID);

      assertThat(response.name()).isEqualTo("Test Station");
      assertThat(response.id()).isEqualTo(STATION_UUID.toString());
    }

    @Test
    @DisplayName("throws when station not found")
    void shouldThrowWhenNotFound() {
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.getStation(STATION_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Station not found");
    }
  }

  @Nested
  @DisplayName("listStations")
  class ListStations {

    @Test
    @DisplayName("returns paginated stations for vendor")
    void shouldListStations() {
      Station station = createTestStation();
      PaginatedList<Station> page = PaginatedList.of(List.of(station), 20, null, false);
      given(stationRepository.findByVendorId(VENDOR_UUID, StationStatus.AVAILABLE, 20, null))
          .willReturn(page);

      PaginatedList<StationResponse> result =
          service.listStations(VENDOR_ID, "AVAILABLE", 20, null);

      assertThat(result.items()).hasSize(1);
      assertThat(result.items().get(0).name()).isEqualTo("Test Station");
    }

    @Test
    @DisplayName("lists all stations when vendorId is null (admin view)")
    void shouldListAllWhenNoVendor() {
      Station station = createTestStation();
      PaginatedList<Station> page = PaginatedList.of(List.of(station), 20, null, false);
      given(stationRepository.findByVendorId(null, null, 20, null)).willReturn(page);

      PaginatedList<StationResponse> result = service.listStations(null, null, 20, null);

      assertThat(result.items()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("updateStation")
  class UpdateStation {

    @Test
    @DisplayName("updates station and publishes StationUpdatedEvent")
    void shouldUpdateStation() {
      Station station = createTestStation();
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.of(station));
      given(stationRepository.save(any())).willReturn(station);

      UpdateStationRequest request = new UpdateStationRequest("New Name", "New Group", 500, null);
      StationResponse response = service.updateStation(STATION_ID, request);

      assertThat(response.name()).isEqualTo("New Name");
      ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
      then(eventPublisher).should().publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue()).isInstanceOf(StationUpdatedEvent.class);
    }

    @Test
    @DisplayName("throws when station not found")
    void shouldThrowWhenNotFound() {
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.empty());

      assertThatThrownBy(
              () -> service.updateStation(STATION_ID, new UpdateStationRequest("N", "G", 100, null)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("changeStatus")
  class ChangeStatus {

    @Test
    @DisplayName("changes status and publishes StationStatusChangedEvent")
    void shouldChangeStatus() {
      Station station = createTestStation();
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.of(station));
      given(stationRepository.save(any())).willReturn(station);

      ChangeStatusRequest request = new ChangeStatusRequest(StationStatus.MAINTENANCE);
      StationResponse response = service.changeStatus(STATION_ID, request);

      assertThat(response.status()).isEqualTo("MAINTENANCE");
      ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
      then(eventPublisher).should().publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue()).isInstanceOf(StationStatusChangedEvent.class);
    }

    @Test
    @DisplayName("throws when station not found")
    void shouldThrowWhenNotFound() {
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  service.changeStatus(
                      STATION_ID, new ChangeStatusRequest(StationStatus.MAINTENANCE)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("deleteStation")
  class DeleteStation {

    @Test
    @DisplayName("soft-deletes station and publishes StationDeletedEvent")
    void shouldDeleteStation() {
      Station station = createTestStation();
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.of(station));

      service.deleteStation(STATION_ID);

      then(stationRepository).should().save(station);
      ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
      then(eventPublisher).should().publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue()).isInstanceOf(StationDeletedEvent.class);
    }

    @Test
    @DisplayName("throws when station not found")
    void shouldThrowWhenNotFound() {
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.deleteStation(STATION_ID))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("findNearby")
  class FindNearby {

    @Test
    @DisplayName("returns nearby stations")
    void shouldFindNearby() {
      Station station = createTestStation();
      given(domainService.findNearby(LOCATION, 10.0)).willReturn(List.of(station));

      List<StationResponse> result = service.findNearby(52.52, 13.405, 10.0);

      assertThat(result).hasSize(1);
      assertThat(result.get(0).name()).isEqualTo("Test Station");
    }

    @Test
    @DisplayName("returns empty list when no stations nearby")
    void shouldReturnEmptyWhenNoneNearby() {
      given(domainService.findNearby(LOCATION, 10.0)).willReturn(List.of());

      List<StationResponse> result = service.findNearby(52.52, 13.405, 10.0);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getStationDetails")
  class GetStationDetails {

    @Test
    @DisplayName("returns station details via StationApi")
    void shouldReturnStationDetails() {
      Station station = createTestStation();
      given(stationRepository.findById(STATION_ID)).willReturn(Optional.of(station));

      var details = service.getStationDetails(STATION_ID);

      assertThat(details.id()).isEqualTo(STATION_UUID);
      assertThat(details.status()).isEqualTo("AVAILABLE");
      assertThat(details.vendorId()).isEqualTo(VENDOR_UUID.toString());
      assertThat(details.connectors()).hasSize(1);
    }
  }
}
