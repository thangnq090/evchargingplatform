package com.evcharging.billing.infrastructure.persistence.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.billing.domain.model.BillingAccount;
import com.evcharging.billing.domain.model.BillingAccountId;
import com.evcharging.shared.kernel.Money;

@DisplayName("BillingAccountEntity")
class BillingAccountEntityTest {

  private BillingAccount createAccount() {
    return BillingAccount.createForCustomer(UUID.randomUUID());
  }

  @Nested
  @DisplayName("fromDomain")
  class FromDomain {

    @Test
    @DisplayName("converts account")
    void shouldConvertAccount() {
      BillingAccount account = createAccount();

      BillingAccountEntity entity = BillingAccountEntity.fromDomain(account);

      assertThat(entity.getId()).isEqualTo(account.getId().getValue());
      assertThat(entity.getCustomerId()).isEqualTo(account.getCustomerId());
      assertThat(entity.getBalanceAmount()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(entity.getBalanceCurrency()).isEqualTo("EUR");
      assertThat(entity.getTotalSpentAmount()).isEqualByComparingTo(BigDecimal.ZERO);
      assertThat(entity.getTotalSpentCurrency()).isEqualTo("EUR");
      assertThat(entity.getLastBilledAt()).isNull();
    }
  }

  @Nested
  @DisplayName("toDomain")
  class ToDomain {

    @Test
    @DisplayName("round-trips")
    void shouldRoundTrip() {
      BillingAccount account = createAccount();
      Instant now = Instant.now();
      account.billInvoice(Money.of(new BigDecimal("25.00"), "EUR"), now);

      BillingAccountEntity entity = BillingAccountEntity.fromDomain(account);
      BillingAccount domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(account.getId());
      assertThat(domain.getCustomerId()).isEqualTo(account.getCustomerId());
      assertThat(domain.getBalance().getAmountExact()).isEqualByComparingTo(account.getBalance().getAmountExact());
      assertThat(domain.getTotalSpent().getAmountExact()).isEqualByComparingTo(account.getTotalSpent().getAmountExact());
      assertThat(domain.getLastBilledAt()).isEqualTo(now);
    }
  }

  @Nested
  @DisplayName("setters")
  class Setters {

    @Test
    @DisplayName("sets all fields")
    void shouldSetAllFields() {
      BillingAccountEntity entity = new BillingAccountEntity();
      entity.setId(UUID.randomUUID());
      entity.setCustomerId(UUID.randomUUID());
      entity.setBalanceAmount(new BigDecimal("10.00"));
      entity.setBalanceCurrency("EUR");
      entity.setTotalSpentAmount(new BigDecimal("100.00"));
      entity.setTotalSpentCurrency("EUR");
      entity.setLastBilledAt(Instant.now());

      assertThat(entity.getId()).isNotNull();
      assertThat(entity.getCustomerId()).isNotNull();
      assertThat(entity.getBalanceAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
      assertThat(entity.getTotalSpentAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
      assertThat(entity.getLastBilledAt()).isNotNull();
      assertThat(entity.getVersion()).isEqualTo(0);
    }
  }
}
