package com.evcharging.billing.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.billing.domain.model.*;
import com.evcharging.shared.kernel.Money;

@DisplayName("InvoiceEntity")
class InvoiceEntityTest {

  private Invoice createInvoice() {
    InvoiceLineItem item = new InvoiceLineItem("Charging Fee", Money.of(new BigDecimal("0.25"), "EUR"), new BigDecimal("50.0"));
    return Invoice.generate(
        UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), List.of(item), Instant.now());
  }

  @Nested
  @DisplayName("fromDomain")
  class FromDomain {

    @Test
    @DisplayName("converts invoice with line items")
    void shouldConvertInvoice() {
      Invoice invoice = createInvoice();

      InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);

      assertThat(entity.getId()).isEqualTo(invoice.getId().getValue());
      assertThat(entity.getSessionId()).isEqualTo(invoice.getSessionId());
      assertThat(entity.getCustomerId()).isEqualTo(invoice.getCustomerId());
      assertThat(entity.getVendorId()).isEqualTo(invoice.getVendorId());
      assertThat(entity.getStatus()).isEqualTo("PENDING");
      assertThat(entity.getLineItems()).hasSize(1);
      assertThat(entity.getCreatedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("round-trips domain to entity and back")
    void shouldRoundTrip() {
      Invoice invoice = createInvoice();

      InvoiceEntity entity = InvoiceEntity.fromDomain(invoice);
      Invoice domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(invoice.getId());
      assertThat(domain.getSessionId()).isEqualTo(invoice.getSessionId());
      assertThat(domain.getCustomerId()).isEqualTo(invoice.getCustomerId());
      assertThat(domain.getVendorId()).isEqualTo(invoice.getVendorId());
      assertThat(domain.getStatus()).isEqualTo(InvoiceStatus.PENDING);
      assertThat(domain.getLineItems()).hasSize(1);
      assertThat(domain.getTotalAmount()).isEqualTo(invoice.getTotalAmount());
    }
  }

  @Nested
  @DisplayName("setters")
  class Setters {

    @Test
    @DisplayName("sets and gets all fields")
    void shouldSetAndGetFields() {
      InvoiceEntity entity = new InvoiceEntity();
      UUID id = UUID.randomUUID();
      entity.setId(id);
      entity.setSessionId(UUID.randomUUID());
      entity.setCustomerId(UUID.randomUUID());
      entity.setVendorId(UUID.randomUUID());
      entity.setTotalAmount(new BigDecimal("12.50"));
      entity.setCurrency("EUR");
      entity.setStatus("PAID");
      entity.setCreatedAt(Instant.now());
      entity.setLineItems(List.of());

      assertThat(entity.getId()).isEqualTo(id);
      assertThat(entity.getSessionId()).isNotNull();
      assertThat(entity.getCustomerId()).isNotNull();
      assertThat(entity.getVendorId()).isNotNull();
      assertThat(entity.getTotalAmount()).isEqualTo(new BigDecimal("12.50"));
      assertThat(entity.getCurrency()).isEqualTo("EUR");
      assertThat(entity.getStatus()).isEqualTo("PAID");
      assertThat(entity.getCreatedAt()).isNotNull();
      assertThat(entity.getLineItems()).isEmpty();
      assertThat(entity.getVersion()).isEqualTo(0);
    }
  }
}
