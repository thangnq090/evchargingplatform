package com.evcharging.billing.application.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvoiceResponse")
class InvoiceResponseTest {

  @Test
  @DisplayName("record with all fields")
  void shouldCreateInvoiceResponse() {
    UUID id = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    Instant now = Instant.now();

    InvoiceResponse.LineItemDto lineItem =
        new InvoiceResponse.LineItemDto("Fee", new BigDecimal("1.00"), "EUR", new BigDecimal("10.0"), new BigDecimal("10.00"));

    InvoiceResponse response = new InvoiceResponse(
        id, sessionId, UUID.randomUUID(), UUID.randomUUID(),
        "PENDING", new BigDecimal("10.00"), "EUR", now, List.of(lineItem));

    assertThat(response.invoiceId()).isEqualTo(id);
    assertThat(response.sessionId()).isEqualTo(sessionId);
    assertThat(response.status()).isEqualTo("PENDING");
    assertThat(response.lineItems()).hasSize(1);
    assertThat(response.lineItems().get(0).description()).isEqualTo("Fee");
  }

  @Test
  @DisplayName("LineItemDto record")
  void shouldCreateLineItemDto() {
    InvoiceResponse.LineItemDto dto =
        new InvoiceResponse.LineItemDto("Fee", new BigDecimal("1.00"), "EUR", new BigDecimal("5.0"), new BigDecimal("5.00"));

    assertThat(dto.description()).isEqualTo("Fee");
    assertThat(dto.unitPrice()).isEqualByComparingTo(new BigDecimal("1.00"));
    assertThat(dto.currency()).isEqualTo("EUR");
    assertThat(dto.quantity()).isEqualByComparingTo(new BigDecimal("5.0"));
    assertThat(dto.totalAmount()).isEqualByComparingTo(new BigDecimal("5.00"));
  }
}
