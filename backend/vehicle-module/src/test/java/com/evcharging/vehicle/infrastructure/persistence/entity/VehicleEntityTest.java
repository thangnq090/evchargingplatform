package com.evcharging.vehicle.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.vehicle.domain.model.RfidNumber;
import com.evcharging.vehicle.domain.model.Vehicle;
import com.evcharging.vehicle.domain.model.VehicleId;
import com.evcharging.vehicle.domain.model.VehicleStatus;
import com.evcharging.vehicle.domain.model.RegistrationPlate;

@DisplayName("VehicleEntity")
class VehicleEntityTest {

  private Vehicle createVehicle() {
    return Vehicle.register(
        RegistrationPlate.of("ABC-123"),
        RfidNumber.of("RFID-001"),
        UUID.randomUUID(),
        Instant.now());
  }

  private Vehicle createDelistedVehicle() {
    Vehicle v = Vehicle.register(
        RegistrationPlate.of("DEF-456"),
        null,
        UUID.randomUUID(),
        Instant.now());
    v.delist(Instant.now());
    return v;
  }

  @Nested
  @DisplayName("fromDomain")
  class FromDomain {

    @Test
    @DisplayName("converts active vehicle")
    void shouldConvertActiveVehicle() {
      Vehicle vehicle = createVehicle();

      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);

      assertThat(entity.getId()).isEqualTo(vehicle.getId().getValue());
      assertThat(entity.getRegistrationPlate()).isEqualTo("ABC-123");
      assertThat(entity.getRfidNumber()).isEqualTo("RFID-001");
      assertThat(entity.getCurrentOwnerId()).isEqualTo(vehicle.getCurrentOwnerId());
      assertThat(entity.getStatus()).isEqualTo("ACTIVE");
      assertThat(entity.getDelistedAt()).isNull();
    }

    @Test
    @DisplayName("converts vehicle without RFID")
    void shouldConvertWithoutRfid() {
      Vehicle vehicle = Vehicle.register(
          RegistrationPlate.of("GHI-789"),
          null,
          UUID.randomUUID(),
          Instant.now());

      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);

      assertThat(entity.getRfidNumber()).isNull();
    }

    @Test
    @DisplayName("converts delisted vehicle")
    void shouldConvertDelistedVehicle() {
      Vehicle vehicle = createDelistedVehicle();

      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);

      assertThat(entity.getStatus()).isEqualTo("DE_LISTED");
      assertThat(entity.getDelistedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("round-trips domain to entity and back")
    void shouldRoundTrip() {
      Vehicle vehicle = createVehicle();

      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);
      Vehicle domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(vehicle.getId());
      assertThat(domain.getRegistrationPlate()).isEqualTo(vehicle.getRegistrationPlate());
      assertThat(domain.getRfidNumber()).isEqualTo(vehicle.getRfidNumber());
      assertThat(domain.getCurrentOwnerId()).isEqualTo(vehicle.getCurrentOwnerId());
      assertThat(domain.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
    }

    @Test
    @DisplayName("round-trips delisted vehicle")
    void shouldRoundTripDelisted() {
      Vehicle vehicle = createDelistedVehicle();

      VehicleEntity entity = VehicleEntity.fromDomain(vehicle);
      Vehicle domain = entity.toDomain();

      assertThat(domain.getStatus()).isEqualTo(VehicleStatus.DE_LISTED);
      assertThat(domain.getDelistedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("setters")
  class Setters {

    @Test
    @DisplayName("sets all fields")
    void shouldSetAllFields() {
      VehicleEntity entity = new VehicleEntity();
      UUID id = UUID.randomUUID();
      Instant now = Instant.now();

      entity.setId(id);
      entity.setRegistrationPlate("XYZ-999");
      entity.setRfidNumber("RFID-999");
      entity.setCurrentOwnerId(UUID.randomUUID());
      entity.setStatus("ACTIVE");
      entity.setCreatedAt(now);
      entity.setDelistedAt(now);

      assertThat(entity.getId()).isEqualTo(id);
      assertThat(entity.getRegistrationPlate()).isEqualTo("XYZ-999");
      assertThat(entity.getRfidNumber()).isEqualTo("RFID-999");
      assertThat(entity.getCurrentOwnerId()).isNotNull();
      assertThat(entity.getStatus()).isEqualTo("ACTIVE");
      assertThat(entity.getCreatedAt()).isEqualTo(now);
      assertThat(entity.getDelistedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("version starts at 0")
    void shouldHaveDefaultVersion() {
      VehicleEntity entity = new VehicleEntity();
      assertThat(entity.getVersion()).isEqualTo(0);
    }
  }
}
