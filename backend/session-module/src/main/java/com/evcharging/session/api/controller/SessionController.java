package com.evcharging.session.api.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evcharging.session.api.dto.MeterReadingRequest;
import com.evcharging.session.api.dto.MonthlyHistoryResponse;
import com.evcharging.session.api.dto.SessionResponse;
import com.evcharging.session.api.dto.StartSessionRequest;
import com.evcharging.session.api.dto.StopSessionRequest;
import com.evcharging.session.application.service.SessionApplicationService;
import com.evcharging.session.domain.model.ChargingSession;
import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.Money;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/sessions")
public class SessionController {

  private final SessionApplicationService service;

  public SessionController(SessionApplicationService service) {
    this.service = service;
  }

  @PostMapping
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<SessionResponse>>> startSession(
      @Valid @RequestBody StartSessionRequest request) {
    return Mono.fromCallable(
            () ->
                service.startSession(
                    request.stationId(),
                    request.connectorId(),
                    request.customerId(),
                    request.vehicleId()))
        .map(
            session ->
                ResponseEntity.created(URI.create("/api/v1/sessions/" + session.getId()))
                    .body(ApiResponse.ok(SessionResponse.from(session))));
  }

  @PostMapping("/{id}/stop")
  @PreAuthorize(
      "hasRole('CUSTOMER') or hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER') or hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<SessionResponse>>> stopSession(
      @PathVariable UUID id, @RequestBody(required = false) StopSessionRequest request) {
    String errorCode = request != null ? request.errorCode() : null;
    return Mono.fromCallable(() -> service.stopSession(id, errorCode))
        .map(session -> ResponseEntity.ok(ApiResponse.ok(SessionResponse.from(session))));
  }

  @PostMapping("/{id}/meter-readings")
  @PreAuthorize("permitAll()") // Permit from system/gateway without individual customer tokens
  public Mono<ResponseEntity<ApiResponse<Void>>> recordMeterReading(
      @PathVariable UUID id, @Valid @RequestBody MeterReadingRequest request) {
    return Mono.fromRunnable(
            () ->
                service.recordMeterReading(
                    id, request.timestamp(), request.energyDeliveredKwh(), request.powerKw()))
        .then(Mono.just(ResponseEntity.accepted().body(ApiResponse.ok(null))));
  }

  @GetMapping("/history")
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<MonthlyHistoryResponse>>> getCustomerHistory(
      @RequestParam UUID customerId, @RequestParam(required = false) String yearMonth) {
    return Mono.fromCallable(
            () -> {
              Map<String, List<ChargingSession>> historyMap =
                  service.getCustomerHistory(customerId, yearMonth);
              List<MonthlyHistoryResponse.MonthGroup> groups = new ArrayList<>();

              for (Map.Entry<String, List<ChargingSession>> entry : historyMap.entrySet()) {
                String month = entry.getKey();
                List<ChargingSession> sessions = entry.getValue();

                int totalSessions = sessions.size();
                BigDecimal totalEnergyKwh =
                    sessions.stream()
                        .map(ChargingSession::getTotalEnergyKwh)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                Currency currency =
                    sessions.isEmpty()
                        ? Currency.getInstance("EUR")
                        : sessions.get(0).getTotalAmount().getCurrency();
                BigDecimal totalAmountVal =
                    sessions.stream()
                        .map(
                            s ->
                                s.getTotalAmount()
                                    .toMonetaryAmount()
                                    .getNumber()
                                    .numberValue(BigDecimal.class))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                Money totalAmount = Money.of(totalAmountVal, currency);

                List<SessionResponse> sessionResponses =
                    sessions.stream().map(SessionResponse::from).toList();

                groups.add(
                    new MonthlyHistoryResponse.MonthGroup(
                        month,
                        new MonthlyHistoryResponse.MonthTotals(
                            totalSessions, totalEnergyKwh, totalAmount),
                        sessionResponses));
              }
              return new MonthlyHistoryResponse(groups);
            })
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }

  @GetMapping("/report")
  @PreAuthorize("hasRole('VENDOR_ADMIN') or hasRole('VENDOR_USER') or hasRole('ADMIN')")
  public Mono<ResponseEntity<ApiResponse<List<SessionResponse>>>> getVendorReport(
      @RequestParam UUID stationId, @RequestParam String date) {
    return Mono.fromCallable(() -> service.getVendorReport(stationId, date))
        .map(sessions -> sessions.stream().map(SessionResponse::from).toList())
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }
}
