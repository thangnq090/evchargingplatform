package com.evcharging.session.application.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.identity.VendorMarkupApi;
import com.evcharging.session.domain.event.MeterReadingRecordedEvent;
import com.evcharging.session.application.events.SessionCompletedEvent;
import com.evcharging.session.domain.event.SessionFailedEvent;
import com.evcharging.session.domain.event.SessionStartedEvent;
import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.session.domain.model.MeterReading;
import com.evcharging.session.domain.model.SessionId;
import com.evcharging.session.domain.repository.ChargingSessionRepository;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.Money;
import com.evcharging.shared.kernel.StationId;
import com.evcharging.shared.kernel.UserId;
import com.evcharging.station.StationApi;

import com.evcharging.session.SessionApi;

@Service
@Transactional
public class SessionApplicationService implements SessionApi {

  private final ChargingSessionRepository sessionRepository;
  private final StationApi stationApi;
  private final VendorMarkupApi vendorMarkupApi;
  private final ApplicationEventPublisher eventPublisher;

  public SessionApplicationService(
      ChargingSessionRepository sessionRepository,
      StationApi stationApi,
      VendorMarkupApi vendorMarkupApi,
      ApplicationEventPublisher eventPublisher) {
    this.sessionRepository = sessionRepository;
    this.stationApi = stationApi;
    this.vendorMarkupApi = vendorMarkupApi;
    this.eventPublisher = eventPublisher;
  }

  /** Starts a new charging session. */
  public ChargingSession startSession(
      UUID stationUuid, Integer connectorId, UUID customerUuid, UUID vehicleId) {
    StationId stationId = StationId.of(stationUuid);
    UserId customerId = UserId.of(customerUuid);

    // Retrieve station details (throws IllegalArgumentException if not found)
    StationApi.StationDetails station = stationApi.getStationDetails(stationId);

    // Verify station status
    if (!"AVAILABLE".equals(station.status())) {
      throw new IllegalStateException("Station is not AVAILABLE: status=" + station.status());
    }

    // Verify specific connector status
    StationApi.ConnectorDetails connector =
        station.connectors().stream()
            .filter(
                c ->
                    c.id().equals(connectorId.toString())
                        || c.id().endsWith("-" + connectorId)) // Try matching by number or suffix
            .findFirst()
            .orElse(null);

    // If matching by ID/suffix failed, try matching positionally or just verify by connectorId
    // mapping
    if (connector == null && station.connectors().size() >= connectorId) {
      connector = station.connectors().get(connectorId - 1);
    }

    if (connector == null) {
      throw new IllegalArgumentException("Connector not found: connectorId=" + connectorId);
    }

    if (!"AVAILABLE".equals(connector.status())) {
      throw new IllegalStateException("Connector is not AVAILABLE: status=" + connector.status());
    }

    // Fetch vendor markup percentage
    UUID vendorUuid = UUID.fromString(station.vendorId());
    MarkupPercentage markup = vendorMarkupApi.getMarkup(vendorUuid).orElse(MarkupPercentage.zero());

    // Calculate marked-up rate
    int basePrice = station.unitPriceTenthCents();
    int markedUpPrice = markup.applyTo(basePrice);

    // Convert tenths of cents to Money (assume default EUR)
    BigDecimal amount =
        BigDecimal.valueOf(markedUpPrice).divide(BigDecimal.valueOf(1000), 4, RoundingMode.HALF_UP);
    Money unitRate = Money.of(amount, "EUR");

    // Construct and start session
    ChargingSession session =
        ChargingSession.start(stationId, connectorId, customerId, vehicleId, unitRate);
    ChargingSession savedSession = sessionRepository.save(session);

    // Publish event
    eventPublisher.publishEvent(
        new SessionStartedEvent(
            savedSession.getId(),
            savedSession.getStationId(),
            savedSession.getConnectorId(),
            savedSession.getCustomerId(),
            savedSession.getUnitRate(),
            savedSession.getStartTime()));

    return savedSession;
  }

  /** Records a new meter reading. */
  public void recordMeterReading(
      UUID sessionIdUuid, Instant timestamp, BigDecimal energyDeliveredKwh, BigDecimal powerKw) {
    SessionId sessionId = SessionId.of(sessionIdUuid);
    ChargingSession session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionIdUuid));

    MeterReading reading = MeterReading.create(sessionId, timestamp, energyDeliveredKwh, powerKw);
    session.recordMeterReading(reading);

    sessionRepository.save(session);

    eventPublisher.publishEvent(
        new MeterReadingRecordedEvent(sessionId, timestamp, energyDeliveredKwh, powerKw));
  }

  /** Stops a charging session, completing or failing it. */
  public ChargingSession stopSession(UUID sessionIdUuid, String errorCode) {
    SessionId sessionId = SessionId.of(sessionIdUuid);
    ChargingSession session =
        sessionRepository
            .findById(sessionId)
            .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionIdUuid));

    Instant now = Instant.now();
    BigDecimal finalEnergy = session.getTotalEnergyKwh(); // Use the latest recorded energy reading

    if (errorCode != null && !errorCode.isBlank()) {
      session.fail(now, errorCode, finalEnergy);
      ChargingSession saved = sessionRepository.save(session);
      eventPublisher.publishEvent(new SessionFailedEvent(sessionId, now, finalEnergy, errorCode));
      return saved;
    } else {
      session.complete(now, finalEnergy);
      ChargingSession saved = sessionRepository.save(session);
      eventPublisher.publishEvent(
          new SessionCompletedEvent(
              sessionId.getValue(),
              now,
              finalEnergy,
              saved.getTotalAmount().getAmountExact(),
              saved.getTotalAmount().getCurrency().getCurrencyCode()));
      return saved;
    }
  }

  /** Retrieves session history for a customer, grouped by month. */
  @Transactional(readOnly = true)
  public Map<String, List<ChargingSession>> getCustomerHistory(
      UUID customerUuid, String yearMonthStr) {
    UserId customerId = UserId.of(customerUuid);
    Instant start = Instant.EPOCH;
    Instant end = Instant.now().plus(java.time.Duration.ofDays(365)); // Far future

    if (yearMonthStr != null && !yearMonthStr.isBlank()) {
      YearMonth ym = YearMonth.parse(yearMonthStr);
      start = ym.atDay(1).atStartOfDay(ZoneOffset.UTC).toInstant();
      end = ym.atEndOfMonth().atTime(23, 59, 59, 999999999).toInstant(ZoneOffset.UTC);
    }

    List<ChargingSession> sessions =
        sessionRepository.findByCustomerIdAndStartTimeBetween(customerId, start, end);

    // Group by YYYY-MM
    return sessions.stream()
        .collect(
            Collectors.groupingBy(
                s -> YearMonth.from(s.getStartTime().atZone(ZoneOffset.UTC)).toString(),
                LinkedHashMap::new,
                Collectors.toList()));
  }

  /** Retrieves session report for a vendor station on a specific day. */
  @Transactional(readOnly = true)
  public List<ChargingSession> getVendorReport(UUID stationUuid, String dateStr) {
    StationId stationId = StationId.of(stationUuid);
    LocalDate date = LocalDate.parse(dateStr);
    Instant start = date.atStartOfDay(ZoneOffset.UTC).toInstant();
    Instant end = date.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().minusNanos(1);

    return sessionRepository.findByStationIdAndStartTimeBetween(stationId, start, end);
  }

  @Override
  @Transactional(readOnly = true)
  public java.util.Optional<SessionDetails> getSessionDetails(UUID sessionId) {
    return sessionRepository.findById(SessionId.of(sessionId))
        .map(session -> new SessionDetails(
            session.getId().getValue(),
            session.getStationId().getValue(),
            session.getConnectorId(),
            session.getCustomerId().getValue(),
            session.getVehicleId(),
            session.getStatus().name(),
            session.getStartTime(),
            session.getEndTime(),
            session.getTotalEnergyKwh(),
            session.getUnitRate().getAmount(),
            session.getUnitRate().getCurrency().getCurrencyCode(),
            session.getTotalAmount().getAmount()
        ));
  }
}
