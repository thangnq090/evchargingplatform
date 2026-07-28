package com.evcharging.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.Money;

@DisplayName("InvoiceLineItem")
class InvoiceLineItemTest {

  @Test
  @DisplayName("calculates total amount")
  void shouldCalculateTotal() {
    Money unitPrice = Money.of(new BigDecimal("0.25"), "EUR");
    BigDecimal quantity = new BigDecimal("100.0");

    InvoiceLineItem item = new InvoiceLineItem("Charging Fee", unitPrice, quantity);

    assertThat(item.getDescription()).isEqualTo("Charging Fee");
    assertThat(item.getUnitPrice()).isEqualTo(unitPrice);
    assertThat(item.getQuantity()).isEqualByComparingTo(quantity);
    assertThat(item.getTotalAmount().getAmountExact()).isEqualByComparingTo(new BigDecimal("25.00"));
  }

  @Test
  @DisplayName("throws on null description")
  void shouldThrowOnNullDescription() {
    assertThatThrownBy(() -> new InvoiceLineItem(null, Money.of(BigDecimal.ONE, "EUR"), BigDecimal.ONE))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("throws on null unitPrice")
  void shouldThrowOnNullUnitPrice() {
    assertThatThrownBy(() -> new InvoiceLineItem("Fee", null, BigDecimal.ONE))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("throws on null quantity")
  void shouldThrowOnNullQuantity() {
    assertThatThrownBy(() -> new InvoiceLineItem("Fee", Money.of(BigDecimal.ONE, "EUR"), null))
        .isInstanceOf(NullPointerException.class);
  }

  @Test
  @DisplayName("throws on negative quantity")
  void shouldThrowOnNegativeQuantity() {
    assertThatThrownBy(() -> new InvoiceLineItem("Fee", Money.of(BigDecimal.ONE, "EUR"), new BigDecimal("-1.0")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @DisplayName("zero quantity is allowed")
  void shouldAllowZeroQuantity() {
    InvoiceLineItem item = new InvoiceLineItem("Fee", Money.of(new BigDecimal("10.00"), "EUR"), BigDecimal.ZERO);
    assertThat(item.getTotalAmount().getAmountExact()).isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("equals and hashCode")
  void shouldImplementEqualsAndHashCode() {
    Money price = Money.of(new BigDecimal("0.25"), "EUR");
    InvoiceLineItem item1 = new InvoiceLineItem("Fee", price, new BigDecimal("10.0"));
    InvoiceLineItem item2 = new InvoiceLineItem("Fee", price, new BigDecimal("10.0"));
    assertThat(item1).isEqualTo(item2);
    assertThat(item1.hashCode()).isEqualTo(item2.hashCode());
  }

  @Test
  @DisplayName("toString includes fields")
  void shouldImplementToString() {
    InvoiceLineItem item = new InvoiceLineItem("Fee", Money.of(new BigDecimal("1.00"), "EUR"), new BigDecimal("5.0"));
    assertThat(item.toString()).contains("Fee");
  }
}
