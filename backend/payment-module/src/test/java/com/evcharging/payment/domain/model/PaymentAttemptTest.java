package com.evcharging.payment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("PaymentAttempt")
class PaymentAttemptTest {

  @Test
  @DisplayName("returns all fields")
  void shouldReturnAllFields() {
    UUID id = UUID.randomUUID();
    Instant now = Instant.now();

    PaymentAttempt attempt =
        new PaymentAttempt(id, 2, PaymentAttemptStatus.FAILED, "ERR_001", "Declined", now);

    assertThat(attempt.getId()).isEqualTo(id);
    assertThat(attempt.getAttemptNumber()).isEqualTo(2);
    assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
    assertThat(attempt.getErrorCode()).isEqualTo("ERR_001");
    assertThat(attempt.getErrorMessage()).isEqualTo("Declined");
    assertThat(attempt.getAttemptedAt()).isEqualTo(now);
  }

  @Test
  @DisplayName("supports null error fields")
  void shouldSupportNullErrorFields() {
    PaymentAttempt attempt =
        new PaymentAttempt(UUID.randomUUID(), 1, PaymentAttemptStatus.SUCCESS, null, null, Instant.now());

    assertThat(attempt.getErrorCode()).isNull();
    assertThat(attempt.getErrorMessage()).isNull();
  }
}
