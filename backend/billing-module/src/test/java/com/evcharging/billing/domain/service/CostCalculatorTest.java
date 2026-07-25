package com.evcharging.billing.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.billing.domain.model.InvoiceLineItem;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.Money;

@DisplayName("CostCalculator")
class CostCalculatorTest {

  private CostCalculator costCalculator;

  @BeforeEach
  void setUp() {
    costCalculator = new CostCalculator();
  }

  @Nested
  @DisplayName("calculateLineItems")
  class CalculateLineItems {

    @Test
    @DisplayName("should produce two line items (base + markup) when markup is non-zero")
    void shouldProduceBaseAndMarkupLineItems() {
      // Given: 10 kWh, total rate = 0.30 EUR/kWh, 20% markup (2000 BP)
      Money unitRate = Money.of(new BigDecimal("0.3000"), "EUR"); // already marked-up total
      MarkupPercentage markup = MarkupPercentage.ofBasisPoints(2000); // 20%

      // When
      List<InvoiceLineItem> items =
          costCalculator.calculateLineItems(new BigDecimal("10.0000"), unitRate, markup);

      // Then
      assertThat(items).hasSize(2);

      InvoiceLineItem baseItem = items.get(0);
      assertThat(baseItem.getDescription()).contains("Base Charging Fee");
      assertThat(baseItem.getQuantity()).isEqualByComparingTo(new BigDecimal("10.0000"));

      InvoiceLineItem markupItem = items.get(1);
      assertThat(markupItem.getDescription()).contains("Platform Markup Fee");
      assertThat(markupItem.getDescription()).containsPattern("20\\.00%");

      // Total of both items should equal energy × total rate
      Money expectedTotal = unitRate.multiply(new BigDecimal("10.0000"));
      Money actualTotal = baseItem.getTotalAmount().add(markupItem.getTotalAmount());
      assertThat(actualTotal.getAmountExact()).isEqualByComparingTo(expectedTotal.getAmountExact());
    }

    @Test
    @DisplayName("should produce only one base line item when markup is zero")
    void shouldProduceOnlyBaseLineItemWhenMarkupIsZero() {
      // Given: 5 kWh, 0.25 EUR/kWh, 0% markup
      Money unitRate = Money.of(new BigDecimal("0.2500"), "EUR");
      MarkupPercentage markup = MarkupPercentage.zero();

      // When
      List<InvoiceLineItem> items =
          costCalculator.calculateLineItems(new BigDecimal("5.0000"), unitRate, markup);

      // Then
      assertThat(items).hasSize(1);
      assertThat(items.get(0).getDescription()).contains("Base Charging Fee");

      Money expectedTotal = Money.of(new BigDecimal("1.2500"), "EUR");
      assertThat(items.get(0).getTotalAmount().getAmountExact())
          .isEqualByComparingTo(expectedTotal.getAmountExact());
    }

    @Test
    @DisplayName("should return zero total amount for zero energy")
    void shouldReturnZeroTotalForZeroEnergy() {
      // Given: 0 kWh
      Money unitRate = Money.of(new BigDecimal("0.3000"), "EUR");
      MarkupPercentage markup = MarkupPercentage.ofBasisPoints(1500); // 15%

      // When
      List<InvoiceLineItem> items =
          costCalculator.calculateLineItems(BigDecimal.ZERO, unitRate, markup);

      // Then: each line item should individually be zero
      for (InvoiceLineItem item : items) {
        assertThat(item.getTotalAmount().isZero())
            .as("Line item '%s' should have zero total for zero energy", item.getDescription())
            .isTrue();
      }
    }

    @Test
    @DisplayName("acceptance criteria: amount = energyKwh × (vendorUnitPrice + adminMarkup)")
    void shouldMatchAcceptanceCriteria() {
      // Story 004-001: Given a completed session,
      // Then amount = energy_kwh × (vendor_unit_price + admin_markup)
      // Let: vendor unit price = 0.20 EUR/kWh, admin markup = 15% (1500 BP)
      // Total marked-up rate per kWh = 0.20 * 1.15 = 0.23
      BigDecimal vendorBaseRate = new BigDecimal("0.2000");
      MarkupPercentage markup = MarkupPercentage.ofBasisPoints(1500);
      BigDecimal markedUpAmount =
          vendorBaseRate
              .multiply(BigDecimal.valueOf(1 + markup.getMultiplier()))
              .setScale(4, java.math.RoundingMode.HALF_UP);

      Money unitRate = Money.of(markedUpAmount, "EUR");
      BigDecimal energyKwh = new BigDecimal("12.5000");

      // When
      List<InvoiceLineItem> items = costCalculator.calculateLineItems(energyKwh, unitRate, markup);

      // Then: total = 12.5 × 0.23 = 2.875
      Money expectedTotal = unitRate.multiply(energyKwh);
      Money actualTotal =
          items.stream().map(InvoiceLineItem::getTotalAmount).reduce(Money.zeroEur(), Money::add);

      assertThat(actualTotal.getAmountExact()).isEqualByComparingTo(expectedTotal.getAmountExact());
    }
  }
}
