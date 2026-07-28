package com.evcharging.vehicle.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.evcharging.vehicle.domain.model.*;
import com.evcharging.vehicle.infrastructure.persistence.entity.VehicleEntity;

@DisplayName("VehicleRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class VehicleRepositoryAdapterTest {

  @Mock private JpaVehicleRepository jpa;

  private VehicleRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new VehicleRepositoryAdapter(jpa);
  }

  private Vehicle createVehicle() {
    return Vehicle.register(
        RegistrationPlate.of("ABC-123"),
        RfidNumber.of("RFID-001"),
        UUID.randomUUID(),
        java.time.Instant.now());
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves vehicle")
    void shouldSaveVehicle() {
      Vehicle vehicle = createVehicle();
      given(jpa.save(any(VehicleEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Vehicle result = adapter.save(vehicle);

      assertThat(result).isNotNull();
      assertThat(result.getRegistrationPlate()).isEqualTo(vehicle.getRegistrationPlate());
      verify(jpa).save(any(VehicleEntity.class));
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns vehicle when found")
    void shouldReturnVehicle() {
      Vehicle vehicle = createVehicle();
      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);
      given(jpa.findById(vehicle.getId().getValue())).willReturn(Optional.of(entity));

      Optional<Vehicle> result = adapter.findById(vehicle.getId());
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(vehicle.getId());
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findById(any(UUID.class))).willReturn(Optional.empty());
      assertThat(adapter.findById(VehicleId.generate())).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByPlateAndStatus")
  class FindByPlateAndStatus {

    @Test
    @DisplayName("returns vehicle")
    void shouldReturnVehicle() {
      Vehicle vehicle = createVehicle();
      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);
      given(jpa.findByRegistrationPlateAndStatus("ABC-123", "ACTIVE"))
          .willReturn(Optional.of(entity));

      Optional<Vehicle> result =
          adapter.findByPlateAndStatus(RegistrationPlate.of("ABC-123"), VehicleStatus.ACTIVE);
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findByRegistrationPlateAndStatus(any(), any())).willReturn(Optional.empty());
      assertThat(
          adapter.findByPlateAndStatus(
              RegistrationPlate.of("XYZ"), VehicleStatus.ACTIVE))
          .isEmpty();
    }
  }

  @Nested
  @DisplayName("findByRfid")
  class FindByRfid {

    @Test
    @DisplayName("returns vehicle by rfid")
    void shouldFindByRfid() {
      Vehicle vehicle = createVehicle();
      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);
      given(jpa.findByRfidNumber("RFID-001")).willReturn(Optional.of(entity));

      Optional<Vehicle> result = adapter.findByRfid(RfidNumber.of("RFID-001"));
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findByRfidNumber(any())).willReturn(Optional.empty());
      assertThat(adapter.findByRfid(RfidNumber.of("UNKNOWN"))).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByOwnerAndStatus")
  class FindByOwnerAndStatus {

    @Test
    @DisplayName("returns vehicles")
    void shouldReturnVehicles() {
      Vehicle vehicle = createVehicle();
      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);
      UUID ownerId = vehicle.getCurrentOwnerId();

      given(jpa.findByCurrentOwnerIdAndStatus(eq(ownerId), eq("ACTIVE"), any(PageRequest.class)))
          .willReturn(new PageImpl<>(List.of(entity)));

      List<Vehicle> result = adapter.findByOwnerAndStatus(ownerId, VehicleStatus.ACTIVE, 0, 20);
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("findByPlatePrefixAndStatus")
  class FindByPlatePrefixAndStatus {

    @Test
    @DisplayName("returns vehicles")
    void shouldReturnVehicles() {
      Vehicle vehicle = createVehicle();
      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);

      given(jpa.findByPlateLikeAndStatus(eq("ABC"), eq("ACTIVE"), any(PageRequest.class)))
          .willReturn(new PageImpl<>(List.of(entity)));

      List<Vehicle> result =
          adapter.findByPlatePrefixAndStatus(
              RegistrationPlate.of("ABC"), VehicleStatus.ACTIVE, 0, 20);
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("existsByPlateAndStatus")
  class ExistsByPlateAndStatus {

    @Test
    @DisplayName("returns true when exists")
    void shouldReturnTrue() {
      given(jpa.existsByRegistrationPlateAndStatus("ABC-123", "ACTIVE")).willReturn(true);

      assertThat(adapter.existsByPlateAndStatus(
          RegistrationPlate.of("ABC-123"), VehicleStatus.ACTIVE)).isTrue();
    }

    @Test
    @DisplayName("returns false when not exists")
    void shouldReturnFalse() {
      given(jpa.existsByRegistrationPlateAndStatus(any(), any())).willReturn(false);

      assertThat(adapter.existsByPlateAndStatus(
          RegistrationPlate.of("XYZ"), VehicleStatus.ACTIVE)).isFalse();
    }
  }

  @Nested
  @DisplayName("existsByRfid")
  class ExistsByRfid {

    @Test
    @DisplayName("returns true when exists")
    void shouldReturnTrue() {
      given(jpa.existsByRfidNumber("RFID-001")).willReturn(true);
      assertThat(adapter.existsByRfid(RfidNumber.of("RFID-001"))).isTrue();
    }

    @Test
    @DisplayName("returns false when not exists")
    void shouldReturnFalse() {
      given(jpa.existsByRfidNumber(any())).willReturn(false);
      assertThat(adapter.existsByRfid(RfidNumber.of("UNKNOWN"))).isFalse();
    }
  }
}
