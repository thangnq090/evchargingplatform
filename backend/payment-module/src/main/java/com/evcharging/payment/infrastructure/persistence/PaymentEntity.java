package com.evcharging.payment.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import com.evcharging.payment.domain.model.PaymentStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payments", schema = "payment")
@Getter
@Setter
@NoArgsConstructor
public class PaymentEntity {

  @Id private UUID id;

  @Column(name = "session_id", nullable = false, unique = true)
  private UUID sessionId;

  @Column(name = "customer_id", nullable = false)
  private UUID customerId;

  @Column(name = "vehicle_id")
  private UUID vehicleId;

  @Column(name = "charge_point_id")
  private UUID chargePointId;

  @Column(name = "amount", nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private PaymentStatus status;

  @Column(name = "payment_method_id")
  private UUID paymentMethodId;

  @Column(name = "provider_payment_id", length = 100)
  private String providerPaymentId;

  @Column(name = "idempotency_key", nullable = false, unique = true, length = 150)
  private String idempotencyKey;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PaymentAttemptEntity> attempts = new ArrayList<>();

  public void addAttempt(PaymentAttemptEntity attempt) {
    attempts.add(attempt);
    attempt.setPayment(this);
  }
}
