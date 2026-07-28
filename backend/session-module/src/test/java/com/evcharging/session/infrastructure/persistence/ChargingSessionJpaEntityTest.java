package com.evcharging.session.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
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

@DisplayName("ChargingSessionJpaEntity")
class ChargingSessionJpaEntityTest {

  private static final UUID SESSION_UUID = UUID.randomUUID();
  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final UUID CUSTOMER_UUID = UUID.randomUUID();
  private static final UUID VEHICLE_UUID = UUID.randomUUID();
  private static final Money UNIT_RATE = Money.of(BigDecimal.valueOf(0.35), "EUR");

  private static ChargingSession createSession() {
    return ChargingSession.reconstitute(
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
  }

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("converts domain to JPA entity")
    void shouldConvertToEntity() {
      ChargingSession session = createSession();

      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);

      assertThat(entity.getId()).isEqualTo(SESSION_UUID);
      assertThat(entity.getStationId()).isEqualTo(STATION_UUID);
      assertThat(entity.getConnectorId()).isEqualTo(1);
      assertThat(entity.getCustomerId()).isEqualTo(CUSTOMER_UUID);
      assertThat(entity.getVehicleId()).isEqualTo(VEHICLE_UUID);
      assertThat(entity.getStatus()).isEqualTo("CHARGING");
      assertThat(entity.getUnitRate()).isEqualTo(UNIT_RATE);
      assertThat(entity.getMeterReadings()).isEmpty();
    }

    @Test
    @DisplayName("converts session with meter readings")
    void shouldConvertWithMeterReadings() {
      ChargingSession session = createSession();
      session.recordMeterReading(
          MeterReading.create(
              session.getId(), Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22)));

      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);

      assertThat(entity.getMeterReadings()).hasSize(1);
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("converts JPA entity to domain via from-then-toDomain round-trip")
    void shouldConvertToDomain() {
      ChargingSession session = createSession();
      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);

      ChargingSession domain = entity.toDomain();

      assertThat(domain.getId().getValue()).isEqualTo(SESSION_UUID);
      assertThat(domain.getStationId().getValue()).isEqualTo(STATION_UUID);
      assertThat(domain.getConnectorId()).isEqualTo(1);
      assertThat(domain.getCustomerId().getValue()).isEqualTo(CUSTOMER_UUID);
      assertThat(domain.getVehicleId()).isEqualTo(VEHICLE_UUID);
      assertThat(domain.getStatus()).isEqualTo(SessionStatus.CHARGING);
      assertThat(domain.getTotalEnergyKwh()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("round-trips with meter readings")
    void shouldRoundTripWithMeterReadings() {
      ChargingSession session = createSession();
      session.recordMeterReading(
          MeterReading.create(
              session.getId(), Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22)));

      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);
      ChargingSession domain = entity.toDomain();

      assertThat(domain.getMeterReadings()).hasSize(1);
      assertThat(domain.getMeterReadings().get(0).getEnergyDeliveredKwh())
          .isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("round-trips completed session")
    void shouldRoundTripCompletedSession() {
      ChargingSession session = createSession();
      Instant now = Instant.now();
      session.complete(now, BigDecimal.TEN);

      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);
      ChargingSession domain = entity.toDomain();

      assertThat(domain.getStatus()).isEqualTo(SessionStatus.COMPLETED);
      assertThat(domain.getEndTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("round-trips failed session")
    void shouldRoundTripFailedSession() {
      ChargingSession session = createSession();
      Instant now = Instant.now();
      session.fail(now, "ERR_FAULT", BigDecimal.TEN);

      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);
      ChargingSession domain = entity.toDomain();

      assertThat(domain.getStatus()).isEqualTo(SessionStatus.FAILED);
      assertThat(domain.getErrorCode()).isEqualTo("ERR_FAULT");
    }
  }

  @Nested
  @DisplayName("updateFrom")
  class UpdateFrom {

    @Test
    @DisplayName("updates entity from domain")
    void shouldUpdateFromDomain() {
      ChargingSession session = createSession();
      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);

      session.recordMeterReading(
          MeterReading.create(
              session.getId(), Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22)));
      session.complete(Instant.now(), BigDecimal.TEN);

      entity.updateFrom(session);

      assertThat(entity.getStatus()).isEqualTo("COMPLETED");
      assertThat(entity.getMeterReadings()).hasSize(1);
      assertThat(entity.getTotalEnergyKwh()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("clears meter readings when domain has none")
    void shouldClearMeterReadings() {
      ChargingSession session = createSession();
      session.recordMeterReading(
          MeterReading.create(
              session.getId(), Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22)));
      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);
      assertThat(entity.getMeterReadings()).hasSize(1);

      // Create a new session without meter readings
      ChargingSession session2 = createSession();
      session2.complete(Instant.now(), BigDecimal.ZERO);
      entity.updateFrom(session2);

      assertThat(entity.getMeterReadings()).isEmpty();
    }
  }

  @Nested
  @DisplayName("getters")
  class Getters {

    @Test
    @DisplayName("all getters work")
    void shouldHaveAllGetters() {
      ChargingSession session = createSession();
      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);

      assertThat(entity.getId()).isEqualTo(SESSION_UUID);
      assertThat(entity.getStationId()).isEqualTo(STATION_UUID);
      assertThat(entity.getConnectorId()).isEqualTo(1);
      assertThat(entity.getCustomerId()).isEqualTo(CUSTOMER_UUID);
      assertThat(entity.getVehicleId()).isEqualTo(VEHICLE_UUID);
      assertThat(entity.getStatus()).isEqualTo("CHARGING");
      assertThat(entity.getStartTime()).isNotNull();
      assertThat(entity.getEndTime()).isNull();
      assertThat(entity.getUnitRate()).isEqualTo(UNIT_RATE);
      assertThat(entity.getTotalEnergyKwh()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(entity.getErrorCode()).isNull();
      assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("getVersion returns 0 for new entity")
    void shouldReturnDefaultVersion() {
      ChargingSession session = createSession();
      ChargingSessionJpaEntity entity = ChargingSessionJpaEntity.from(session, true);
      assertThat(entity.getVersion()).isEqualTo(0);
    }
  }
}
