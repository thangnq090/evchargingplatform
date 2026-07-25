package com.evcharging.billing.domain.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/** Value object representing a unique billing account identifier. */
public final class BillingAccountId implements Serializable {

  private final UUID value;

  private BillingAccountId(UUID value) {
    this.value = Objects.requireNonNull(value, "Billing account ID value cannot be null");
  }

  public static BillingAccountId of(UUID value) {
    return new BillingAccountId(value);
  }

  public static BillingAccountId generate() {
    return new BillingAccountId(UUID.randomUUID());
  }

  public UUID getValue() {
    return value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BillingAccountId that = (BillingAccountId) o;
    return Objects.equals(value, that.value);
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
