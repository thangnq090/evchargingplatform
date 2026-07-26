package com.evcharging.payment.infrastructure.persistence;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import com.evcharging.payment.domain.model.PaymentAttemptStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "payment_attempts", schema = "payment")
@Getter
@Setter
@NoArgsConstructor
public class PaymentAttemptEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private PaymentEntity payment;

  @Column(name = "attempt_number", nullable = false)
  private int attemptNumber;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private PaymentAttemptStatus status;

  @Column(name = "error_code", length = 50)
  private String errorCode;

  @Column(name = "error_message", columnDefinition = "TEXT")
  private String errorMessage;

  @Column(name = "attempted_at", nullable = false)
  private Instant attemptedAt;

  public PaymentAttemptEntity(
      UUID id,
      PaymentEntity payment,
      int attemptNumber,
      PaymentAttemptStatus status,
      String errorCode,
      String errorMessage,
      Instant attemptedAt) {
    this.id = id;
    this.payment = payment;
    this.attemptNumber = attemptNumber;
    this.status = status;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.attemptedAt = attemptedAt;
  }
}
