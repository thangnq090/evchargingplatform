package com.evcharging.billing.infrastructure.persistence.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.billing.domain.model.BillingAccount;
import com.evcharging.billing.infrastructure.persistence.entity.BillingAccountEntity;

@DisplayName("BillingAccountRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class BillingAccountRepositoryAdapterTest {

  @Mock private JpaBillingAccountRepository jpa;

  private BillingAccountRepositoryAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new BillingAccountRepositoryAdapter(jpa);
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves account")
    void shouldSaveAccount() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      given(jpa.save(any(BillingAccountEntity.class))).willAnswer(inv -> inv.getArgument(0));

      BillingAccount result = adapter.save(account);

      assertThat(result).isNotNull();
      assertThat(result.getCustomerId()).isEqualTo(account.getCustomerId());
    }
  }

  @Nested
  @DisplayName("findByCustomerId")
  class FindByCustomerId {

    @Test
    @DisplayName("returns account when found")
    void shouldReturnAccount() {
      BillingAccount account = BillingAccount.createForCustomer(UUID.randomUUID());
      BillingAccountEntity entity = BillingAccountEntity.fromDomain(account);
      given(jpa.findByCustomerId(account.getCustomerId())).willReturn(Optional.of(entity));

      Optional<BillingAccount> result = adapter.findByCustomerId(account.getCustomerId());
      assertThat(result).isPresent();
      assertThat(result.get().getCustomerId()).isEqualTo(account.getCustomerId());
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findByCustomerId(any(UUID.class))).willReturn(Optional.empty());
      assertThat(adapter.findByCustomerId(UUID.randomUUID())).isEmpty();
    }
  }
}
