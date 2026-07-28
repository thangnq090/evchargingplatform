package com.evcharging.payment.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.payment.domain.model.PaymentAttemptStatus;

@DisplayName("PaymentEntity")
class PaymentEntityTest {

  @Nested
  @DisplayName("addAttempt")
  class AddAttempt {

    @Test
    @DisplayName("adds attempt and sets back-reference")
    void shouldAddAttempt() {
      PaymentEntity payment = new PaymentEntity();
      payment.setId(UUID.randomUUID());

      PaymentAttemptEntity attempt =
          new PaymentAttemptEntity(
              UUID.randomUUID(),
              payment,
              1,
              PaymentAttemptStatus.SUCCESS,
              null,
              "OK",
              Instant.now());

      payment.addAttempt(attempt);

      assertThat(payment.getAttempts()).hasSize(1);
      assertThat(payment.getAttempts().get(0).getPayment()).isSameAs(payment);
    }

    @Test
    @DisplayName("initializes empty attempts list")
    void shouldInitializeEmptyAttempts() {
      PaymentEntity payment = new PaymentEntity();
      assertThat(payment.getAttempts()).isNotNull().isEmpty();
    }
  }

  @Nested
  @DisplayName("PaymentAttemptEntity")
  class PaymentAttemptEntityTest {

    @Test
    @DisplayName("constructor sets all fields")
    void shouldSetAllFields() {
      UUID id = UUID.randomUUID();
      PaymentEntity payment = new PaymentEntity();
      payment.setId(UUID.randomUUID());
      Instant now = Instant.now();

      PaymentAttemptEntity attempt =
          new PaymentAttemptEntity(id, payment, 3, PaymentAttemptStatus.FAILED, "ERR_400", "Bad request", now);

      assertThat(attempt.getId()).isEqualTo(id);
      assertThat(attempt.getPayment()).isSameAs(payment);
      assertThat(attempt.getAttemptNumber()).isEqualTo(3);
      assertThat(attempt.getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
      assertThat(attempt.getErrorCode()).isEqualTo("ERR_400");
      assertThat(attempt.getErrorMessage()).isEqualTo("Bad request");
      assertThat(attempt.getAttemptedAt()).isEqualTo(now);
    }
  }
}
