package com.evcharging.station.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.Location;

@DisplayName("Location Value Object Tests")
class LocationTest {

  @Nested
  @DisplayName("creation")
  class Creation {

    @Test
    @DisplayName("creates location from valid coordinates")
    void shouldCreateLocation() {
      Location loc = Location.of(52.5200, 13.4050);
      assertThat(loc.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(52.5200));
      assertThat(loc.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(13.4050));
    }

    @Test
    @DisplayName("rejects latitude out of range")
    void shouldRejectInvalidLatitude() {
      assertThatThrownBy(() -> Location.of(-91, 0)).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> Location.of(91, 0)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("rejects longitude out of range")
    void shouldRejectInvalidLongitude() {
      assertThatThrownBy(() -> Location.of(0, -181)).isInstanceOf(IllegalArgumentException.class);
      assertThatThrownBy(() -> Location.of(0, 181)).isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("equality")
  class Equality {

    @Test
    @DisplayName("same coordinates are equal")
    void shouldBeEqual() {
      Location loc1 = Location.of(52.5200, 13.4050);
      Location loc2 = Location.of(52.5200, 13.4050);
      assertThat(loc1).isEqualTo(loc2);
      assertThat(loc1.hashCode()).isEqualTo(loc2.hashCode());
    }

    @Test
    @DisplayName("different coordinates are not equal")
    void shouldNotBeEqual() {
      Location loc1 = Location.of(52.5200, 13.4050);
      Location loc2 = Location.of(48.8566, 2.3522);
      assertThat(loc1).isNotEqualTo(loc2);
    }
  }

  @Nested
  @DisplayName("reconstitution")
  class Reconstitution {

    @Test
    @DisplayName("reconstitutes from persistence without validation")
    void shouldReconstitute() {
      Location loc =
          Location.reconstitute(BigDecimal.valueOf(52.5200), BigDecimal.valueOf(13.4050));
      assertThat(loc.getLatitude()).isEqualByComparingTo(BigDecimal.valueOf(52.5200));
      assertThat(loc.getLongitude()).isEqualByComparingTo(BigDecimal.valueOf(13.4050));
    }
  }
}
