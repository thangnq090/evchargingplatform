package com.evcharging.session.api.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.session.api.dto.SessionSearchResponse;
import com.evcharging.session.application.service.SessionApplicationService;
import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.SessionStatus;
import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("SessionController (extended)")
@ExtendWith(MockitoExtension.class)
class SessionControllerExtendedTest {

  @Mock private SessionApplicationService service;

  private SessionController controller;

  private static final UUID SESSION_UUID = UUID.randomUUID();
  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final UUID CUSTOMER_UUID = UUID.randomUUID();
  private static final UUID VEHICLE_UUID = UUID.randomUUID();
  private static final Money UNIT_RATE = Money.of(BigDecimal.valueOf(0.35), "EUR");

  private static ChargingSession createSession() {
    return ChargingSession.reconstitute(
        com.evcharging.session.domain.model.SessionId.of(SESSION_UUID),
        StationId.of(STATION_UUID),
        1,
        UserId.of(CUSTOMER_UUID),
        VEHICLE_UUID,
        SessionStatus.CHARGING,
        Instant.now(),
        null,
        UNIT_RATE,
        BigDecimal.ZERO,
        Money.zeroEur(),
        null,
        Instant.now(),
        List.of());
  }

  @BeforeEach
  void setUp() {
    controller = new SessionController(service);
  }

  @Nested
  @DisplayName("startSession")
  class StartSession {

    @Test
    @DisplayName("starts session and returns 201")
    void shouldStartSession() {
      ChargingSession session = createSession();
      given(service.startSession(STATION_UUID, 1, CUSTOMER_UUID, VEHICLE_UUID))
          .willReturn(session);

      var request =
          new com.evcharging.session.api.dto.StartSessionRequest(
              STATION_UUID, 1, CUSTOMER_UUID, VEHICLE_UUID);

      StepVerifier.create(controller.startSession(request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().value()).isEqualTo(201);
            assertThat(res.getHeaders().getLocation()).isNotNull();
            ApiResponse<?> body = res.getBody();
            assertThat(body).isNotNull();
            assertThat(body.success()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("stopSession")
  class StopSession {

    @Test
    @DisplayName("stops session successfully")
    void shouldStopSession() {
      ChargingSession session = createSession();
      session.complete(Instant.now(), BigDecimal.TEN);
      given(service.stopSession(SESSION_UUID, null)).willReturn(session);

      StepVerifier.create(controller.stopSession(SESSION_UUID, null))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("stops session with error code")
    void shouldStopSessionWithError() {
      ChargingSession session = createSession();
      session.fail(Instant.now(), "ERR_FAULT", BigDecimal.TEN);
      var request = new com.evcharging.session.api.dto.StopSessionRequest("ERR_FAULT");
      given(service.stopSession(SESSION_UUID, "ERR_FAULT")).willReturn(session);

      StepVerifier.create(controller.stopSession(SESSION_UUID, request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("recordMeterReading")
  class RecordMeterReading {

    @Test
    @DisplayName("records meter reading")
    void shouldRecordMeterReading() {
      var request =
          new com.evcharging.session.api.dto.MeterReadingRequest(
              Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22));

      StepVerifier.create(controller.recordMeterReading(SESSION_UUID, request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().value()).isEqualTo(202);
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("getCustomerHistory")
  class GetCustomerHistory {

    @Test
    @DisplayName("returns history grouped by month")
    void shouldReturnHistory() {
      ChargingSession session = createSession();
      String month = java.time.YearMonth.now().toString();
      given(service.getCustomerHistory(CUSTOMER_UUID, null))
          .willReturn(Map.of(month, List.of(session)));

      StepVerifier.create(controller.getCustomerHistory(CUSTOMER_UUID, null))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("getVendorReport")
  class GetVendorReport {

    @Test
    @DisplayName("returns vendor report")
    void shouldReturnReport() {
      given(service.getVendorReport(STATION_UUID, "2025-01-01"))
          .willReturn(List.of());

      StepVerifier.create(controller.getVendorReport(STATION_UUID, "2025-01-01"))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            ApiResponse<?> body = res.getBody();
            assertThat(body).isNotNull();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("searchSessions")
  class SearchSessions {

    @Test
    @DisplayName("returns search results")
    void shouldReturnSearchResults() {
      SessionSearchResponse result =
          new SessionSearchResponse(
              SESSION_UUID, STATION_UUID, 1, CUSTOMER_UUID, "CUST-1",
              VEHICLE_UUID, "AUD186", "CHARGING",
              Instant.now(), null,
              UNIT_RATE, BigDecimal.TEN,
              Money.of(BigDecimal.valueOf(3.50), "EUR"),
              null, Instant.now());
      given(service.searchSessions("AUD186")).willReturn(List.of(result));

      StepVerifier.create(controller.searchSessions("AUD186"))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            ApiResponse<?> body = res.getBody();
            assertThat(body).isNotNull();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("returns empty for null query")
    void shouldHandleNullQuery() {
      given(service.searchSessions(null)).willReturn(List.of());

      StepVerifier.create(controller.searchSessions(null))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }
}
