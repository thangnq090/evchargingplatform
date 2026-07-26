package com.evcharging.payment.domain.port;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentProvider {

  PaymentResult authorize(
      UUID customerId,
      UUID paymentMethodId,
      BigDecimal amount,
      String currency,
      String idempotencyKey);

  PaymentResult capture(
      String providerPaymentId, BigDecimal amount, String currency, String idempotencyKey);

  PaymentResult voidAuthorization(String providerPaymentId);

  PaymentResult refund(String providerPaymentId, BigDecimal amount, String currency, String reason);

  record PaymentResult(
      boolean success, String providerPaymentId, String errorCode, String errorMessage) {
    public static PaymentResult success(String providerPaymentId) {
      return new PaymentResult(true, providerPaymentId, null, null);
    }

    public static PaymentResult failure(String errorCode, String errorMessage) {
      return new PaymentResult(false, null, errorCode, errorMessage);
    }
  }
}
