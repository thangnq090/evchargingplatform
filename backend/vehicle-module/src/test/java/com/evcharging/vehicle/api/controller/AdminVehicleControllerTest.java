package com.evcharging.vehicle.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

import com.evcharging.vehicle.api.dto.OwnershipRecordResponse;
import com.evcharging.vehicle.api.dto.VehicleResponse;
import com.evcharging.vehicle.application.service.VehicleApplicationService;
import com.evcharging.vehicle.domain.model.*;

import reactor.test.StepVerifier;

@DisplayName("AdminVehicleController")
@ExtendWith(MockitoExtension.class)
class AdminVehicleControllerTest {

  @Mock private VehicleApplicationService vehicleApplicationService;

  private AdminVehicleController controller;

  @BeforeEach
  void setUp() {
    controller = new AdminVehicleController(vehicleApplicationService);
  }

  private Vehicle createVehicle() {
    return Vehicle.register(
        RegistrationPlate.of("ABC-123"),
        RfidNumber.of("RFID-001"),
        UUID.randomUUID(),
        Instant.now());
  }

  @Nested
  @DisplayName("getVehicle")
  class GetVehicle {

    @Test
    @DisplayName("returns vehicle when found")
    void shouldReturnVehicle() {
      Vehicle vehicle = createVehicle();
      UUID vehicleId = vehicle.getId().getValue();
      given(vehicleApplicationService.adminGetVehicle(vehicleId)).willReturn(Optional.of(vehicle));

      StepVerifier.create(controller.getVehicle(vehicleId))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("returns 404 when not found")
    void shouldReturn404() {
      UUID vehicleId = UUID.randomUUID();
      given(vehicleApplicationService.adminGetVehicle(vehicleId)).willReturn(Optional.empty());

      StepVerifier.create(controller.getVehicle(vehicleId))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("getOwnershipHistory")
  class GetOwnershipHistory {

    @Test
    @DisplayName("returns ownership records")
    void shouldReturnOwnershipRecords() {
      UUID vehicleId = UUID.randomUUID();
      OwnershipRecord record =
          new OwnershipRecord(
              UUID.randomUUID(),
              vehicleId,
              UUID.randomUUID(),
              Instant.now().minusSeconds(86400),
              Instant.now());
      given(vehicleApplicationService.adminGetOwnershipHistory(vehicleId))
          .willReturn(List.of(record));

      StepVerifier.create(controller.getOwnershipHistory(vehicleId))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("returns empty list")
    void shouldReturnEmptyList() {
      UUID vehicleId = UUID.randomUUID();
      given(vehicleApplicationService.adminGetOwnershipHistory(vehicleId)).willReturn(List.of());

      StepVerifier.create(controller.getOwnershipHistory(vehicleId))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }
}
