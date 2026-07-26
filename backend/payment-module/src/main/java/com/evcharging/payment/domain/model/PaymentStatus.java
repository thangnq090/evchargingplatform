package com.evcharging.payment.domain.model;

public enum PaymentStatus {
  PENDING,
  AUTHORIZED,
  CAPTURED,
  FAILED,
  VOIDED,
  REFUNDED
}
