package com.evcharging.vehicle.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("RegistrationPlate value object")
class RegistrationPlateTest {

  @Nested
  @DisplayName("of()")
  class Of {

    @Test
    @DisplayName("normalises raw input to uppercase and trimmed")
    void shouldNormaliseInput() {
      RegistrationPlate plate = RegistrationPlate.of("  abc-1234  ");
      assertThat(plate.getValue()).isEqualTo("ABC-1234");
    }

    @Test
    @DisplayName("accepts valid alphanumeric plates")
    void shouldAcceptValidPlate() {
      assertThat(RegistrationPlate.of("ABC123").getValue()).isEqualTo("ABC123");
      assertThat(RegistrationPlate.of("AB-CD-12").getValue()).isEqualTo("AB-CD-12");
      assertThat(RegistrationPlate.of("A").getValue()).isEqualTo("A");
    }

    @Test
    @DisplayName("rejects null input")
    void shouldRejectNull() {
      assertThatThrownBy(() -> RegistrationPlate.of(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("rejects blank input")
    void shouldRejectBlank() {
      assertThatThrownBy(() -> RegistrationPlate.of("   "))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("rejects plate with special characters")
    void shouldRejectInvalidChars() {
      assertThatThrownBy(() -> RegistrationPlate.of("ABC!@#"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid registration plate format");
    }

    @Test
    @DisplayName("rejects plate exceeding 20 characters")
    void shouldRejectTooLong() {
      assertThatThrownBy(() -> RegistrationPlate.of("ABCDEFGHIJ12345678901")) // 21 chars
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid registration plate format");
    }
  }

  @Nested
  @DisplayName("equality")
  class Equality {

    @Test
    @DisplayName("equal when same normalised value")
    void shouldBeEqualForSameValue() {
      RegistrationPlate a = RegistrationPlate.of("ABC-123");
      RegistrationPlate b = RegistrationPlate.of("abc-123");
      assertThat(a).isEqualTo(b);
      assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("not equal for different plates")
    void shouldNotBeEqualForDifferentValues() {
      assertThat(RegistrationPlate.of("ABC-123")).isNotEqualTo(RegistrationPlate.of("XYZ-456"));
    }
  }
}
