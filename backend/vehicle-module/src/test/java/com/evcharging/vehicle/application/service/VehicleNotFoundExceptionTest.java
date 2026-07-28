package com.evcharging.vehicle.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VehicleNotFoundException")
class VehicleNotFoundExceptionTest {

  @Test
  @DisplayName("stores vehicleId")
  void shouldStoreVehicleId() {
    UUID vehicleId = UUID.randomUUID();
    VehicleNotFoundException ex = new VehicleNotFoundException(vehicleId);

    assertThat(ex.getVehicleId()).isEqualTo(vehicleId);
    assertThat(ex.getMessage()).contains(vehicleId.toString());
  }
}
