package com.evcharging.billing.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.billing.domain.model.InvoiceLineItem;
import com.evcharging.shared.kernel.Money;

@DisplayName("InvoiceLineItemEntity")
class InvoiceLineItemEntityTest {

  @Nested
  @DisplayName("fromDomain")
  class FromDomain {

    @Test
    @DisplayName("converts line item")
    void shouldConvertLineItem() {
      InvoiceLineItem item = new InvoiceLineItem("Charging Fee", Money.of(new BigDecimal("0.25"), "EUR"), new BigDecimal("50.0"));
      InvoiceEntity invoiceEntity = new InvoiceEntity();
      invoiceEntity.setId(UUID.randomUUID());

      InvoiceLineItemEntity entity = InvoiceLineItemEntity.fromDomain(item, invoiceEntity);

      assertThat(entity.getDescription()).isEqualTo("Charging Fee");
      assertThat(entity.getUnitPriceAmount()).isEqualByComparingTo(new BigDecimal("0.25"));
      assertThat(entity.getUnitPriceCurrency()).isEqualTo("EUR");
      assertThat(entity.getQuantity()).isEqualByComparingTo(new BigDecimal("50.0"));
      assertThat(entity.getInvoice()).isSameAs(invoiceEntity);
      assertThat(entity.getId()).isNotNull();
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("round-trips")
    void shouldRoundTrip() {
      InvoiceLineItem item = new InvoiceLineItem("Markup Fee", Money.of(new BigDecimal("0.05"), "EUR"), new BigDecimal("100.0"));
      InvoiceEntity invoiceEntity = new InvoiceEntity();
      invoiceEntity.setId(UUID.randomUUID());

      InvoiceLineItemEntity entity = InvoiceLineItemEntity.fromDomain(item, invoiceEntity);
      InvoiceLineItem domain = entity.toDomain();

      assertThat(domain.getDescription()).isEqualTo("Markup Fee");
      assertThat(domain.getUnitPrice().getAmountExact()).isEqualByComparingTo(new BigDecimal("0.05"));
      assertThat(domain.getQuantity()).isEqualByComparingTo(new BigDecimal("100.0"));
    }
  }

  @Nested
  @DisplayName("setters")
  class Setters {

    @Test
    @DisplayName("sets all fields")
    void shouldSetAllFields() {
      InvoiceLineItemEntity entity = new InvoiceLineItemEntity();
      entity.setId(UUID.randomUUID());
      entity.setInvoice(new InvoiceEntity());
      entity.setDescription("Test");
      entity.setUnitPriceAmount(new BigDecimal("1.00"));
      entity.setUnitPriceCurrency("EUR");
      entity.setQuantity(new BigDecimal("2.0"));
      entity.setTotalAmount(new BigDecimal("2.00"));
      entity.setCurrency("EUR");

      assertThat(entity.getId()).isNotNull();
      assertThat(entity.getDescription()).isEqualTo("Test");
      assertThat(entity.getUnitPriceAmount()).isEqualByComparingTo(new BigDecimal("1.00"));
      assertThat(entity.getQuantity()).isEqualByComparingTo(new BigDecimal("2.0"));
      assertThat(entity.getTotalAmount()).isEqualByComparingTo(new BigDecimal("2.00"));
    }
  }
}
