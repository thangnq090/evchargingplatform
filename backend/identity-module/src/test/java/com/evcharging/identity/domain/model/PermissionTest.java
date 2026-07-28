package com.evcharging.identity.domain.model;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Permission enum")
class PermissionTest {

  @Test
  @DisplayName("has expected values")
  void shouldHaveExpectedValues() {
    assertThat(Permission.values()).hasSize(13);
    assertThat(Permission.valueOf("STATION_READ")).isEqualTo(Permission.STATION_READ);
    assertThat(Permission.valueOf("BILLING_MANAGE")).isEqualTo(Permission.BILLING_MANAGE);
    assertThat(Permission.valueOf("VENDOR_MANAGE")).isEqualTo(Permission.VENDOR_MANAGE);
  }

  @Test
  @DisplayName("valueOf throws for unknown")
  void shouldThrowForUnknown() {
    assertThatThrownBy(() -> Permission.valueOf("UNKNOWN"))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
