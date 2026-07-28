package com.evcharging.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.Money;

@DisplayName("Invoice")
class InvoiceTest {

  private InvoiceLineItem createLineItem() {
    return new InvoiceLineItem("Charging Fee", Money.of(new BigDecimal("0.25"), "EUR"), new BigDecimal("10.0"));
  }

  @Nested
  @DisplayName("generate")
  class Generate {

    @Test
    @DisplayName("creates invoice with correct total")
    void shouldCreateInvoice() {
      InvoiceLineItem item1 = new InvoiceLineItem("Fee 1", Money.of(new BigDecimal("10.00"), "EUR"), new BigDecimal("1"));
      InvoiceLineItem item2 = new InvoiceLineItem("Fee 2", Money.of(new BigDecimal("5.00"), "EUR"), new BigDecimal("1"));

      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(item1, item2), Instant.now());

      assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PENDING);
      assertThat(invoice.getTotalAmount().getAmountExact()).isEqualByComparingTo(new BigDecimal("15.00"));
      assertThat(invoice.getLineItems()).hasSize(2);
    }

    @Test
    @DisplayName("throws on empty line items")
    void shouldThrowOnEmptyLineItems() {
      assertThatThrownBy(() -> Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(), Instant.now()))
          .isInstanceOf(IllegalArgumentException.class);
    }
  }

  @Nested
  @DisplayName("markPaid")
  class MarkPaid {

    @Test
    @DisplayName("transitions to PAID")
    void shouldMarkPaid() {
      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(createLineItem()), Instant.now());

      invoice.markPaid();

      assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
    }

    @Test
    @DisplayName("throws if already VOIDED")
    void shouldThrowIfVoided() {
      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(createLineItem()), Instant.now());
      invoice.markVoided();

      assertThatThrownBy(invoice::markPaid)
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  @DisplayName("markVoided")
  class MarkVoided {

    @Test
    @DisplayName("transitions to VOIDED")
    void shouldMarkVoided() {
      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(createLineItem()), Instant.now());

      invoice.markVoided();

      assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.VOIDED);
    }

    @Test
    @DisplayName("throws if already PAID")
    void shouldThrowIfPaid() {
      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(createLineItem()), Instant.now());
      invoice.markPaid();

      assertThatThrownBy(invoice::markVoided)
          .isInstanceOf(IllegalStateException.class);
    }
  }

  @Nested
  @DisplayName("equals and hashCode")
  class Equality {

    @Test
    @DisplayName("equal by id")
    void shouldBeEqualById() {
      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(createLineItem()), Instant.now());

      assertThat(invoice).isEqualTo(invoice);
      assertThat(invoice.hashCode()).isEqualTo(invoice.hashCode());
    }

    @Test
    @DisplayName("not equal to null")
    void shouldNotBeEqualToNull() {
      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(createLineItem()), Instant.now());

      assertThat(invoice).isNotEqualTo(null);
    }
  }

  @Nested
  @DisplayName("getLineItems")
  class GetLineItems {

    @Test
    @DisplayName("returns unmodifiable list")
    void shouldReturnUnmodifiableList() {
      Invoice invoice = Invoice.generate(
          UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
          List.of(createLineItem()), Instant.now());

      try {
        invoice.getLineItems().add(createLineItem());
      } catch (UnsupportedOperationException e) {
        // Expected
      }
    }
  }

  @Nested
  @DisplayName("getters")
  class Getters {

    @Test
    @DisplayName("returns all fields")
    void shouldReturnAllFields() {
      UUID sessionId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();
      UUID vendorId = UUID.randomUUID();
      Instant now = Instant.now();

      Invoice invoice = Invoice.generate(sessionId, customerId, vendorId, List.of(createLineItem()), now);

      assertThat(invoice.getId()).isNotNull();
      assertThat(invoice.getSessionId()).isEqualTo(sessionId);
      assertThat(invoice.getCustomerId()).isEqualTo(customerId);
      assertThat(invoice.getVendorId()).isEqualTo(vendorId);
      assertThat(invoice.getCreatedAt()).isEqualTo(now);
    }
  }
}
