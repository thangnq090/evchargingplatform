package com.evcharging.billing.infrastructure.persistence.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import com.evcharging.billing.domain.model.BillingAccount;
import com.evcharging.billing.domain.model.BillingAccountId;
import com.evcharging.shared.kernel.Money;

@Entity
@Table(name = "billing_accounts", schema = "billing")
public class BillingAccountEntity {

  @Id private UUID id;

  @Column(name = "customer_id", nullable = false, unique = true)
  private UUID customerId;

  @Column(name = "balance_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal balanceAmount;

  @Column(name = "balance_currency", nullable = false, length = 3)
  private String balanceCurrency;

  @Column(name = "total_spent_amount", nullable = false, precision = 19, scale = 4)
  private BigDecimal totalSpentAmount;

  @Column(name = "total_spent_currency", nullable = false, length = 3)
  private String totalSpentCurrency;

  @Column(name = "last_billed_at")
  private Instant lastBilledAt;

  @Version private int version;

  public BillingAccountEntity() {}

  public static BillingAccountEntity fromDomain(BillingAccount account) {
    BillingAccountEntity entity = new BillingAccountEntity();
    entity.id = account.getId().getValue();
    entity.customerId = account.getCustomerId();
    entity.balanceAmount = account.getBalance().getAmountExact();
    entity.balanceCurrency = account.getBalance().getCurrency().getCurrencyCode();
    entity.totalSpentAmount = account.getTotalSpent().getAmountExact();
    entity.totalSpentCurrency = account.getTotalSpent().getCurrency().getCurrencyCode();
    entity.lastBilledAt = account.getLastBilledAt();
    return entity;
  }

  public BillingAccount toDomain() {
    return new BillingAccount(
        BillingAccountId.of(id),
        customerId,
        Money.of(balanceAmount, balanceCurrency),
        Money.of(totalSpentAmount, totalSpentCurrency),
        lastBilledAt);
  }

  // Getters and Setters
  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public void setCustomerId(UUID customerId) {
    this.customerId = customerId;
  }

  public BigDecimal getBalanceAmount() {
    return balanceAmount;
  }

  public void setBalanceAmount(BigDecimal balanceAmount) {
    this.balanceAmount = balanceAmount;
  }

  public String getBalanceCurrency() {
    return balanceCurrency;
  }

  public void setBalanceCurrency(String balanceCurrency) {
    this.balanceCurrency = balanceCurrency;
  }

  public BigDecimal getTotalSpentAmount() {
    return totalSpentAmount;
  }

  public void setTotalSpentAmount(BigDecimal totalSpentAmount) {
    this.totalSpentAmount = totalSpentAmount;
  }

  public String getTotalSpentCurrency() {
    return totalSpentCurrency;
  }

  public void setTotalSpentCurrency(String totalSpentCurrency) {
    this.totalSpentCurrency = totalSpentCurrency;
  }

  public Instant getLastBilledAt() {
    return lastBilledAt;
  }

  public void setLastBilledAt(Instant lastBilledAt) {
    this.lastBilledAt = lastBilledAt;
  }

  public int getVersion() {
    return version;
  }
}
