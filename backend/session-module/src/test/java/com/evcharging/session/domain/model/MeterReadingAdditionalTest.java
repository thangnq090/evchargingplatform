package com.evcharging.session.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("MeterReading (additional)")
class MeterReadingAdditionalTest {

  private static final SessionId SESSION_ID = SessionId.generate();

  @Nested
  @DisplayName("reconstitute")
  class Reconstitute {

    @Test
    @DisplayName("reconstitutes from persistence")
    void shouldReconstitute() {
      UUID id = UUID.randomUUID();
      Instant timestamp = Instant.now();
      MeterReading reading =
          MeterReading.reconstitute(id, SESSION_ID, timestamp, BigDecimal.TEN, BigDecimal.valueOf(22));

      assertThat(reading.getId()).isEqualTo(id);
      assertThat(reading.getSessionId()).isEqualTo(SESSION_ID);
      assertThat(reading.getTimestamp()).isEqualTo(timestamp);
      assertThat(reading.getEnergyDeliveredKwh()).isEqualByComparingTo(BigDecimal.TEN);
      assertThat(reading.getPowerKw()).isEqualByComparingTo(BigDecimal.valueOf(22));
    }
  }

  @Nested
  @DisplayName("equals and hashCode")
  class Equality {

    @Test
    @DisplayName("equal when same ID")
    void shouldBeEqual() {
      UUID id = UUID.randomUUID();
      MeterReading r1 = MeterReading.reconstitute(
          id, SESSION_ID, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));
      MeterReading r2 = MeterReading.reconstitute(
          id, SESSION_ID, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));
      assertThat(r1).isEqualTo(r2);
      assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
    }

    @Test
    @DisplayName("not equal when different ID")
    void shouldNotBeEqual() {
      MeterReading r1 = MeterReading.create(SESSION_ID, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));
      MeterReading r2 = MeterReading.create(SESSION_ID, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));
      assertThat(r1).isNotEqualTo(r2);
    }

    @Test
    @DisplayName("not equal to null")
    void shouldNotBeEqualToNull() {
      MeterReading r = MeterReading.create(SESSION_ID, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));
      assertThat(r).isNotEqualTo(null);
    }

    @Test
    @DisplayName("not equal to other type")
    void shouldNotBeEqualToOtherType() {
      MeterReading r = MeterReading.create(SESSION_ID, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));
      assertThat(r).isNotEqualTo("string");
    }

    @Test
    @DisplayName("equal to itself")
    void shouldBeEqualToSelf() {
      MeterReading r = MeterReading.create(SESSION_ID, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));
      assertThat(r).isEqualTo(r);
    }
  }
}
