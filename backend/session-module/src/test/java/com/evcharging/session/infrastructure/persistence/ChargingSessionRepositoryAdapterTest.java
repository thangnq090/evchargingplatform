package com.evcharging.session.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.SessionId;
import com.evcharging.session.domain.model.SessionStatus;
import com.evcharging.session.domain.repository.ChargingSessionRepository;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;

@DisplayName("ChargingSessionRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class ChargingSessionRepositoryAdapterTest {

  @Mock private SpringDataChargingSessionRepository jpa;

  private ChargingSessionRepositoryAdapter adapter;

  private static final UUID SESSION_UUID = UUID.randomUUID();
  private static final UUID STATION_UUID = UUID.randomUUID();
  private static final UUID CUSTOMER_UUID = UUID.randomUUID();
  private static final UUID VEHICLE_UUID = UUID.randomUUID();
  private static final Money UNIT_RATE = Money.of(BigDecimal.valueOf(0.35), "EUR");

  @BeforeEach
  void setUp() {
    adapter = new ChargingSessionRepositoryAdapter(jpa);
  }

  private ChargingSessionJpaEntity createJpaEntity() {
    ChargingSession session = createDomainSession();
    return ChargingSessionJpaEntity.from(session, true);
  }

  private ChargingSession createDomainSession() {
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
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves new session")
    void shouldSaveNewSession() {
      given(jpa.findById(SESSION_UUID)).willReturn(Optional.empty());
      given(jpa.save(any(ChargingSessionJpaEntity.class)))
          .willAnswer(inv -> inv.getArgument(0));

      ChargingSession session = createDomainSession();
      ChargingSession result = adapter.save(session);

      assertThat(result).isNotNull();
      then(jpa).should().save(any(ChargingSessionJpaEntity.class));
    }

    @Test
    @DisplayName("updates existing session")
    void shouldUpdateExistingSession() {
      ChargingSessionJpaEntity existing = createJpaEntity();
      given(jpa.findById(SESSION_UUID)).willReturn(Optional.of(existing));
      given(jpa.save(any(ChargingSessionJpaEntity.class)))
          .willAnswer(inv -> inv.getArgument(0));

      ChargingSession session = createDomainSession();
      ChargingSession result = adapter.save(session);

      assertThat(result).isNotNull();
      then(jpa).should().save(any(ChargingSessionJpaEntity.class));
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns session when found")
    void shouldReturnSession() {
      given(jpa.findById(SESSION_UUID)).willReturn(Optional.of(createJpaEntity()));

      Optional<ChargingSession> result =
          adapter.findById(SessionId.of(SESSION_UUID));
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findById(SESSION_UUID)).willReturn(Optional.empty());

      Optional<ChargingSession> result =
          adapter.findById(SessionId.of(SESSION_UUID));
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByCustomerIdAndStartTimeBetween")
  class FindByCustomerIdAndStartTimeBetween {

    @Test
    @DisplayName("returns sessions for customer")
    void shouldReturnSessions() {
      given(jpa.findByCustomerIdAndStartTimeBetween(
          eq(CUSTOMER_UUID), any(), any()))
          .willReturn(List.of(createJpaEntity()));

      List<ChargingSession> result =
          adapter.findByCustomerIdAndStartTimeBetween(
              UserId.of(CUSTOMER_UUID), Instant.EPOCH, Instant.now());
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("findByStationIdAndStartTimeBetween")
  class FindByStationIdAndStartTimeBetween {

    @Test
    @DisplayName("returns sessions for station")
    void shouldReturnSessions() {
      given(jpa.findByStationIdAndStartTimeBetween(
          eq(STATION_UUID), any(), any()))
          .willReturn(List.of(createJpaEntity()));

      List<ChargingSession> result =
          adapter.findByStationIdAndStartTimeBetween(
              StationId.of(STATION_UUID), Instant.EPOCH, Instant.now());
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("searchSessions")
  class SearchSessions {

    @Test
    @DisplayName("returns search results")
    void shouldReturnSearchResults() {
      SpringDataChargingSessionRepository.SessionSearchResultProjection projection =
          mock(SpringDataChargingSessionRepository.SessionSearchResultProjection.class);
      given(projection.getId()).willReturn(SESSION_UUID);
      given(projection.getStationId()).willReturn(STATION_UUID);
      given(projection.getConnectorId()).willReturn(1);
      given(projection.getCustomerId()).willReturn(CUSTOMER_UUID);
      given(projection.getCustomerAccountNumber()).willReturn("CUST-1");
      given(projection.getVehicleId()).willReturn(VEHICLE_UUID);
      given(projection.getRegistrationPlate()).willReturn("AUD186");
      given(projection.getStatus()).willReturn("CHARGING");
      given(projection.getStartTime()).willReturn(Instant.now());
      given(projection.getEndTime()).willReturn(null);
      given(projection.getUnitRateAmount()).willReturn(BigDecimal.valueOf(0.35));
      given(projection.getUnitRateCurrency()).willReturn("EUR");
      given(projection.getTotalEnergyKwh()).willReturn(BigDecimal.TEN);
      given(projection.getTotalAmountAmount()).willReturn(BigDecimal.valueOf(3.50));
      given(projection.getTotalAmountCurrency()).willReturn("EUR");
      given(projection.getErrorCode()).willReturn(null);
      given(projection.getCreatedAt()).willReturn(Instant.now());

      given(jpa.searchSessions("AUD186")).willReturn(List.of(projection));

      List<ChargingSessionRepository.SessionSearchResult> results =
          adapter.searchSessions("AUD186");

      assertThat(results).hasSize(1);
      assertThat(results.get(0).registrationPlate()).isEqualTo("AUD186");
    }
  }
}
