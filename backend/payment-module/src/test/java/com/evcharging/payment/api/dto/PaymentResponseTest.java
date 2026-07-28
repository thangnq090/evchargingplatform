package com.evcharging.payment.api.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.evcharging.payment.domain.model.Payment;
import com.evcharging.payment.domain.model.PaymentAttempt;
import com.evcharging.payment.domain.model.PaymentAttemptStatus;
import com.evcharging.payment.domain.model.PaymentStatus;

@DisplayName("PaymentResponse")
class PaymentResponseTest {

  @Test
  @DisplayName("from creates response from payment with no attempts")
  void shouldCreateFromPayment() {
    Payment payment =
        new Payment(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("15.00"),
            "EUR",
            UUID.randomUUID(),
            "idemp-key",
            Instant.now(),
            Instant.now());
    payment.markAuthorized("prov_auth");
    payment.markCaptured();

    PaymentResponse response = PaymentResponse.from(payment);

    assertThat(response.id()).isEqualTo(payment.getId());
    assertThat(response.sessionId()).isEqualTo(payment.getSessionId());
    assertThat(response.amount()).isEqualTo("15.00");
    assertThat(response.currency()).isEqualTo("EUR");
    assertThat(response.status()).isEqualTo("CAPTURED");
    assertThat(response.providerPaymentId()).isEqualTo("prov_auth");
    assertThat(response.idempotencyKey()).isEqualTo("idemp-key");
    assertThat(response.attempts()).isEmpty();
  }

  @Test
  @DisplayName("from creates response from payment with attempts")
  void shouldCreateFromPaymentWithAttempts() {
    Payment payment =
        new Payment(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("30.00"),
            "USD",
            null,
            "idemp-key-2",
            Instant.now(),
            Instant.now());

    payment.addAttempt(
        new PaymentAttempt(
            UUID.randomUUID(), 1, PaymentAttemptStatus.SUCCESS, null, "Auth OK", Instant.now()));
    payment.addAttempt(
        new PaymentAttempt(
            UUID.randomUUID(), 2, PaymentAttemptStatus.SUCCESS, null, "Capture OK", Instant.now()));

    PaymentResponse response = PaymentResponse.from(payment);

    assertThat(response.attempts()).hasSize(2);
    assertThat(response.attempts().get(0).status()).isEqualTo("SUCCESS");
    assertThat(response.attempts().get(0).attemptNumber()).isEqualTo(1);
    assertThat(response.attempts().get(1).attemptNumber()).isEqualTo(2);
  }

  @Test
  @DisplayName("from with failed attempt")
  void shouldMapFailedAttempt() {
    Payment payment =
        new Payment(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            new BigDecimal("10.00"),
            "USD",
            null,
            "idemp-key-3",
            Instant.now(),
            Instant.now());

    payment.addAttempt(
        new PaymentAttempt(
            UUID.randomUUID(), 1, PaymentAttemptStatus.FAILED, "ERR_500", "Declined", Instant.now()));

    PaymentResponse response = PaymentResponse.from(payment);

    assertThat(response.attempts()).hasSize(1);
    assertThat(response.attempts().get(0).errorCode()).isEqualTo("ERR_500");
    assertThat(response.attempts().get(0).errorMessage()).isEqualTo("Declined");
  }

  @Test
  @DisplayName("PaymentAttemptResponse record")
  void shouldCreatePaymentAttemptResponse() {
    UUID id = UUID.randomUUID();
    PaymentAttemptResponse response =
        new PaymentAttemptResponse(id, 1, "SUCCESS", null, "OK");

    assertThat(response.id()).isEqualTo(id);
    assertThat(response.attemptNumber()).isEqualTo(1);
    assertThat(response.status()).isEqualTo("SUCCESS");
    assertThat(response.errorCode()).isNull();
    assertThat(response.errorMessage()).isEqualTo("OK");
  }
}
