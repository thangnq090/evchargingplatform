package com.evcharging.session.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.MeterReading;
import com.evcharging.session.domain.model.SessionId;
import com.evcharging.session.domain.model.SessionStatus;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

@DisplayName("MeterReadingJpaEntity")
class MeterReadingJpaEntityTest {

  private static final UUID SESSION_UUID = UUID.randomUUID();
  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final UUID CUSTOMER_UUID = UUID.randomUUID();
  private static final UUID VEHICLE_UUID = UUID.randomUUID();
  private static final Money UNIT_RATE = Money.of(BigDecimal.valueOf(0.35), "EUR");

  private static ChargingSessionJpaEntity createSessionEntity() {
    ChargingSession session = ChargingSession.reconstitute(
        SessionId.of(SESSION_UUID),
        StationId.of(STATION_UUID),
        1,
        UserId.of(CUSTOMER_UUID),
        VEHICLE_UUID,
        SessionStatus.CHARGING,
        Instant.now(),
        null,
        UNIT_RATE,
        BigDecimal.ZERO,
        Money.zero(UNIT_RATE.getCurrency()),
        null,
        Instant.now(),
        List.of());
    return ChargingSessionJpaEntity.from(session, true);
  }

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("converts domain meter reading to JPA entity")
    void shouldConvertToEntity() {
      Instant timestamp = Instant.now();
      MeterReading reading =
          MeterReading.create(
              SessionId.of(SESSION_UUID), timestamp, BigDecimal.TEN, BigDecimal.valueOf(22));

      ChargingSessionJpaEntity sessionEntity = createSessionEntity();

      MeterReadingJpaEntity entity = MeterReadingJpaEntity.from(reading, sessionEntity, true);

      assertThat(entity.getId()).isEqualTo(reading.getId());
      assertThat(entity.getSession()).isSameAs(sessionEntity);
      assertThat(entity.getTimestamp()).isEqualTo(timestamp);
      assertThat(entity.getEnergyDeliveredKwh()).isEqualByComparingTo(BigDecimal.TEN);
      assertThat(entity.getPowerKw()).isEqualByComparingTo(BigDecimal.valueOf(22));
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("converts JPA entity to domain meter reading via round-trip")
    void shouldConvertToDomain() {
      Instant timestamp = Instant.now();
      MeterReading reading =
          MeterReading.create(
              SessionId.of(SESSION_UUID), timestamp, BigDecimal.TEN, BigDecimal.valueOf(22));

      ChargingSessionJpaEntity sessionEntity = createSessionEntity();
      MeterReadingJpaEntity entity = MeterReadingJpaEntity.from(reading, sessionEntity, false);

      MeterReading domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(reading.getId());
      assertThat(domain.getSessionId().getValue()).isEqualTo(SESSION_UUID);
      assertThat(domain.getTimestamp()).isEqualTo(timestamp);
      assertThat(domain.getEnergyDeliveredKwh()).isEqualByComparingTo(BigDecimal.TEN);
      assertThat(domain.getPowerKw()).isEqualByComparingTo(BigDecimal.valueOf(22));
    }
  }
}
