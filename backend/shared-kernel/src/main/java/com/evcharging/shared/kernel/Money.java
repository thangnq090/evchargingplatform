package com.evcharging.shared.kernel;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value object representing a monetary amount with currency. Uses JSR 354 (Money & Currency API)
 * for type-safe monetary operations.
 *
 * <p>This is a SHARED technical utility - NOT a domain concept. Each module should define its own
 * domain-specific Money types if needed.
 */
@Embeddable
@AttributeOverrides({
  @AttributeOverride(name = "amount", column = @Column(name = "amount", precision = 19, scale = 4)),
  @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3))
})
public final class Money implements Serializable {

  private final BigDecimal amount;
  private final Currency currency;

  private Money(BigDecimal amount, Currency currency) {
    this.amount = Objects.requireNonNull(amount, "Amount cannot be null");
    this.currency = Objects.requireNonNull(currency, "Currency cannot be null");

    // Validate scale - max 4 decimal places
    if (amount.scale() > 4) {
      throw new IllegalArgumentException("Amount cannot have more than 4 decimal places");
    }
  }

  /** Creates a Money instance from amount and currency code. */
  public static Money of(BigDecimal amount, String currencyCode) {
    return new Money(amount, Currency.getInstance(currencyCode));
  }

  /** Creates a Money instance from amount and Currency. */
  public static Money of(BigDecimal amount, Currency currency) {
    return new Money(amount, currency);
  }

  /** Creates a Money instance from a JSR 354 MonetaryAmount. */
  public static Money of(javax.money.MonetaryAmount monetaryAmount) {
    return new Money(
        monetaryAmount.getNumber().numberValue(BigDecimal.class),
        Currency.getInstance(monetaryAmount.getCurrency().getCurrencyCode()));
  }

  /** Creates a zero amount for the given currency. */
  public static Money zero(Currency currency) {
    return new Money(BigDecimal.ZERO.setScale(4), currency);
  }

  /** Creates a zero amount in EUR (default platform currency). */
  public static Money zeroEur() {
    return zero(Currency.getInstance("EUR"));
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public Currency getCurrency() {
    return currency;
  }

  /** Returns the amount as a BigDecimal with 4 decimal places. */
  public BigDecimal getAmountExact() {
    return amount.setScale(4);
  }

  /** Adds another Money amount. Currencies must match. */
  public Money add(Money other) {
    requireSameCurrency(other);
    return new Money(this.amount.add(other.amount), this.currency);
  }

  /** Subtracts another Money amount. Currencies must match. */
  public Money subtract(Money other) {
    requireSameCurrency(other);
    return new Money(this.amount.subtract(other.amount), this.currency);
  }

  /** Multiplies by a factor. */
  public Money multiply(BigDecimal factor) {
    return new Money(
        this.amount.multiply(factor).setScale(4, BigDecimal.ROUND_HALF_EVEN), this.currency);
  }

  /** Divides by a divisor. */
  public Money divide(BigDecimal divisor) {
    return new Money(this.amount.divide(divisor, 4, BigDecimal.ROUND_HALF_EVEN), this.currency);
  }

  /** Returns the absolute value. */
  public Money abs() {
    return new Money(this.amount.abs(), this.currency);
  }

  /** Negates the amount. */
  public Money negate() {
    return new Money(this.amount.negate(), this.currency);
  }

  /** Checks if amount is zero. */
  public boolean isZero() {
    return BigDecimal.ZERO.compareTo(amount) == 0;
  }

  /** Checks if amount is positive. */
  public boolean isPositive() {
    return amount.compareTo(BigDecimal.ZERO) > 0;
  }

  /** Checks if amount is negative. */
  public boolean isNegative() {
    return amount.compareTo(BigDecimal.ZERO) < 0;
  }

  /** Checks if amount is greater than or equal to another. */
  public boolean isGreaterThanOrEqual(Money other) {
    requireSameCurrency(other);
    return this.amount.compareTo(other.amount) >= 0;
  }

  /** Checks if amount is less than another. */
  public boolean isLessThan(Money other) {
    requireSameCurrency(other);
    return this.amount.compareTo(other.amount) < 0;
  }

  private void requireSameCurrency(Money other) {
    if (!this.currency.equals(other.currency)) {
      throw new IllegalArgumentException(
          "Currency mismatch: " + this.currency + " vs " + other.currency);
    }
  }

  /** Converts to JSR 354 MonetaryAmount for calculations. */
  public javax.money.MonetaryAmount toMonetaryAmount() {
    return org.javamoney.moneta.Money.of(amount, currency.getCurrencyCode());
  }

  @Override
  public String toString() {
    return currency.getSymbol() + " " + amount.setScale(2).toPlainString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Money money = (Money) o;
    return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
  }

  @Override
  public int hashCode() {
    return Objects.hash(amount, currency);
  }
}
