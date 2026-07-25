package com.evcharging.session.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

@DisplayName("ChargingSession Domain Tests")
class ChargingSessionTest {

  private static final StationId STATION_ID = StationId.generate();
  private static final Integer CONNECTOR_ID = 1;
  private static final UserId CUSTOMER_ID = UserId.generate();
  private static final UUID VEHICLE_ID = UUID.randomUUID();
  private static final Money UNIT_RATE = Money.of(BigDecimal.valueOf(0.35), "EUR");

  @Nested
  @DisplayName("start")
  class Start {

    @Test
    @DisplayName("starts session in CHARGING status with zero total energy and amount")
    void shouldStartSession() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, CONNECTOR_ID, CUSTOMER_ID, VEHICLE_ID, UNIT_RATE);

      assertThat(session.getId()).isNotNull();
      assertThat(session.getStationId()).isEqualTo(STATION_ID);
      assertThat(session.getConnectorId()).isEqualTo(CONNECTOR_ID);
      assertThat(session.getCustomerId()).isEqualTo(CUSTOMER_ID);
      assertThat(session.getVehicleId()).isEqualTo(VEHICLE_ID);
      assertThat(session.getStatus()).isEqualTo(SessionStatus.CHARGING);
      assertThat(session.getStartTime()).isNotNull();
      assertThat(session.getEndTime()).isNull();
      assertThat(session.getUnitRate()).isEqualTo(UNIT_RATE);
      assertThat(session.getTotalEnergyKwh()).isZero();
      assertThat(session.getTotalAmount()).isEqualTo(Money.zero(UNIT_RATE.getCurrency()));
      assertThat(session.getErrorCode()).isNull();
      assertThat(session.getCreatedAt()).isNotNull();
      assertThat(session.getMeterReadings()).isEmpty();
    }
  }

  @Nested
  @DisplayName("recordMeterReading")
  class RecordMeterReading {

    @Test
    @DisplayName("appends meter reading and recalculates total energy and amount")
    void shouldRecordMeterReading() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, CONNECTOR_ID, CUSTOMER_ID, VEHICLE_ID, UNIT_RATE);
      Instant timestamp = Instant.now();
      BigDecimal energy = BigDecimal.valueOf(10.5);
      BigDecimal power = BigDecimal.valueOf(22.0);
      MeterReading reading = MeterReading.create(session.getId(), timestamp, energy, power);

      session.recordMeterReading(reading);

      assertThat(session.getMeterReadings()).hasSize(1);
      assertThat(session.getTotalEnergyKwh()).isEqualTo(energy);
      assertThat(session.getTotalAmount()).isEqualTo(UNIT_RATE.multiply(energy));
    }

    @Test
    @DisplayName("rejects reading with energy less than previous reading")
    void shouldRejectDecreasingEnergy() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, CONNECTOR_ID, CUSTOMER_ID, VEHICLE_ID, UNIT_RATE);
      Instant now = Instant.now();
      session.recordMeterReading(
          MeterReading.create(
              session.getId(), now, BigDecimal.valueOf(10.5), BigDecimal.valueOf(22.0)));

      assertThatThrownBy(
              () ->
                  session.recordMeterReading(
                      MeterReading.create(
                          session.getId(),
                          now.plusSeconds(10),
                          BigDecimal.valueOf(9.5),
                          BigDecimal.valueOf(22.0))))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("complete")
  class Complete {

    @Test
    @DisplayName("completes session and updates status and amount")
    void shouldCompleteSession() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, CONNECTOR_ID, CUSTOMER_ID, VEHICLE_ID, UNIT_RATE);
      Instant now = Instant.now();
      BigDecimal finalEnergy = BigDecimal.valueOf(20.0);

      session.complete(now, finalEnergy);

      assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
      assertThat(session.getEndTime()).isEqualTo(now);
      assertThat(session.getTotalEnergyKwh()).isEqualTo(finalEnergy);
      assertThat(session.getTotalAmount()).isEqualTo(UNIT_RATE.multiply(finalEnergy));
    }
  }

  @Nested
  @DisplayName("fail")
  class Fail {

    @Test
    @DisplayName("marks session as FAILED with error code")
    void shouldFailSession() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, CONNECTOR_ID, CUSTOMER_ID, VEHICLE_ID, UNIT_RATE);
      Instant now = Instant.now();
      BigDecimal finalEnergy = BigDecimal.valueOf(5.0);

      session.fail(now, "ERR_CHARGER_FAULT", finalEnergy);

      assertThat(session.getStatus()).isEqualTo(SessionStatus.FAILED);
      assertThat(session.getEndTime()).isEqualTo(now);
      assertThat(session.getErrorCode()).isEqualTo("ERR_CHARGER_FAULT");
      assertThat(session.getTotalEnergyKwh()).isEqualTo(finalEnergy);
      assertThat(session.getTotalAmount()).isEqualTo(UNIT_RATE.multiply(finalEnergy));
    }
  }
}
