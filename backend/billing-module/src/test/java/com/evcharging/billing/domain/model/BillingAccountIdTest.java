package com.evcharging.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BillingAccountId")
class BillingAccountIdTest {

  @Test
  @DisplayName("generates unique id")
  void shouldGenerateId() {
    BillingAccountId id = BillingAccountId.generate();
    assertThat(id.getValue()).isNotNull();
  }

  @Test
  @DisplayName("of creates from UUID")
  void shouldCreateFromUuid() {
    UUID uuid = UUID.randomUUID();
    BillingAccountId id = BillingAccountId.of(uuid);
    assertThat(id.getValue()).isEqualTo(uuid);
  }

  @Test
  @DisplayName("equals and hashCode")
  void shouldImplementEquality() {
    UUID uuid = UUID.randomUUID();
    BillingAccountId id1 = BillingAccountId.of(uuid);
    BillingAccountId id2 = BillingAccountId.of(uuid);

    assertThat(id1).isEqualTo(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }

  @Test
  @DisplayName("not equal to different id")
  void shouldNotBeEqualToDifferentId() {
    BillingAccountId id1 = BillingAccountId.generate();
    BillingAccountId id2 = BillingAccountId.generate();
    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  @DisplayName("toString returns uuid string")
  void shouldImplementToString() {
    UUID uuid = UUID.randomUUID();
    BillingAccountId id = BillingAccountId.of(uuid);
    assertThat(id.toString()).isEqualTo(uuid.toString());
  }
}
