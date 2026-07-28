package com.evcharging.billing.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("InvoiceId")
class InvoiceIdTest {

  @Test
  @DisplayName("generates unique id")
  void shouldGenerateId() {
    InvoiceId id = InvoiceId.generate();
    assertThat(id.getValue()).isNotNull();
  }

  @Test
  @DisplayName("of creates from UUID")
  void shouldCreateFromUuid() {
    UUID uuid = UUID.randomUUID();
    InvoiceId id = InvoiceId.of(uuid);
    assertThat(id.getValue()).isEqualTo(uuid);
  }

  @Test
  @DisplayName("equals and hashCode")
  void shouldImplementEquality() {
    UUID uuid = UUID.randomUUID();
    InvoiceId id1 = InvoiceId.of(uuid);
    InvoiceId id2 = InvoiceId.of(uuid);

    assertThat(id1).isEqualTo(id2);
    assertThat(id1.hashCode()).isEqualTo(id2.hashCode());
  }

  @Test
  @DisplayName("not equal to different id")
  void shouldNotBeEqualToDifferentId() {
    InvoiceId id1 = InvoiceId.generate();
    InvoiceId id2 = InvoiceId.generate();
    assertThat(id1).isNotEqualTo(id2);
  }

  @Test
  @DisplayName("toString returns uuid string")
  void shouldImplementToString() {
    UUID uuid = UUID.randomUUID();
    InvoiceId id = InvoiceId.of(uuid);
    assertThat(id.toString()).isEqualTo(uuid.toString());
  }
}
