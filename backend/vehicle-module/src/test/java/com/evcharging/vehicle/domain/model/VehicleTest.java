package com.evcharging.vehicle.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Vehicle aggregate root")
class VehicleTest {

  static final RegistrationPlate PLATE = RegistrationPlate.of("ABC-1234");
  static final RfidNumber RFID = RfidNumber.of("04A3B5C2D1E0");
  static final UUID OWNER_ID = UUID.randomUUID();
  static final Instant NOW = Instant.now();

  private Vehicle activeVehicle;

  @BeforeEach
  void setUp() {
    activeVehicle = Vehicle.register(PLATE, null, OWNER_ID, NOW);
  }

  @Nested
  @DisplayName("register()")
  class Register {

    @Test
    @DisplayName("creates vehicle with ACTIVE status and generated ID")
    void shouldCreateActiveVehicle() {
      assertThat(activeVehicle.getId()).isNotNull();
      assertThat(activeVehicle.getRegistrationPlate()).isEqualTo(PLATE);
      assertThat(activeVehicle.getRfidNumber()).isNull();
      assertThat(activeVehicle.getCurrentOwnerId()).isEqualTo(OWNER_ID);
      assertThat(activeVehicle.getStatus()).isEqualTo(VehicleStatus.ACTIVE);
      assertThat(activeVehicle.getCreatedAt()).isEqualTo(NOW);
      assertThat(activeVehicle.getDelistedAt()).isNull();
    }

    @Test
    @DisplayName("creates vehicle with RFID when provided")
    void shouldCreateVehicleWithRfid() {
      Vehicle v = Vehicle.register(PLATE, RFID, OWNER_ID, NOW);
      assertThat(v.getRfidNumber()).isEqualTo(RFID);
    }

    @Test
    @DisplayName("generates unique ID per registration")
    void shouldGenerateUniqueIds() {
      Vehicle v1 = Vehicle.register(PLATE, null, OWNER_ID, NOW);
      Vehicle v2 = Vehicle.register(PLATE, null, OWNER_ID, NOW);
      assertThat(v1.getId()).isNotEqualTo(v2.getId());
    }

    @Test
    @DisplayName("rejects null owner ID")
    void shouldRejectNullOwnerId() {
      assertThatThrownBy(() -> Vehicle.register(PLATE, null, null, NOW))
          .isInstanceOf(NullPointerException.class)
          .hasMessageContaining("Owner ID");
    }
  }

  @Nested
  @DisplayName("associateRfid()")
  class AssociateRfid {

    @Test
    @DisplayName("associates RFID on an ACTIVE vehicle with no prior RFID")
    void shouldAssociateRfidSuccessfully() {
      activeVehicle.associateRfid(RFID);
      assertThat(activeVehicle.getRfidNumber()).isEqualTo(RFID);
    }

    @Test
    @DisplayName("rejects RFID association on DE_LISTED vehicle")
    void shouldRejectAssociationOnDelistedVehicle() {
      activeVehicle.delist(NOW);
      assertThatThrownBy(() -> activeVehicle.associateRfid(RFID))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("DE_LISTED");
    }

    @Test
    @DisplayName("rejects second RFID when one already associated")
    void shouldRejectDoubleRfidAssociation() {
      activeVehicle.associateRfid(RFID);
      assertThatThrownBy(() -> activeVehicle.associateRfid(RfidNumber.of("ANOTHER-RFID")))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("already has an RFID");
    }

    @Test
    @DisplayName("rejects null RFID")
    void shouldRejectNullRfid() {
      assertThatThrownBy(() -> activeVehicle.associateRfid(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("delist()")
  class Delist {

    @Test
    @DisplayName("transitions ACTIVE vehicle to DE_LISTED and records timestamp")
    void shouldDelistSuccessfully() {
      Instant delistTime = Instant.now();
      activeVehicle.delist(delistTime);

      assertThat(activeVehicle.getStatus()).isEqualTo(VehicleStatus.DE_LISTED);
      assertThat(activeVehicle.getDelistedAt()).isEqualTo(delistTime);
    }

    @Test
    @DisplayName("rejects delisting an already DE_LISTED vehicle")
    void shouldRejectDoubleDelisting() {
      activeVehicle.delist(NOW);
      assertThatThrownBy(() -> activeVehicle.delist(Instant.now()))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("DE_LISTED");
    }

    @Test
    @DisplayName("rejects null delist timestamp")
    void shouldRejectNullTimestamp() {
      assertThatThrownBy(() -> activeVehicle.delist(null)).isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("equality")
  class Equality {

    @Test
    @DisplayName("equal when same VehicleId")
    void shouldBeEqualById() {
      VehicleId id = VehicleId.generate();
      Vehicle a = new Vehicle(id, PLATE, null, OWNER_ID, VehicleStatus.ACTIVE, NOW, null);
      Vehicle b = new Vehicle(id, PLATE, null, OWNER_ID, VehicleStatus.ACTIVE, NOW, null);
      assertThat(a).isEqualTo(b);
      assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("not equal when different VehicleId")
    void shouldNotBeEqualForDifferentIds() {
      Vehicle a = Vehicle.register(PLATE, null, OWNER_ID, NOW);
      Vehicle b = Vehicle.register(PLATE, null, OWNER_ID, NOW);
      assertThat(a).isNotEqualTo(b);
    }
  }
}
