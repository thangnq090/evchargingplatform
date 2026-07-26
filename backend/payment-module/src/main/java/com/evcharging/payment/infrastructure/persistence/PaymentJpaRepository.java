package com.evcharging.payment.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, UUID> {

  Optional<PaymentEntity> findBySessionId(UUID sessionId);

  Optional<PaymentEntity> findByIdempotencyKey(String idempotencyKey);
}
