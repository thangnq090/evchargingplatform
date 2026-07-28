package com.evcharging.vehicle.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("VehicleSecurityConfig")
class VehicleSecurityConfigTest {

  @Test
  @DisplayName("can be instantiated")
  void shouldInstantiate() {
    VehicleSecurityConfig config = new VehicleSecurityConfig();
    assertThat(config).isNotNull();
  }
}
