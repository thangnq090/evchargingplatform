package com.evcharging.billing.domain.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import com.evcharging.billing.domain.model.InvoiceLineItem;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.Money;

/** Domain service to perform invoice cost calculations and generate line items. */
public class CostCalculator {

  /** Calculates line items for base charging and markup based on energy and rate parameters. */
  public List<InvoiceLineItem> calculateLineItems(
      BigDecimal energyKwh, Money unitRate, MarkupPercentage markup) {
    List<InvoiceLineItem> lineItems = new ArrayList<>();

    // Deconstruct unitRate into base rate and markup rate
    BigDecimal totalRateAmount = unitRate.getAmountExact();
    double markupMultiplier = markup.getMultiplier();
    
    BigDecimal divisor = BigDecimal.valueOf(1.0 + markupMultiplier);
    BigDecimal baseRateAmount = totalRateAmount.divide(divisor, 4, RoundingMode.HALF_UP);
    BigDecimal markupRateAmount = totalRateAmount.subtract(baseRateAmount);

    Money baseRate = Money.of(baseRateAmount, unitRate.getCurrency());
    Money markupRate = Money.of(markupRateAmount, unitRate.getCurrency());

    // Create Base Charging Fee line item
    String baseDesc = String.format("Base Charging Fee (%s kWh @ %s %s/kWh)",
        energyKwh.setScale(2, RoundingMode.HALF_UP),
        unitRate.getCurrency().getCurrencyCode(),
        baseRateAmount.setScale(4, RoundingMode.HALF_UP).toPlainString());
    lineItems.add(new InvoiceLineItem(baseDesc, baseRate, energyKwh));

    // Create Platform Markup Fee line item if markup is positive
    if (markup.getBasisPoints() > 0) {
      String markupDesc = String.format("Platform Markup Fee (%.2f%%)",
          markup.getMultiplier() * 100.0);
      lineItems.add(new InvoiceLineItem(markupDesc, markupRate, energyKwh));
    }

    return lineItems;
  }
}
