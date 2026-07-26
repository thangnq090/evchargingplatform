package com.evcharging.payment.api.dto;

import java.util.List;
import java.util.UUID;

import com.evcharging.payment.domain.model.Payment;

public record PaymentResponse(
    UUID id,
    UUID sessionId,
    UUID customerId,
    UUID vehicleId,
    UUID chargePointId,
    String amount,
    String currency,
    String status,
    String providerPaymentId,
    String idempotencyKey,
    List<PaymentAttemptResponse> attempts) {
  public static PaymentResponse from(Payment payment) {
    return new PaymentResponse(
        payment.getId(),
        payment.getSessionId(),
        payment.getCustomerId(),
        payment.getVehicleId(),
        payment.getChargePointId(),
        payment.getAmount().toPlainString(),
        payment.getCurrency(),
        payment.getStatus().name(),
        payment.getProviderPaymentId(),
        payment.getIdempotencyKey(),
        payment.getAttempts().stream()
            .map(
                a ->
                    new PaymentAttemptResponse(
                        a.getId(),
                        a.getAttemptNumber(),
                        a.getStatus().name(),
                        a.getErrorCode(),
                        a.getErrorMessage()))
            .toList());
  }
}
