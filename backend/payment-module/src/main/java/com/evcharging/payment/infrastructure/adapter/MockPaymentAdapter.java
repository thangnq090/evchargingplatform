package com.evcharging.payment.infrastructure.adapter;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.evcharging.payment.domain.port.PaymentProvider;

@Component
public class MockPaymentAdapter implements PaymentProvider {

  @Override
  public PaymentResult authorize(
      UUID customerId,
      UUID paymentMethodId,
      BigDecimal amount,
      String currency,
      String idempotencyKey) {
    String mockProviderId = "mock_auth_" + UUID.randomUUID().toString().substring(0, 8);
    return PaymentResult.success(mockProviderId);
  }

  @Override
  public PaymentResult capture(
      String providerPaymentId, BigDecimal amount, String currency, String idempotencyKey) {
    String mockCaptureId =
        providerPaymentId != null
            ? providerPaymentId
            : "mock_cap_" + UUID.randomUUID().toString().substring(0, 8);
    return PaymentResult.success(mockCaptureId);
  }

  @Override
  public PaymentResult voidAuthorization(String providerPaymentId) {
    return PaymentResult.success(providerPaymentId);
  }

  @Override
  public PaymentResult refund(
      String providerPaymentId, BigDecimal amount, String currency, String reason) {
    return PaymentResult.success(providerPaymentId);
  }
}
