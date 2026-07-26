package com.evcharging.payment.domain.model;

import java.time.Instant;
import java.util.UUID;

public class PaymentAttempt {

  private final UUID id;
  private final int attemptNumber;
  private final PaymentAttemptStatus status;
  private final String errorCode;
  private final String errorMessage;
  private final Instant attemptedAt;

  public PaymentAttempt(
      UUID id,
      int attemptNumber,
      PaymentAttemptStatus status,
      String errorCode,
      String errorMessage,
      Instant attemptedAt) {
    this.id = id;
    this.attemptNumber = attemptNumber;
    this.status = status;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.attemptedAt = attemptedAt;
  }

  public UUID getId() {
    return id;
  }

  public int getAttemptNumber() {
    return attemptNumber;
  }

  public PaymentAttemptStatus getStatus() {
    return status;
  }

  public String getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public Instant getAttemptedAt() {
    return attemptedAt;
  }
}
