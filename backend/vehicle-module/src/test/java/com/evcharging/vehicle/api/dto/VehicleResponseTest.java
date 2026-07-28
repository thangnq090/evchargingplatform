package com.evcharging.vehicle.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.vehicle.domain.model.*;

@DisplayName("VehicleResponse")
class VehicleResponseTest {

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("creates response from vehicle with RFID")
    void shouldCreateFromVehicleWithRfid() {
      Vehicle vehicle = Vehicle.register(
          RegistrationPlate.of("ABC-123"),
          RfidNumber.of("RFID-001"),
          UUID.randomUUID(),
          Instant.now());

      VehicleResponse response = VehicleResponse.from(vehicle);

      assertThat(response.id()).isEqualTo(vehicle.getId().getValue());
      assertThat(response.registrationPlate()).isEqualTo("ABC-123");
      assertThat(response.rfidNumber()).isEqualTo("RFID-001");
      assertThat(response.status()).isEqualTo("ACTIVE");
      assertThat(response.ownerId()).isEqualTo(vehicle.getCurrentOwnerId());
      assertThat(response.delistedAt()).isNull();
    }

    @Test
    @DisplayName("creates response from vehicle without RFID")
    void shouldCreateFromVehicleWithoutRfid() {
      Vehicle vehicle = Vehicle.register(
          RegistrationPlate.of("DEF-456"),
          null,
          UUID.randomUUID(),
          Instant.now());

      VehicleResponse response = VehicleResponse.from(vehicle);

      assertThat(response.rfidNumber()).isNull();
    }

    @Test
    @DisplayName("creates response from delisted vehicle")
    void shouldCreateFromDelistedVehicle() {
      Vehicle vehicle = Vehicle.register(
          RegistrationPlate.of("GHI-789"),
          null,
          UUID.randomUUID(),
          Instant.now());
      Instant delistTime = Instant.now();
      vehicle.delist(delistTime);

      VehicleResponse response = VehicleResponse.from(vehicle);

      assertThat(response.status()).isEqualTo("DE_LISTED");
      assertThat(response.delistedAt()).isEqualTo(delistTime);
    }
  }
}
