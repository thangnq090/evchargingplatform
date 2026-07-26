package com.evcharging.payment.domain.port;

import java.util.Optional;
import java.util.UUID;

import com.evcharging.payment.domain.model.Payment;

public interface PaymentRepository {

  Payment save(Payment payment);

  Optional<Payment> findById(UUID id);

  Optional<Payment> findBySessionId(UUID sessionId);

  Optional<Payment> findByIdempotencyKey(String idempotencyKey);
}
