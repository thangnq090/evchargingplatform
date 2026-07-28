package com.evcharging.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

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

import com.evcharging.payment.domain.model.Payment;
import com.evcharging.payment.domain.model.PaymentAttemptStatus;
import com.evcharging.payment.domain.model.PaymentStatus;
import com.evcharging.payment.domain.port.PaymentProvider;
import com.evcharging.payment.domain.port.PaymentProvider.PaymentResult;
import com.evcharging.payment.domain.port.PaymentRepository;
import com.evcharging.payment.infrastructure.adapter.MockPaymentAdapter;

@DisplayName("PaymentOrchestrator")
@ExtendWith(MockitoExtension.class)
class PaymentOrchestratorTest {

  @Mock private PaymentRepository paymentRepository;
  @Mock private PaymentProvider paymentProvider;

  private PaymentOrchestrator paymentOrchestrator;

  @BeforeEach
  void setUp() {
    paymentOrchestrator = new PaymentOrchestrator(paymentRepository, paymentProvider);
  }

  private void stubNewPaymentSave() {
    // The orchestrator calls save twice (or more) - allow multiple saves
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));
  }

  @Nested
  @DisplayName("processPayment")
  class ProcessPayment {

    @Test
    @DisplayName("success flow: authorize + capture")
    void shouldProcessPaymentSuccessfully() {
      UUID sessionId = UUID.randomUUID();
      UUID customerId = UUID.randomUUID();
      UUID vehicleId = UUID.randomUUID();
      UUID chargePointId = UUID.randomUUID();
      BigDecimal amount = new BigDecimal("25.50");

      given(paymentRepository.findByIdempotencyKey(any())).willReturn(Optional.empty());
      stubNewPaymentSave();

      given(paymentProvider.authorize(any(), any(), any(), any(), any()))
          .willReturn(PaymentResult.success("prov_auth_1"));
      given(paymentProvider.capture(eq("prov_auth_1"), any(), any(), any()))
          .willReturn(PaymentResult.success("prov_auth_1"));

      Payment result =
          paymentOrchestrator.processPayment(
              sessionId, customerId, vehicleId, chargePointId, amount, "USD", null);

      assertThat(result).isNotNull();
      assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
      assertThat(result.getAmount()).isEqualTo(amount);
      assertThat(result.getSessionId()).isEqualTo(sessionId);
      assertThat(result.getAttempts()).hasSize(2);
      assertThat(result.getAttempts().get(0).getStatus()).isEqualTo(PaymentAttemptStatus.SUCCESS);
      assertThat(result.getAttempts().get(0).getStatus()).isEqualTo(PaymentAttemptStatus.SUCCESS);
      verify(paymentProvider).authorize(any(), any(), any(), any(), any());
      verify(paymentProvider).capture(eq("prov_auth_1"), any(), any(), any());
    }

    @Test
    @DisplayName("returns existing payment for idempotent call")
    void shouldReturnExistingPayment() {
      UUID sessionId = UUID.randomUUID();
      String key = "session:" + sessionId + ":charge";

      Payment existing =
          new Payment(
              UUID.randomUUID(),
              sessionId,
              UUID.randomUUID(),
              null,
              null,
              new BigDecimal("10.00"),
              "USD",
              null,
              key,
              Instant.now(),
              Instant.now());
      existing.markCaptured();

      when(paymentRepository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

      Payment result =
          paymentOrchestrator.processPayment(
              sessionId, UUID.randomUUID(), null, null, new BigDecimal("10.00"), "USD", null);

      assertThat(result).isEqualTo(existing);
      verify(paymentRepository, never()).save(any());
    }

    @Test
    @DisplayName("authorization failure")
    void shouldHandleAuthorizationFailure() {
      UUID sessionId = UUID.randomUUID();

      given(paymentRepository.findByIdempotencyKey(any())).willReturn(Optional.empty());
      stubNewPaymentSave();

      given(paymentProvider.authorize(any(), any(), any(), any(), any()))
          .willReturn(PaymentResult.failure("AUTH_DECLINED", "Card declined"));

      Payment result =
          paymentOrchestrator.processPayment(
              sessionId, UUID.randomUUID(), null, null, new BigDecimal("10.00"), "USD", null);

      assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
      assertThat(result.getAttempts()).hasSize(1);
      assertThat(result.getAttempts().get(0).getErrorCode()).isEqualTo("AUTH_DECLINED");
      assertThat(result.getAttempts().get(0).getStatus()).isEqualTo(PaymentAttemptStatus.FAILED);
      verify(paymentProvider, never()).capture(any(), any(), any(), any());
    }

    @Test
    @DisplayName("capture failure with void compensation")
    void shouldHandleCaptureFailure() {
      UUID sessionId = UUID.randomUUID();

      given(paymentRepository.findByIdempotencyKey(any())).willReturn(Optional.empty());
      stubNewPaymentSave();

      given(paymentProvider.authorize(any(), any(), any(), any(), any()))
          .willReturn(PaymentResult.success("prov_auth_1"));
      given(paymentProvider.capture(eq("prov_auth_1"), any(), any(), any()))
          .willReturn(PaymentResult.failure("CAPTURE_FAILED", "Network error"));
      given(paymentProvider.voidAuthorization(eq("prov_auth_1")))
          .willReturn(PaymentResult.success("prov_auth_1"));

      Payment result =
          paymentOrchestrator.processPayment(
              sessionId, UUID.randomUUID(), null, null, new BigDecimal("10.00"), "USD", null);

      assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
      assertThat(result.getAttempts()).hasSize(2);
      assertThat(result.getAttempts().get(1).getErrorCode()).isEqualTo("CAPTURE_FAILED");
      verify(paymentProvider).voidAuthorization("prov_auth_1");
    }

    @Test
    @DisplayName("defaults currency to USD when null")
    void shouldDefaultCurrency() {
      UUID sessionId = UUID.randomUUID();

      given(paymentRepository.findByIdempotencyKey(any())).willReturn(Optional.empty());
      stubNewPaymentSave();

      given(paymentProvider.authorize(any(), any(), any(), any(), any()))
          .willReturn(PaymentResult.success("prov_1"));
      given(paymentProvider.capture(any(), any(), any(), any()))
          .willReturn(PaymentResult.success("prov_1"));

      Payment result =
          paymentOrchestrator.processPayment(
              sessionId, UUID.randomUUID(), null, null, new BigDecimal("10.00"), null, null);

      assertThat(result.getCurrency()).isEqualTo("USD");
    }
  }
}
