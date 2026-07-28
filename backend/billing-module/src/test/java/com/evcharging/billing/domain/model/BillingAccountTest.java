package com.evcharging.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.Money;

@DisplayName("BillingAccount")
class BillingAccountTest {

  @Nested
  @DisplayName("createForCustomer")
  class CreateForCustomer {

    @Test
    @DisplayName("creates account with zero balances")
    void shouldCreateAccount() {
      UUID customerId = UUID.randomUUID();

      BillingAccount account = BillingAccount.createForCustomer(customerId);

      assertThat(account.getId()).isNotNull();
      assertThat(account.getCustomerId()).isEqualTo(customerId);
      assertThat(account.getBalance().getAmountExact()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(account.getTotalSpent().getAmountExact()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(account.getLastBilledAt()).isNull();
    }
  }

  @Nested
  @DisplayName("billInvoice")
  class BillInvoice {

    @Test
    @DisplayName("adds amount to balance and totalSpent")
    void shouldBillInvoice() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      Money amount = Money.of(new BigDecimal("25.00"), "EUR");
      Instant now = Instant.now();

      account.billInvoice(amount, now);

      assertThat(account.getBalance().getAmountExact()).isEqualByComparingTo(new BigDecimal("25.00"));
      assertThat(account.getTotalSpent().getAmountExact()).isEqualByComparingTo(new BigDecimal("25.00"));
      assertThat(account.getLastBilledAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("accumulates multiple invoices")
    void shouldAccumulate() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());

      account.billInvoice(Money.of(new BigDecimal("10.00"), "EUR"), Instant.now());
      account.billInvoice(Money.of(new BigDecimal("20.00"), "EUR"), Instant.now());

      assertThat(account.getBalance().getAmountExact()).isEqualByComparingTo(new BigDecimal("30.00"));
      assertThat(account.getTotalSpent().getAmountExact()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("throws on null amount")
    void shouldThrowOnNull() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      assertThatThrownBy(() -> account.billInvoice(null, Instant.now()))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("recordPayment")
  class RecordPayment {

    @Test
    @DisplayName("subtracts payment from balance")
    void shouldRecordPayment() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      account.billInvoice(Money.of(new BigDecimal("50.00"), "EUR"), Instant.now());

      account.recordPayment(Money.of(new BigDecimal("30.00"), "EUR"));

      assertThat(account.getBalance().getAmountExact()).isEqualByComparingTo(new BigDecimal("20.00"));
      assertThat(account.getTotalSpent().getAmountExact()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("throws on null amount")
    void shouldThrowOnNull() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      assertThatThrownBy(() -> account.recordPayment(null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("equals and hashCode")
  class Equality {

    @Test
    @DisplayName("equal by id")
    void shouldBeEqualById() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      assertThat(account).isEqualTo(account);
      assertThat(account.hashCode()).isEqualTo(account.hashCode());
    }

    @Test
    @DisplayName("not equal to null")
    void shouldNotBeEqualToNull() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      assertThat(account).isNotEqualTo(null);
    }
  }

  @Nested
  @DisplayName("getters")
  class Getters {

    @Test
    @DisplayName("returns all fields")
    void shouldReturnAllFields() {
      UUID customerId = UUID.randomUUID();
      BillingAccount account = BillingAccount.createForCustomer(customerId);

      assertThat(account.getId()).isNotNull();
      assertThat(account.getCustomerId()).isEqualTo(customerId);
      assertThat(account.getBalance()).isNotNull();
      assertThat(account.getTotalSpent()).isNotNull();
      assertThat(account.getLastBilledAt()).isNull();
    }
  }
}
