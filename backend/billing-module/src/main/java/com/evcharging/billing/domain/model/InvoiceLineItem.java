package com.evcharging.billing.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import com.evcharging.shared.kernel.Money;

/** Value object representing a line item on an invoice. */
public final class InvoiceLineItem {

  private final String description;
  private final Money unitPrice;
  private final BigDecimal quantity;
  private final Money totalAmount;

  public InvoiceLineItem(String description, Money unitPrice, BigDecimal quantity) {
    this.description = Objects.requireNonNull(description, "Description cannot be null");
    this.unitPrice = Objects.requireNonNull(unitPrice, "UnitPrice cannot be null");
    this.quantity = Objects.requireNonNull(quantity, "Quantity cannot be null");
    if (quantity.compareTo(BigDecimal.ZERO) < 0) {
      throw new IllegalArgumentException("Quantity cannot be negative");
    }
    this.totalAmount = unitPrice.multiply(quantity);
  }

  public String getDescription() {
    return description;
  }

  public Money getUnitPrice() {
    return unitPrice;
  }

  public BigDecimal getQuantity() {
    return quantity;
  }

  public Money getTotalAmount() {
    return totalAmount;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InvoiceLineItem that = (InvoiceLineItem) o;
    return Objects.equals(description, that.description) &&
           Objects.equals(unitPrice, that.unitPrice) &&
           Objects.equals(quantity, that.quantity) &&
           Objects.equals(totalAmount, that.totalAmount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, unitPrice, quantity, totalAmount);
  }

  @Override
  public String toString() {
    return "InvoiceLineItem{" +
           "description='" + description + '\'' +
           ", unitPrice=" + unitPrice +
           ", quantity=" + quantity +
           ", totalAmount=" + totalAmount +
           '}';
  }
}
