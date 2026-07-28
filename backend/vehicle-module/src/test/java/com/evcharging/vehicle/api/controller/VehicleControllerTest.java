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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.shared.security.SecurityUtils;
import com.evcharging.vehicle.api.dto.RegisterVehicleRequest;
import com.evcharging.vehicle.api.dto.AssociateRfidRequest;
import com.evcharging.vehicle.application.service.VehicleApplicationService;
import com.evcharging.vehicle.domain.model.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("VehicleController")
@ExtendWith(MockitoExtension.class)
class VehicleControllerTest {

  @Mock private VehicleApplicationService vehicleApplicationService;

  private VehicleController controller;
  private static final UUID CUSTOMER_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller = new VehicleController(vehicleApplicationService);
  }

  private Vehicle createVehicle() {
    return Vehicle.register(
        RegistrationPlate.of("ABC-123"),
        RfidNumber.of("RFID-001"),
        CUSTOMER_UUID,
        Instant.now());
  }

  @Nested
  @DisplayName("registerVehicle")
  class RegisterVehicle {

    @Test
    @DisplayName("registers vehicle successfully")
    void shouldRegisterVehicle() {
      RegisterVehicleRequest request = new RegisterVehicleRequest("ABC-123", "RFID-001");
      Vehicle vehicle = createVehicle();

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(CUSTOMER_UUID));
        given(vehicleApplicationService.registerVehicle(CUSTOMER_UUID, "ABC-123", "RFID-001"))
            .willReturn(vehicle);

        StepVerifier.create(controller.registerVehicle(request))
            .assertNext(response -> {
              assertThat(response.getStatusCode().value()).isEqualTo(201);
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("listMyVehicles")
  class ListMyVehicles {

    @Test
    @DisplayName("lists vehicles")
    void shouldListVehicles() {
      Vehicle vehicle = createVehicle();

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(CUSTOMER_UUID));
        given(vehicleApplicationService.listMyVehicles(CUSTOMER_UUID, 0, 20))
            .willReturn(List.of(vehicle));

        StepVerifier.create(controller.listMyVehicles(0, 20))
            .assertNext(response -> {
              assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            })
            .verifyComplete();
      }
    }

    @Test
    @DisplayName("caps limit at 100")
    void shouldCapLimit() {
      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(CUSTOMER_UUID));
        given(vehicleApplicationService.listMyVehicles(CUSTOMER_UUID, 0, 100))
            .willReturn(List.of());

        StepVerifier.create(controller.listMyVehicles(0, 500))
            .assertNext(response -> {
              assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("getVehicle")
  class GetVehicle {

    @Test
    @DisplayName("returns vehicle when found")
    void shouldReturnVehicle() {
      Vehicle vehicle = createVehicle();
      UUID vehicleId = vehicle.getId().getValue();

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(CUSTOMER_UUID));
        given(vehicleApplicationService.getMyVehicle(vehicleId, CUSTOMER_UUID))
            .willReturn(Optional.of(vehicle));

        StepVerifier.create(controller.getVehicle(vehicleId))
            .assertNext(response -> {
              assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            })
            .verifyComplete();
      }
    }

    @Test
    @DisplayName("returns 404 when not found")
    void shouldReturn404() {
      UUID vehicleId = UUID.randomUUID();

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(CUSTOMER_UUID));
        given(vehicleApplicationService.getMyVehicle(vehicleId, CUSTOMER_UUID))
            .willReturn(Optional.empty());

        StepVerifier.create(controller.getVehicle(vehicleId))
            .assertNext(response -> {
              assertThat(response.getStatusCode().is4xxClientError()).isTrue();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("associateRfid")
  class AssociateRfid {

    @Test
    @DisplayName("associates RFID successfully")
    void shouldAssociateRfid() {
      UUID vehicleId = UUID.randomUUID();
      AssociateRfidRequest request = new AssociateRfidRequest("RFID-NEW");
      Vehicle vehicle = createVehicle();

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(CUSTOMER_UUID));
        given(vehicleApplicationService.associateRfid(vehicleId, CUSTOMER_UUID, "RFID-NEW"))
            .willReturn(vehicle);

        StepVerifier.create(controller.associateRfid(vehicleId, request))
            .assertNext(response -> {
              assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("delistVehicle")
  class DelistVehicle {

    @Test
    @DisplayName("delists vehicle successfully")
    void shouldDelistVehicle() {
      UUID vehicleId = UUID.randomUUID();

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(CUSTOMER_UUID));

        StepVerifier.create(controller.delistVehicle(vehicleId))
            .assertNext(response -> {
              assertThat(response.getStatusCode().value()).isEqualTo(204);
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("lookupByRfid")
  class LookupByRfid {

    @Test
    @DisplayName("returns vehicle when found")
    void shouldReturnVehicle() {
      Vehicle vehicle = createVehicle();

      given(vehicleApplicationService.lookupByRfid("RFID-001"))
          .willReturn(Optional.of(vehicle));

      StepVerifier.create(controller.lookupByRfid("RFID-001"))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("returns 404 when not found")
    void shouldReturn404() {
      given(vehicleApplicationService.lookupByRfid("UNKNOWN"))
          .willReturn(Optional.empty());

      StepVerifier.create(controller.lookupByRfid("UNKNOWN"))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is4xxClientError()).isTrue();
          })
          .verifyComplete();
    }
  }
}
