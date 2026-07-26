package com.evcharging.payment.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.payment.domain.model.Payment;
import com.evcharging.payment.domain.model.PaymentAttempt;
import com.evcharging.payment.domain.port.PaymentRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepository {

  private final PaymentJpaRepository jpaRepository;

  @Override
  public Payment save(Payment payment) {
    PaymentEntity entity =
        jpaRepository.findById(payment.getId()).orElseGet(() -> toEntity(payment));

    updateEntity(entity, payment);
    PaymentEntity saved = jpaRepository.save(entity);
    return toDomain(saved);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Payment> findById(UUID id) {
    return jpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Payment> findBySessionId(UUID sessionId) {
    return jpaRepository.findBySessionId(sessionId).map(this::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
    return jpaRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
  }

  private PaymentEntity toEntity(Payment domain) {
    PaymentEntity entity = new PaymentEntity();
    entity.setId(domain.getId());
    entity.setSessionId(domain.getSessionId());
    entity.setCustomerId(domain.getCustomerId());
    entity.setVehicleId(domain.getVehicleId());
    entity.setChargePointId(domain.getChargePointId());
    entity.setAmount(domain.getAmount());
    entity.setCurrency(domain.getCurrency());
    entity.setStatus(domain.getStatus());
    entity.setPaymentMethodId(domain.getPaymentMethodId());
    entity.setProviderPaymentId(domain.getProviderPaymentId());
    entity.setIdempotencyKey(domain.getIdempotencyKey());
    entity.setCreatedAt(domain.getCreatedAt());
    entity.setUpdatedAt(domain.getUpdatedAt());
    return entity;
  }

  private void updateEntity(PaymentEntity entity, Payment domain) {
    entity.setStatus(domain.getStatus());
    entity.setProviderPaymentId(domain.getProviderPaymentId());
    entity.setUpdatedAt(domain.getUpdatedAt());

    // sync attempts
    entity.getAttempts().clear();
    for (PaymentAttempt attempt : domain.getAttempts()) {
      PaymentAttemptEntity attemptEntity =
          new PaymentAttemptEntity(
              attempt.getId(),
              entity,
              attempt.getAttemptNumber(),
              attempt.getStatus(),
              attempt.getErrorCode(),
              attempt.getErrorMessage(),
              attempt.getAttemptedAt());
      entity.addAttempt(attemptEntity);
    }
  }

  private Payment toDomain(PaymentEntity entity) {
    Payment domain =
        new Payment(
            entity.getId(),
            entity.getSessionId(),
            entity.getCustomerId(),
            entity.getVehicleId(),
            entity.getChargePointId(),
            entity.getAmount(),
            entity.getCurrency(),
            entity.getPaymentMethodId(),
            entity.getIdempotencyKey(),
            entity.getCreatedAt(),
            entity.getUpdatedAt());
    if (entity.getStatus() == com.evcharging.payment.domain.model.PaymentStatus.AUTHORIZED
        || entity.getStatus() == com.evcharging.payment.domain.model.PaymentStatus.CAPTURED) {
      domain.markAuthorized(entity.getProviderPaymentId());
    }
    if (entity.getStatus() == com.evcharging.payment.domain.model.PaymentStatus.CAPTURED) {
      domain.markCaptured();
    }
    if (entity.getStatus() == com.evcharging.payment.domain.model.PaymentStatus.FAILED) {
      domain.markFailed();
    }
    for (PaymentAttemptEntity a : entity.getAttempts()) {
      domain.addAttempt(
          new PaymentAttempt(
              a.getId(),
              a.getAttemptNumber(),
              a.getStatus(),
              a.getErrorCode(),
              a.getErrorMessage(),
              a.getAttemptedAt()));
    }
    return domain;
  }
}
