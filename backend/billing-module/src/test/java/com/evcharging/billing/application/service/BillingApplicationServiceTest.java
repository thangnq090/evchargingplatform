package com.evcharging.billing.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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

import com.evcharging.billing.domain.event.InvoiceGeneratedEvent;
import com.evcharging.billing.domain.model.BillingAccount;
import com.evcharging.billing.domain.model.Invoice;
import com.evcharging.billing.domain.repository.BillingAccountRepository;
import com.evcharging.billing.domain.repository.InvoiceRepository;
import com.evcharging.identity.VendorMarkupApi;
import com.evcharging.session.SessionApi;
import com.evcharging.session.SessionApi.SessionDetails;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.station.StationApi;
import com.evcharging.station.StationApi.StationDetails;

@DisplayName("BillingApplicationService")
@ExtendWith(MockitoExtension.class)
class BillingApplicationServiceTest {

  @Mock InvoiceRepository invoiceRepository;
  @Mock BillingAccountRepository billingAccountRepository;
  @Mock SessionApi sessionApi;
  @Mock StationApi stationApi;
  @Mock VendorMarkupApi vendorMarkupApi;
  @Mock ApplicationEventPublisher eventPublisher;

  BillingApplicationService service;

  UUID sessionId = UUID.randomUUID();
  UUID customerId = UUID.randomUUID();
  UUID stationId = UUID.randomUUID();
  UUID vendorId = UUID.randomUUID();

  SessionDetails sessionDetails;
  StationDetails stationDetails;

  @BeforeEach
  void setUp() {
    service =
        new BillingApplicationService(
            invoiceRepository,
            billingAccountRepository,
            sessionApi,
            stationApi,
            vendorMarkupApi,
            eventPublisher);

    sessionDetails =
        new SessionDetails(
            sessionId,
            stationId,
            1,
            customerId,
            null,
            "COMPLETED",
            Instant.now().minusSeconds(300),
            Instant.now(),
            new BigDecimal("10.0000"),
            new BigDecimal("0.2500"),
            "EUR",
            new BigDecimal("2.5000"));

    stationDetails =
        new StationDetails(stationId, "AVAILABLE", vendorId.toString(), 2500, List.of());
  }

  @Nested
  @DisplayName("generateInvoice")
  class GenerateInvoice {

    @Test
    @DisplayName("should create invoice with line items and publish event")
    void shouldCreateInvoiceAndPublishEvent() {
      // Given
      given(invoiceRepository.findBySessionId(sessionId)).willReturn(Optional.empty());
      given(sessionApi.getSessionDetails(sessionId)).willReturn(Optional.of(sessionDetails));
      given(stationApi.getStationDetails(any())).willReturn(stationDetails);
      given(vendorMarkupApi.getMarkup(vendorId))
          .willReturn(Optional.of(MarkupPercentage.ofBasisPoints(1500))); // 15%
      given(billingAccountRepository.findByCustomerId(customerId))
          .willReturn(Optional.empty()); // new customer
      given(invoiceRepository.save(any(Invoice.class))).willAnswer(inv -> inv.getArgument(0));
      given(billingAccountRepository.save(any(BillingAccount.class)))
          .willAnswer(inv -> inv.getArgument(0));

      // When
      Invoice result = service.generateInvoice(sessionId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getSessionId()).isEqualTo(sessionId);
      assertThat(result.getCustomerId()).isEqualTo(customerId);
      assertThat(result.getVendorId()).isEqualTo(vendorId);
      assertThat(result.getLineItems()).isNotEmpty();
      assertThat(result.getTotalAmount().getAmountExact()).isPositive();

      // Verify event published
      ArgumentCaptor<InvoiceGeneratedEvent> eventCaptor =
          ArgumentCaptor.forClass(InvoiceGeneratedEvent.class);
      verify(eventPublisher).publishEvent(eventCaptor.capture());
      assertThat(eventCaptor.getValue().sessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("should be idempotent — return existing invoice if already generated")
    void shouldBeIdempotentWhenInvoiceAlreadyExists() {
      // Given: existing invoice present
      Invoice existing =
          Invoice.generate(
              sessionId,
              customerId,
              vendorId,
              List.of(
                  new com.evcharging.billing.domain.model.InvoiceLineItem(
                      "Base Charging Fee",
                      com.evcharging.shared.kernel.Money.of(new BigDecimal("0.2500"), "EUR"),
                      new BigDecimal("10.0000"))),
              Instant.now());
      given(invoiceRepository.findBySessionId(sessionId)).willReturn(Optional.of(existing));

      // When
      Invoice result = service.generateInvoice(sessionId);

      // Then: no new invoice created, no side effects
      assertThat(result.getSessionId()).isEqualTo(sessionId);
      verifyNoInteractions(sessionApi, stationApi, vendorMarkupApi, eventPublisher);
    }

    @Test
    @DisplayName("should throw when session not found")
    void shouldThrowWhenSessionNotFound() {
      given(invoiceRepository.findBySessionId(sessionId)).willReturn(Optional.empty());
      given(sessionApi.getSessionDetails(sessionId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.generateInvoice(sessionId))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Session not found");
    }
  }

  @Nested
  @DisplayName("getAdminIncomeReport")
  class GetAdminIncomeReport {

    @Test
    @DisplayName("should return total revenue and session count for date range")
    void shouldReturnReportForDateRange() {
      // Given: two invoices for same vendor
      Invoice inv1 =
          Invoice.generate(
              UUID.randomUUID(),
              customerId,
              vendorId,
              List.of(
                  new com.evcharging.billing.domain.model.InvoiceLineItem(
                      "Base",
                      com.evcharging.shared.kernel.Money.of(new BigDecimal("0.25"), "EUR"),
                      new BigDecimal("10.0000"))),
              Instant.now());
      Invoice inv2 =
          Invoice.generate(
              UUID.randomUUID(),
              UUID.randomUUID(),
              vendorId,
              List.of(
                  new com.evcharging.billing.domain.model.InvoiceLineItem(
                      "Base",
                      com.evcharging.shared.kernel.Money.of(new BigDecimal("0.25"), "EUR"),
                      new BigDecimal("5.0000"))),
              Instant.now());

      given(invoiceRepository.findAllByCreatedAtBetween(any(), any()))
          .willReturn(List.of(inv1, inv2));

      // When
      var report =
          service.getAdminIncomeReport(LocalDate.now().minusDays(7), LocalDate.now(), null);

      // Then
      assertThat(report.sessionCount()).isEqualTo(2);
      assertThat(report.totalRevenue()).isPositive();
    }

    @Test
    @DisplayName("should filter by vendorId when specified")
    void shouldFilterByVendor() {
      // Given: invoice only for this vendor
      Invoice inv =
          Invoice.generate(
              UUID.randomUUID(),
              customerId,
              vendorId,
              List.of(
                  new com.evcharging.billing.domain.model.InvoiceLineItem(
                      "Base",
                      com.evcharging.shared.kernel.Money.of(new BigDecimal("0.25"), "EUR"),
                      new BigDecimal("8.0000"))),
              Instant.now());

      given(invoiceRepository.findByVendorIdAndCreatedAtBetween(any(), any(), any()))
          .willReturn(List.of(inv));

      // When
      var report =
          service.getAdminIncomeReport(LocalDate.now().minusDays(30), LocalDate.now(), vendorId);

      // Then
      assertThat(report.sessionCount()).isEqualTo(1);
      verify(invoiceRepository).findByVendorIdAndCreatedAtBetween(any(), any(), any());
    }
  }
}
