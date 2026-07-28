package com.evcharging.billing.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.billing.application.dto.InvoiceResponse;
import com.evcharging.billing.application.service.BillingApplicationService;

import reactor.test.StepVerifier;

@DisplayName("AdminBillingController")
@ExtendWith(MockitoExtension.class)
class AdminBillingControllerTest {

  @Mock private BillingApplicationService billingApplicationService;

  private AdminBillingController controller;

  @BeforeEach
  void setUp() {
    controller = new AdminBillingController(billingApplicationService);
  }

  @Nested
  @DisplayName("getAdminIncomeReport")
  class GetAdminIncomeReport {

    @Test
    @DisplayName("returns income report")
    void shouldReturnReport() {
      var report = new com.evcharging.billing.BillingApi.IncomeSummary(150.0, 10);
      given(billingApplicationService.getAdminIncomeReport(
          LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null))
          .willReturn(report);

      StepVerifier.create(controller.getAdminIncomeReport(
          LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), null))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("returns report with vendor filter")
    void shouldReturnReportWithVendor() {
      UUID vendorId = UUID.randomUUID();
      var report = new com.evcharging.billing.BillingApi.IncomeSummary(100.0, 5);
      given(billingApplicationService.getAdminIncomeReport(
          LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), vendorId))
          .willReturn(report);

      StepVerifier.create(controller.getAdminIncomeReport(
          LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31), vendorId))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("getInvoiceBySession")
  class GetInvoiceBySession {

    @Test
    @DisplayName("returns invoice when found")
    void shouldReturnInvoice() {
      UUID sessionId = UUID.randomUUID();
      InvoiceResponse response = new InvoiceResponse(
          UUID.randomUUID(), sessionId, UUID.randomUUID(), UUID.randomUUID(),
          "PENDING", new BigDecimal("25.00"), "EUR", Instant.now(), List.of());
      given(billingApplicationService.getInvoiceBySessionId(sessionId))
          .willReturn(Optional.of(response));

      StepVerifier.create(controller.getInvoiceBySession(sessionId))
          .assertNext(resp -> {
            assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("returns 404 when not found")
    void shouldReturn404() {
      UUID sessionId = UUID.randomUUID();
      given(billingApplicationService.getInvoiceBySessionId(sessionId))
          .willReturn(Optional.empty());

      StepVerifier.create(controller.getInvoiceBySession(sessionId))
          .assertNext(resp -> {
            assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
          })
          .verifyComplete();
    }
  }
}
