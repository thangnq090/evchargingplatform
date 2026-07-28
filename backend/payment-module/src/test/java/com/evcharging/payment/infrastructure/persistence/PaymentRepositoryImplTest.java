package com.evcharging.payment.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
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

import com.evcharging.payment.domain.model.Payment;
import com.evcharging.payment.domain.model.PaymentAttempt;
import com.evcharging.payment.domain.model.PaymentAttemptStatus;
import com.evcharging.payment.domain.model.PaymentStatus;

@DisplayName("PaymentRepositoryImpl")
@ExtendWith(MockitoExtension.class)
class PaymentRepositoryImplTest {

  @Mock private PaymentJpaRepository jpa;

  private PaymentRepositoryImpl adapter;

  @BeforeEach
  void setUp() {
    adapter = new PaymentRepositoryImpl(jpa);
  }

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
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves new payment")
    void shouldSaveNewPayment() {
      Payment payment = createPayment();
      given(jpa.findById(payment.getId())).willReturn(Optional.empty());
      given(jpa.save(any(PaymentEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Payment result = adapter.save(payment);

      assertThat(result).isNotNull();
      assertThat(result.getSessionId()).isEqualTo(payment.getSessionId());
      then(jpa).should().save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("updates existing payment")
    void shouldUpdateExistingPayment() {
      Payment payment = createPayment();
      PaymentEntity existingEntity = new PaymentEntity();
      existingEntity.setId(payment.getId());
      existingEntity.setSessionId(payment.getSessionId());
      existingEntity.setCustomerId(payment.getCustomerId());
      existingEntity.setAmount(payment.getAmount());
      existingEntity.setCurrency(payment.getCurrency());
      existingEntity.setStatus(PaymentStatus.PENDING);
      existingEntity.setIdempotencyKey(payment.getIdempotencyKey());
      existingEntity.setCreatedAt(payment.getCreatedAt());
      existingEntity.setUpdatedAt(payment.getUpdatedAt());

      given(jpa.findById(payment.getId())).willReturn(Optional.of(existingEntity));
      given(jpa.save(any(PaymentEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Payment result = adapter.save(payment);

      assertThat(result).isNotNull();
      then(jpa).should().save(any(PaymentEntity.class));
    }

    @Test
    @DisplayName("saves payment with attempts")
    void shouldSavePaymentWithAttempts() {
      Payment payment = createPayment();
      payment.markAuthorized("prov_auth_123");

      payment.addAttempt(
          new PaymentAttempt(UUID.randomUUID(), 1, PaymentAttemptStatus.SUCCESS, null, "Auth OK", Instant.now()));

      given(jpa.findById(payment.getId())).willReturn(Optional.empty());
      given(jpa.save(any(PaymentEntity.class))).willAnswer(inv -> inv.getArgument(0));

      Payment result = adapter.save(payment);

      assertThat(result).isNotNull();
      assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns payment when found")
    void shouldReturnPayment() {
      UUID id = UUID.randomUUID();
      PaymentEntity entity = createEntity(id);

      given(jpa.findById(id)).willReturn(Optional.of(entity));

      Optional<Payment> result = adapter.findById(id);
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(id);
      assertThat(result.get().getSessionId()).isEqualTo(entity.getSessionId());
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findById(any(UUID.class))).willReturn(Optional.empty());
      assertThat(adapter.findById(UUID.randomUUID())).isEmpty();
    }
  }

  @Nested
  @DisplayName("findBySessionId")
  class FindBySessionId {

    @Test
    @DisplayName("returns payment by session id")
    void shouldFindBySessionId() {
      UUID sessionId = UUID.randomUUID();
      PaymentEntity entity = createEntity(UUID.randomUUID());
      entity.setSessionId(sessionId);

      given(jpa.findBySessionId(sessionId)).willReturn(Optional.of(entity));

      Optional<Payment> result = adapter.findBySessionId(sessionId);
      assertThat(result).isPresent();
      assertThat(result.get().getSessionId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("returns empty when session not found")
    void shouldReturnEmpty() {
      given(jpa.findBySessionId(any(UUID.class))).willReturn(Optional.empty());
      assertThat(adapter.findBySessionId(UUID.randomUUID())).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByIdempotencyKey")
  class FindByIdempotencyKey {

    @Test
    @DisplayName("returns payment by idempotency key")
    void shouldFindByIdempotencyKey() {
      PaymentEntity entity = createEntity(UUID.randomUUID());
      entity.setIdempotencyKey("key-123");

      given(jpa.findByIdempotencyKey("key-123")).willReturn(Optional.of(entity));

      Optional<Payment> result = adapter.findByIdempotencyKey("key-123");
      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when key not found")
    void shouldReturnEmpty() {
      given(jpa.findByIdempotencyKey("unknown")).willReturn(Optional.empty());
      assertThat(adapter.findByIdempotencyKey("unknown")).isEmpty();
    }
  }

  @Nested
  @DisplayName("toDomain conversions")
  class ToDomainConversions {

    @Test
    @DisplayName("converts AUTHORIZED status")
    void shouldConvertAuthorized() {
      PaymentEntity entity = createEntity(UUID.randomUUID());
      entity.setStatus(PaymentStatus.AUTHORIZED);
      entity.setProviderPaymentId("prov_123");

      given(jpa.findById(entity.getId())).willReturn(Optional.of(entity));

      Payment result = adapter.findById(entity.getId()).get();
      assertThat(result.getStatus()).isEqualTo(PaymentStatus.AUTHORIZED);
      assertThat(result.getProviderPaymentId()).isEqualTo("prov_123");
    }

    @Test
    @DisplayName("converts CAPTURED status")
    void shouldConvertCaptured() {
      PaymentEntity entity = createEntity(UUID.randomUUID());
      entity.setStatus(PaymentStatus.CAPTURED);
      entity.setProviderPaymentId("prov_123");

      given(jpa.findById(entity.getId())).willReturn(Optional.of(entity));

      Payment result = adapter.findById(entity.getId()).get();
      assertThat(result.getStatus()).isEqualTo(PaymentStatus.CAPTURED);
    }

    @Test
    @DisplayName("converts FAILED status")
    void shouldConvertFailed() {
      PaymentEntity entity = createEntity(UUID.randomUUID());
      entity.setStatus(PaymentStatus.FAILED);

      given(jpa.findById(entity.getId())).willReturn(Optional.of(entity));

      Payment result = adapter.findById(entity.getId()).get();
      assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    @DisplayName("converts payment with attempts")
    void shouldConvertWithAttempts() {
      UUID paymentId = UUID.randomUUID();
      PaymentEntity entity = createEntity(paymentId);
      entity.setStatus(PaymentStatus.CAPTURED);
      entity.setProviderPaymentId("prov_123");

      PaymentAttemptEntity attempt =
          new PaymentAttemptEntity(
              UUID.randomUUID(),
              entity,
              1,
              PaymentAttemptStatus.SUCCESS,
              null,
              "Auth OK",
              Instant.now());
      entity.getAttempts().add(attempt);

      given(jpa.findById(paymentId)).willReturn(Optional.of(entity));

      Payment result = adapter.findById(paymentId).get();
      assertThat(result.getAttempts()).hasSize(1);
      assertThat(result.getAttempts().get(0).getStatus()).isEqualTo(PaymentAttemptStatus.SUCCESS);
    }
  }

  private PaymentEntity createEntity(UUID id) {
    PaymentEntity entity = new PaymentEntity();
    entity.setId(id);
    entity.setSessionId(UUID.randomUUID());
    entity.setCustomerId(UUID.randomUUID());
    entity.setAmount(new BigDecimal("25.00"));
    entity.setCurrency("EUR");
    entity.setStatus(PaymentStatus.PENDING);
    entity.setIdempotencyKey("idemp-123");
    entity.setCreatedAt(Instant.now());
    entity.setUpdatedAt(Instant.now());
    return entity;
  }
}
