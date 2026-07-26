package com.evcharging.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Payment {

  private final UUID id;
  private final UUID sessionId;
  private final UUID customerId;
  private final UUID vehicleId;
  private final UUID chargePointId;
  private final BigDecimal amount;
  private final String currency;
  private final UUID paymentMethodId;
  private final String idempotencyKey;
  private final Instant createdAt;

  private PaymentStatus status;
  private String providerPaymentId;
  private Instant updatedAt;
  private final List<PaymentAttempt> attempts = new ArrayList<>();

  public Payment(
      UUID id,
      UUID sessionId,
      UUID customerId,
      UUID vehicleId,
      UUID chargePointId,
      BigDecimal amount,
      String currency,
      UUID paymentMethodId,
      String idempotencyKey,
      Instant createdAt,
      Instant updatedAt) {
    this.id = id;
    this.sessionId = sessionId;
    this.customerId = customerId;
    this.vehicleId = vehicleId;
    this.chargePointId = chargePointId;
    this.amount = amount;
    this.currency = currency;
    this.paymentMethodId = paymentMethodId;
    this.idempotencyKey = idempotencyKey;
    this.status = PaymentStatus.PENDING;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }

  public void markAuthorized(String providerPaymentId) {
    this.providerPaymentId = providerPaymentId;
    this.status = PaymentStatus.AUTHORIZED;
    this.updatedAt = Instant.now();
  }

  public void markCaptured() {
    this.status = PaymentStatus.CAPTURED;
    this.updatedAt = Instant.now();
  }

  public void markFailed() {
    this.status = PaymentStatus.FAILED;
    this.updatedAt = Instant.now();
  }

  public void addAttempt(PaymentAttempt attempt) {
    this.attempts.add(attempt);
  }

  public UUID getId() {
    return id;
  }

  public UUID getSessionId() {
    return sessionId;
  }

  public UUID getCustomerId() {
    return customerId;
  }

  public UUID getVehicleId() {
    return vehicleId;
  }

  public UUID getChargePointId() {
    return chargePointId;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public String getCurrency() {
    return currency;
  }

  public UUID getPaymentMethodId() {
    return paymentMethodId;
  }

  public String getIdempotencyKey() {
    return idempotencyKey;
  }

  public PaymentStatus getStatus() {
    return status;
  }

  public String getProviderPaymentId() {
    return providerPaymentId;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public List<PaymentAttempt> getAttempts() {
    return Collections.unmodifiableList(attempts);
  }
}
