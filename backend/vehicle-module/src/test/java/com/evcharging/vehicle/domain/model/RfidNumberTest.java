package com.evcharging.vehicle.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RfidNumber value object")
class RfidNumberTest {

  @Nested
  @DisplayName("of()")
  class Of {

    @Test
    @DisplayName("accepts valid RFID and trims whitespace")
    void shouldAcceptAndTrim() {
      RfidNumber rfid = RfidNumber.of("  04A3B5C2D1E0  ");
      assertThat(rfid.getValue()).isEqualTo("04A3B5C2D1E0");
    }

    @Test
    @DisplayName("rejects null input")
    void shouldRejectNull() {
      assertThatThrownBy(() -> RfidNumber.of(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("rejects blank input")
    void shouldRejectBlank() {
      assertThatThrownBy(() -> RfidNumber.of("   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("rejects RFID exceeding 50 characters")
    void shouldRejectTooLong() {
      String tooLong = "A".repeat(51);
      assertThatThrownBy(() -> RfidNumber.of(tooLong))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("max length of 50");
    }

    @Test
    @DisplayName("accepts exactly 50 characters")
    void shouldAcceptExactlyMaxLength() {
      String exactly50 = "A".repeat(50);
      assertThat(RfidNumber.of(exactly50).getValue()).hasSize(50);
    }
  }

  @Nested
  @DisplayName("equality — case-insensitive")
  class Equality {

    @Test
    @DisplayName("equal when same value case-insensitively")
    void shouldBeEqualCaseInsensitive() {
      RfidNumber lower = RfidNumber.of("04a3b5c2d1e0");
      RfidNumber upper = RfidNumber.of("04A3B5C2D1E0");
      assertThat(lower).isEqualTo(upper);
      assertThat(lower.hashCode()).isEqualTo(upper.hashCode());
    }

    @Test
    @DisplayName("not equal for different RFID values")
    void shouldNotBeEqualForDifferentValues() {
      assertThat(RfidNumber.of("RFID-001")).isNotEqualTo(RfidNumber.of("RFID-002"));
    }
  }
}
