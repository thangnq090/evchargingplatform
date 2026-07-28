package com.evcharging.station.api.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.Location;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.shared.pagination.PaginatedList;
import com.evcharging.shared.security.SecurityUtils;
import com.evcharging.station.application.dto.ChangeStatusRequest;
import com.evcharging.station.application.dto.CreateStationRequest;
import com.evcharging.station.application.dto.StationResponse;
import com.evcharging.station.application.dto.UpdateStationRequest;
import com.evcharging.station.application.service.StationApplicationService;
import com.evcharging.station.domain.model.StationStatus;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("StationController")
@ExtendWith(MockitoExtension.class)
class StationControllerTest {

  @Mock private StationApplicationService service;

  private StationController controller;

  private static final String STATION_ID = UUID.randomUUID().toString();
  private static final String VENDOR_ID = UUID.randomUUID().toString();
  private static final UUID VENDOR_UUID = UUID.fromString(VENDOR_ID);
  private static final Location LOCATION = Location.of(52.52, 13.405);

  private static StationResponse createStationResponse() {
    return new StationResponse(
        STATION_ID,
        VENDOR_ID,
        "Test Station",
        null,
        350,
        "AVAILABLE",
        LOCATION,
        List.of(),
        Instant.now(),
        Instant.now());
  }

  @BeforeEach
  void setUp() {
    controller = new StationController(service);
  }

  @Nested
  @DisplayName("getStation")
  class GetStation {

    @Test
    @DisplayName("returns station by ID")
    void shouldReturnStation() {
      StationResponse response = createStationResponse();
      given(service.getStation(StationId.of(UUID.fromString(STATION_ID)))).willReturn(response);

      StepVerifier.create(controller.getStation(UUID.fromString(STATION_ID)))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            ApiResponse<StationResponse> body = res.getBody();
            assertThat(body).isNotNull();
            assertThat(body.data().id()).isEqualTo(STATION_ID);
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("createStation")
  class CreateStation {

    @Test
    @DisplayName("creates station with vendor context")
    void shouldCreateStation() {
      CreateStationRequest request = new CreateStationRequest(
          "New Station", null, 300, LOCATION, List.of());
      StationResponse response = createStationResponse();
      given(service.createStation(VendorId.of(VENDOR_UUID), request)).willReturn(response);

      try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
        mockedStatic
            .when(SecurityUtils::getReactiveVendorId)
            .thenReturn(Mono.just(VENDOR_UUID));

        StepVerifier.create(controller.createStation(request))
            .assertNext(res -> {
              assertThat(res.getStatusCode().value()).isEqualTo(201);
              assertThat(res.getHeaders().getLocation()).isNotNull();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("listStations")
  class ListStations {

    @Test
    @DisplayName("lists stations for vendor")
    void shouldListStations() {
      PaginatedList<StationResponse> paginated =
          PaginatedList.of(List.of(createStationResponse()), 20, null, false);
      given(service.listStations(VendorId.of(VENDOR_UUID), null, 20, null)).willReturn(paginated);

      try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
        mockedStatic
            .when(SecurityUtils::getReactiveVendorId)
            .thenReturn(Mono.just(VENDOR_UUID));

        StepVerifier.create(controller.listStations(null, 20, null))
            .assertNext(res -> {
              assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
              ApiResponse<PaginatedList<StationResponse>> body = res.getBody();
              assertThat(body).isNotNull();
              assertThat(body.data().items()).hasSize(1);
            })
            .verifyComplete();
      }
    }

    @Test
    @DisplayName("lists stations for admin (no vendor)")
    void shouldListStationsAsAdmin() {
      PaginatedList<StationResponse> paginated =
          PaginatedList.of(List.of(createStationResponse()), 20, null, false);
      given(service.listStations(null, "AVAILABLE", 20, null)).willReturn(paginated);

      try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
        mockedStatic
            .when(SecurityUtils::getReactiveVendorId)
            .thenReturn(Mono.empty());

        StepVerifier.create(controller.listStations("AVAILABLE", 20, null))
            .assertNext(res -> {
              assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("updateStation")
  class UpdateStation {

    @Test
    @DisplayName("updates station")
    void shouldUpdateStation() {
      UpdateStationRequest request = new UpdateStationRequest("Updated", null, null, null);
      StationResponse response = createStationResponse();
      given(service.updateStation(StationId.of(UUID.fromString(STATION_ID)), request))
          .willReturn(response);

      StepVerifier.create(controller.updateStation(UUID.fromString(STATION_ID), request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("changeStatus")
  class ChangeStatus {

    @Test
    @DisplayName("changes station status")
    void shouldChangeStatus() {
      ChangeStatusRequest request = new ChangeStatusRequest(StationStatus.UNAVAILABLE);
      StationResponse response = createStationResponse();
      given(service.changeStatus(StationId.of(UUID.fromString(STATION_ID)), request))
          .willReturn(response);

      StepVerifier.create(controller.changeStatus(UUID.fromString(STATION_ID), request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("deleteStation")
  class DeleteStation {

    @Test
    @DisplayName("deletes station")
    void shouldDeleteStation() {
      doNothing().when(service).deleteStation(StationId.of(UUID.fromString(STATION_ID)));

      StepVerifier.create(controller.deleteStation(UUID.fromString(STATION_ID)))
          .assertNext(res -> {
            assertThat(res.getStatusCode().value()).isEqualTo(204);
            assertThat(res.getBody()).isNull();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("findNearby")
  class FindNearby {

    @Test
    @DisplayName("finds nearby stations")
    void shouldFindNearby() {
      List<StationResponse> stations = List.of(createStationResponse());
      given(service.findNearby(52.52, 13.405, 10.0)).willReturn(stations);

      StepVerifier.create(controller.findNearby(52.52, 13.405, 10.0, "ALL"))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            ApiResponse<List<StationResponse>> body = res.getBody();
            assertThat(body).isNotNull();
            assertThat(body.data()).hasSize(1);
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("filters by status")
    void shouldFilterByStatus() {
      List<StationResponse> stations = List.of(createStationResponse());
      given(service.findNearby(52.52, 13.405, 5.0)).willReturn(stations);

      StepVerifier.create(controller.findNearby(52.52, 13.405, 5.0, "UNAVAILABLE"))
          .assertNext(res -> {
            ApiResponse<List<StationResponse>> body = res.getBody();
            assertThat(body).isNotNull();
            assertThat(body.data()).isEmpty();
          })
          .verifyComplete();
    }
  }
}
