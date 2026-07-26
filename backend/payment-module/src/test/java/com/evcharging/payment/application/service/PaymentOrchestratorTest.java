package com.evcharging.payment.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.payment.domain.model.Payment;
import com.evcharging.payment.domain.model.PaymentStatus;
import com.evcharging.payment.domain.port.PaymentProvider;
import com.evcharging.payment.domain.port.PaymentRepository;
import com.evcharging.payment.infrastructure.adapter.MockPaymentAdapter;

@ExtendWith(MockitoExtension.class)
class PaymentOrchestratorTest {

  @Mock private PaymentRepository paymentRepository;

  private PaymentProvider paymentProvider;
  private PaymentOrchestrator paymentOrchestrator;

  @BeforeEach
  void setUp() {
    paymentProvider = new MockPaymentAdapter();
    paymentOrchestrator = new PaymentOrchestrator(paymentRepository, paymentProvider);
  }

  @Test
  void processPayment_success() {
    UUID sessionId = UUID.randomUUID();
    UUID customerId = UUID.randomUUID();
    UUID vehicleId = UUID.randomUUID();
    UUID chargePointId = UUID.randomUUID();
    BigDecimal amount = new BigDecimal("25.50");

    when(paymentRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
    when(paymentRepository.save(any(Payment.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Payment result =
        paymentOrchestrator.processPayment(
            sessionId, customerId, vehicleId, chargePointId, amount, "USD", null);

    assertThat(result).isNotNull();
    assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    assertThat(result.getAmount()).isEqualTo(amount);
    assertThat(result.getSessionId()).isEqualTo(sessionId);
    assertThat(result.getAttempts()).hasSize(2);
    verify(paymentRepository, atLeast(3)).save(any(Payment.class));
  }

  @Test
  void processPayment_idempotent() {
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
}
