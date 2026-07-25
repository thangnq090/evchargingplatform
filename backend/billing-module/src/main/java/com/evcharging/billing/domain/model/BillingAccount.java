package com.evcharging.billing.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import com.evcharging.shared.kernel.Money;

/** Aggregate root representing a customer's billing account. */
public class BillingAccount {

  private final BillingAccountId id;
  private final UUID customerId;
  private Money balance;
  private Money totalSpent;
  private Instant lastBilledAt;

  public BillingAccount(
      BillingAccountId id, UUID customerId, Money balance, Money totalSpent, Instant lastBilledAt) {
    this.id = Objects.requireNonNull(id, "Account ID cannot be null");
    this.customerId = Objects.requireNonNull(customerId, "Customer ID cannot be null");
    this.balance = Objects.requireNonNull(balance, "Balance cannot be null");
    this.totalSpent = Objects.requireNonNull(totalSpent, "Total spent cannot be null");
    this.lastBilledAt = lastBilledAt;
  }

  public static BillingAccount createForCustomer(UUID customerId) {
    return new BillingAccount(
        BillingAccountId.generate(), customerId, Money.zeroEur(), Money.zeroEur(), null);
  }

  public void billInvoice(Money amount, Instant billedAt) {
    Objects.requireNonNull(amount, "Invoice amount cannot be null");
    this.balance = this.balance.add(amount);
    this.totalSpent = this.totalSpent.add(amount);
    this.lastBilledAt = billedAt;
  }

  public void recordPayment(Money paymentAmount) {
    Objects.requireNonNull(paymentAmount, "Payment amount cannot be null");
    this.balance = this.balance.subtract(paymentAmount);
  }

  public BillingAccountId getId() {
    return id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public Money getBalance() {
    return balance;
  }

  public Money getTotalSpent() {
    return totalSpent;
  }

  public Instant getLastBilledAt() {
    return lastBilledAt;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BillingAccount that = (BillingAccount) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }
}
