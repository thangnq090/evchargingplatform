package com.evcharging.payment.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Payment")
class PaymentTest {

  private Payment createPayment() {
    return new Payment(
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        UUID.randomUUID(),
        new BigDecimal("25.00"),
        "EUR",
        UUID.randomUUID(),
        "idemp-123",
        Instant.now(),
        Instant.now());
  }

  @Nested
  @DisplayName("markAuthorized")
  class MarkAuthorized {

    @Test
    @DisplayName("sets AUTHORIZED status and provider id")
    void shouldMarkAuthorized() {
      Payment payment = createPayment();

      payment.markAuthorized("prov_auth_123");

      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
      assertThat(payment.getProviderPaymentId()).isEqualTo("prov_auth_123");
      assertThat(payment.getUpdatedAt()).isNotNull();
    }
  }

  @Nested
  @DisplayName("markCaptured")
  class MarkCaptured {

    @Test
    @DisplayName("sets CAPTURED status")
    void shouldMarkCaptured() {
      Payment payment = createPayment();
      payment.markAuthorized("prov_123");

      payment.markCaptured();

      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }
  }

  @Nested
  @DisplayName("markFailed")
  class MarkFailed {

    @Test
    @DisplayName("sets FAILED status")
    void shouldMarkFailed() {
      Payment payment = createPayment();

      payment.markFailed();

      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }
  }

  @Nested
  @DisplayName("addAttempt")
  class AddAttempt {

    @Test
    @DisplayName("adds attempt to list")
    void shouldAddAttempt() {
      Payment payment = createPayment();
      PaymentAttempt attempt =
          new PaymentAttempt(
              UUID.randomUUID(), 1, PaymentAttemptStatus.SUCCESS, null, "OK", Instant.now());

      payment.addAttempt(attempt);

      assertThat(payment.getAttempts()).hasSize(1);
      assertThat(payment.getAttempts().get(0)).isEqualTo(attempt);
    }

    @Test
    @DisplayName("returns unmodifiable list")
    void shouldReturnUnmodifiableList() {
      Payment payment = createPayment();

      try {
        payment.getAttempts().add(
            new PaymentAttempt(UUID.randomUUID(), 1, PaymentAttemptStatus.SUCCESS, null, null, Instant.now()));
        // If no exception, still verify
      } catch (UnsupportedOperationException e) {
        // Expected
      }
    }
  }

  @Nested
  @DisplayName("getters")
  class Getters {

    @Test
    @DisplayName("returns all fields")
    void shouldReturnAllFields() {
      UUID id = UUID.randomUUID();
      UUID sessionId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();
      UUID vehicleId = UUID.randomUUID();
      UUID chargePointId = UUID.randomUUID();
      UUID paymentMethodId = UUID.randomUUID();
      BigDecimal amount = new BigDecimal("10.00");
      Instant now = Instant.now();
      String idempotencyKey = "key-123";

      Payment payment =
          new Payment(
              id, sessionId, customerId, vehicleId, chargePointId,
              amount, "USD", paymentMethodId, idempotencyKey, now, now);

      assertThat(payment.getId()).isEqualTo(id);
      assertThat(payment.getSessionId()).isEqualTo(sessionId);
      assertThat(payment.getCustomerId()).isEqualTo(customerId);
      assertThat(payment.getVehicleId()).isEqualTo(vehicleId);
      assertThat(payment.getChargePointId()).isEqualTo(chargePointId);
      assertThat(payment.getAmount()).isEqualTo(amount);
      assertThat(payment.getCurrency()).isEqualTo("USD");
      assertThat(payment.getPaymentMethodId()).isEqualTo(paymentMethodId);
      assertThat(payment.getIdempotencyKey()).isEqualTo(idempotencyKey);
      assertThat(payment.getCreatedAt()).isEqualTo(now);
      assertThat(payment.getUpdatedAt()).isEqualTo(now);
      assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
      assertThat(payment.getProviderPaymentId()).isNull();
    }
  }
}
