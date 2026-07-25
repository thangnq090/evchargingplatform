package com.evcharging.billing.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Value object representing a unique invoice identifier. */
public final class InvoiceId implements Serializable {

  private final UUID value;

  private InvoiceId(UUID value) {
    this.value = Objects.requireNonNull(value, "Invoice ID value cannot be null");
  }

  public static InvoiceId of(UUID value) {
    return new InvoiceId(value);
  }

  public static InvoiceId generate() {
    return new InvoiceId(UUID.randomUUID());
  }

  public UUID getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InvoiceId invoiceId = (InvoiceId) o;
    return Objects.equals(value, invoiceId.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
