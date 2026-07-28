package com.evcharging.session.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.evcharging.identity.VendorMarkupApi;
import com.evcharging.session.SessionApi;
import com.evcharging.session.api.dto.SessionSearchResponse;
import com.evcharging.session.domain.event.MeterReadingRecordedEvent;
import com.evcharging.session.application.events.SessionCompletedEvent;
import com.evcharging.session.domain.event.SessionFailedEvent;
import com.evcharging.session.domain.event.SessionStartedEvent;
import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.SessionId;
import com.evcharging.session.domain.model.SessionStatus;
import com.evcharging.session.domain.repository.ChargingSessionRepository;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;
import com.evcharging.station.StationApi;

@DisplayName("SessionApplicationService")
@ExtendWith(MockitoExtension.class)
class SessionApplicationServiceTest {

  @Mock private ChargingSessionRepository sessionRepository;
  @Mock private StationApi stationApi;
  @Mock private VendorMarkupApi vendorMarkupApi;
  @Mock private ApplicationEventPublisher eventPublisher;

  private SessionApplicationService service;

  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final UUID CUSTOMER_UUID = UUID.randomUUID();
  private static final UUID VEHICLE_UUID = UUID.randomUUID();
  private static final StationId STATION_ID = StationId.of(STATION_UUID);

  @BeforeEach
  void setUp() {
    service =
        new SessionApplicationService(
            sessionRepository, stationApi, vendorMarkupApi, eventPublisher);
  }

  private StationApi.StationDetails availableStation() {
    return new StationApi.StationDetails(
        STATION_UUID, "AVAILABLE", VENDOR_UUID.toString(), 350,
        List.of(new StationApi.ConnectorDetails("c1", "AVAILABLE")));
  }

  @Nested
  @DisplayName("startSession")
  class StartSession {

    @Test
    @DisplayName("starts session successfully")
    void shouldStartSession() {
      StationApi.StationDetails station = availableStation();
      given(stationApi.getStationDetails(STATION_ID)).willReturn(station);
      given(vendorMarkupApi.getMarkup(VENDOR_UUID))
          .willReturn(Optional.of(MarkupPercentage.ofBasisPoints(1500)));
      given(sessionRepository.save(any(ChargingSession.class)))
          .willAnswer(inv -> inv.getArgument(0));

      ChargingSession result =
          service.startSession(STATION_UUID, 1, CUSTOMER_UUID, VEHICLE_UUID);

      assertThat(result).isNotNull();
      assertThat(result.getStatus()).isEqualTo(SessionStatus.CHARGING);
      assertThat(result.getStationId()).isEqualTo(STATION_ID);
      assertThat(result.getConnectorId()).isEqualTo(1);

      then(eventPublisher).should().publishEvent(any(SessionStartedEvent.class));
    }

    @Test
    @DisplayName("throws when station is not AVAILABLE")
    void shouldThrowWhenStationNotAvailable() {
      StationApi.StationDetails station =
          new StationApi.StationDetails(
              STATION_UUID, "OUT_OF_SERVICE", VENDOR_UUID.toString(), 350, List.of());
      given(stationApi.getStationDetails(STATION_ID)).willReturn(station);

      assertThatThrownBy(
              () -> service.startSession(STATION_UUID, 1, CUSTOMER_UUID, VEHICLE_UUID))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("not AVAILABLE");
    }

    @Test
    @DisplayName("throws when connector not found")
    void shouldThrowWhenConnectorNotFound() {
      StationApi.StationDetails station =
          new StationApi.StationDetails(
              STATION_UUID, "AVAILABLE", VENDOR_UUID.toString(), 350, List.of());
      given(stationApi.getStationDetails(STATION_ID)).willReturn(station);

      assertThatThrownBy(
              () -> service.startSession(STATION_UUID, 1, CUSTOMER_UUID, VEHICLE_UUID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Connector not found");
    }

    @Test
    @DisplayName("throws when connector is not AVAILABLE")
    void shouldThrowWhenConnectorNotAvailable() {
      StationApi.StationDetails station =
          new StationApi.StationDetails(
              STATION_UUID, "AVAILABLE", VENDOR_UUID.toString(), 350,
              List.of(new StationApi.ConnectorDetails("c1", "IN_USE")));
      given(stationApi.getStationDetails(STATION_ID)).willReturn(station);

      assertThatThrownBy(
              () -> service.startSession(STATION_UUID, 1, CUSTOMER_UUID, VEHICLE_UUID))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("not AVAILABLE");
    }

    @Test
    @DisplayName("uses zero markup when vendor has no markup")
    void shouldUseZeroMarkupWhenNotFound() {
      StationApi.StationDetails station = availableStation();
      given(stationApi.getStationDetails(STATION_ID)).willReturn(station);
      given(vendorMarkupApi.getMarkup(VENDOR_UUID)).willReturn(Optional.empty());
      given(sessionRepository.save(any(ChargingSession.class)))
          .willAnswer(inv -> inv.getArgument(0));

      ChargingSession result =
          service.startSession(STATION_UUID, 1, CUSTOMER_UUID, VEHICLE_UUID);

      assertThat(result.getUnitRate().getAmount())
          .isEqualByComparingTo(BigDecimal.valueOf(0.3500));
    }

    @Test
    @DisplayName("matches connector by index when ID doesn't match")
    void shouldMatchConnectorByIndex() {
      StationApi.StationDetails station =
          new StationApi.StationDetails(
              STATION_UUID, "AVAILABLE", VENDOR_UUID.toString(), 350,
              List.of(
                  new StationApi.ConnectorDetails("c1", "AVAILABLE"),
                  new StationApi.ConnectorDetails("c2", "AVAILABLE")));
      given(stationApi.getStationDetails(STATION_ID)).willReturn(station);
      given(vendorMarkupApi.getMarkup(VENDOR_UUID)).willReturn(Optional.empty());
      given(sessionRepository.save(any(ChargingSession.class)))
          .willAnswer(inv -> inv.getArgument(0));

      ChargingSession result =
          service.startSession(STATION_UUID, 2, CUSTOMER_UUID, VEHICLE_UUID);

      assertThat(result.getConnectorId()).isEqualTo(2);
    }
  }

  @Nested
  @DisplayName("stopSession")
  class StopSession {

    @Test
    @DisplayName("completes session without error code")
    void shouldCompleteSession() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, 1, UserId.of(CUSTOMER_UUID), VEHICLE_UUID,
              Money.of(BigDecimal.valueOf(0.35), "EUR"));
      SessionId sessionId = session.getId();
      given(sessionRepository.findById(sessionId)).willReturn(Optional.of(session));
      given(sessionRepository.save(any(ChargingSession.class)))
          .willAnswer(inv -> inv.getArgument(0));

      ChargingSession result = service.stopSession(sessionId.getValue(), null);

      assertThat(result.getStatus()).isEqualTo(SessionStatus.COMPLETED);
      then(eventPublisher).should().publishEvent(any(SessionCompletedEvent.class));
    }

    @Test
    @DisplayName("fails session with error code")
    void shouldFailSession() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, 1, UserId.of(CUSTOMER_UUID), VEHICLE_UUID,
              Money.of(BigDecimal.valueOf(0.35), "EUR"));
      SessionId sessionId = session.getId();
      given(sessionRepository.findById(sessionId)).willReturn(Optional.of(session));
      given(sessionRepository.save(any(ChargingSession.class)))
          .willAnswer(inv -> inv.getArgument(0));

      ChargingSession result = service.stopSession(sessionId.getValue(), "ERR_FAULT");

      assertThat(result.getStatus()).isEqualTo(SessionStatus.FAILED);
      assertThat(result.getErrorCode()).isEqualTo("ERR_FAULT");
      then(eventPublisher).should().publishEvent(any(SessionFailedEvent.class));
    }

    @Test
    @DisplayName("throws when session not found")
    void shouldThrowWhenSessionNotFound() {
      UUID unknownId = UUID.randomUUID();
      given(sessionRepository.findById(SessionId.of(unknownId)))
          .willReturn(Optional.empty());

      assertThatThrownBy(() -> service.stopSession(unknownId, null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Session not found");
    }
  }

  @Nested
  @DisplayName("recordMeterReading")
  class RecordMeterReading {

    @Test
    @DisplayName("records meter reading")
    void shouldRecordMeterReading() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, 1, UserId.of(CUSTOMER_UUID), VEHICLE_UUID,
              Money.of(BigDecimal.valueOf(0.35), "EUR"));
      SessionId sessionId = session.getId();
      given(sessionRepository.findById(sessionId)).willReturn(Optional.of(session));
      given(sessionRepository.save(any(ChargingSession.class)))
          .willAnswer(inv -> inv.getArgument(0));

      Instant now = Instant.now();
      service.recordMeterReading(sessionId.getValue(), now, BigDecimal.TEN, BigDecimal.valueOf(22));

      then(eventPublisher).should().publishEvent(any(MeterReadingRecordedEvent.class));
      then(sessionRepository).should().save(any(ChargingSession.class));
    }

    @Test
    @DisplayName("throws when session not found")
    void shouldThrowWhenSessionNotFound() {
      UUID unknownId = UUID.randomUUID();
      given(sessionRepository.findById(SessionId.of(unknownId)))
          .willReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  service.recordMeterReading(
                      unknownId, Instant.now(), BigDecimal.TEN, BigDecimal.valueOf(22)))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("getCustomerHistory")
  class GetCustomerHistory {

    @Test
    @DisplayName("returns grouped history")
    void shouldReturnGroupedHistory() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, 1, UserId.of(CUSTOMER_UUID), VEHICLE_UUID,
              Money.of(BigDecimal.valueOf(0.35), "EUR"));
      given(sessionRepository.findByCustomerIdAndStartTimeBetween(
          eq(UserId.of(CUSTOMER_UUID)), any(), any()))
          .willReturn(List.of(session));

      Map<String, List<ChargingSession>> result =
          service.getCustomerHistory(CUSTOMER_UUID, null);

      assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("filters by yearMonth")
    void shouldFilterByYearMonth() {
      YearMonth now = YearMonth.now();
      String yearMonthStr = now.toString();
      given(sessionRepository.findByCustomerIdAndStartTimeBetween(
          eq(UserId.of(CUSTOMER_UUID)), any(), any()))
          .willReturn(List.of());

      Map<String, List<ChargingSession>> result =
          service.getCustomerHistory(CUSTOMER_UUID, yearMonthStr);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getVendorReport")
  class GetVendorReport {

    @Test
    @DisplayName("returns sessions for station on given date")
    void shouldReturnReport() {
      String dateStr = LocalDate.now().toString();
      given(sessionRepository.findByStationIdAndStartTimeBetween(
          eq(STATION_ID), any(), any()))
          .willReturn(List.of());

      List<ChargingSession> result =
          service.getVendorReport(STATION_UUID, dateStr);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("searchSessions")
  class SearchSessions {

    @Test
    @DisplayName("returns search results")
    void shouldReturnSearchResults() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, 1, UserId.of(CUSTOMER_UUID), VEHICLE_UUID,
              Money.of(BigDecimal.valueOf(0.35), "EUR"));
      UUID sessionId = session.getId().getValue();

      ChargingSessionRepository.SessionSearchResult searchResult =
          new ChargingSessionRepository.SessionSearchResult(
              sessionId, STATION_UUID, 1, CUSTOMER_UUID, "CUST-1",
              VEHICLE_UUID, "AUD186", "CHARGING",
              Instant.now(), null,
              BigDecimal.valueOf(0.35), "EUR",
              BigDecimal.ZERO,
              BigDecimal.ZERO, "EUR",
              null, Instant.now());

      given(sessionRepository.searchSessions("AUD186")).willReturn(List.of(searchResult));

      List<SessionSearchResponse> results = service.searchSessions("AUD186");

      assertThat(results).hasSize(1);
      assertThat(results.get(0).registrationPlate()).isEqualTo("AUD186");
    }

    @Test
    @DisplayName("trims query before search")
    void shouldTrimQuery() {
      given(sessionRepository.searchSessions("AUD")).willReturn(List.of());

      List<SessionSearchResponse> results = service.searchSessions("  AUD  ");

      assertThat(results).isEmpty();
      then(sessionRepository).should().searchSessions("AUD");
    }

    @Test
    @DisplayName("handles null query")
    void shouldHandleNullQuery() {
      given(sessionRepository.searchSessions(null)).willReturn(List.of());

      List<SessionSearchResponse> results = service.searchSessions(null);

      assertThat(results).isEmpty();
    }
  }

  @Nested
  @DisplayName("getSessionDetails")
  class GetSessionDetails {

    @Test
    @DisplayName("returns session details when found")
    void shouldReturnSessionDetails() {
      ChargingSession session =
          ChargingSession.start(STATION_ID, 1, UserId.of(CUSTOMER_UUID), VEHICLE_UUID,
              Money.of(BigDecimal.valueOf(0.35), "EUR"));
      given(sessionRepository.findById(session.getId())).willReturn(Optional.of(session));

      Optional<SessionApi.SessionDetails> result =
          service.getSessionDetails(session.getId().getValue());

      assertThat(result).isPresent();
      assertThat(result.get().stationId()).isEqualTo(STATION_UUID);
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmptyWhenNotFound() {
      UUID unknownId = UUID.randomUUID();
      given(sessionRepository.findById(SessionId.of(unknownId)))
          .willReturn(Optional.empty());

      Optional<SessionApi.SessionDetails> result =
          service.getSessionDetails(unknownId);

      assertThat(result).isEmpty();
    }
  }
}
