package com.evcharging.payment.application.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.payment.domain.model.Payment;
import com.evcharging.payment.domain.model.PaymentAttempt;
import com.evcharging.payment.domain.model.PaymentAttemptStatus;
import com.evcharging.payment.domain.port.PaymentProvider;
import com.evcharging.payment.domain.port.PaymentProvider.PaymentResult;
import com.evcharging.payment.domain.port.PaymentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOrchestrator {

  private final PaymentRepository paymentRepository;
  private final PaymentProvider paymentProvider;

  @Transactional
  public Payment processPayment(
      UUID sessionId,
      UUID customerId,
      UUID vehicleId,
      UUID chargePointId,
      BigDecimal amount,
      String currency,
      UUID paymentMethodId) {

    String idempotencyKey = "session:" + sessionId + ":charge";

    Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
    if (existingPayment.isPresent()) {
      log.info("Payment with idempotency key {} already processed.", idempotencyKey);
      return existingPayment.get();
    }

    Instant now = Instant.now();
    Payment payment =
        new Payment(
            UUID.randomUUID(),
            sessionId,
            customerId != null ? customerId : UUID.randomUUID(),
            vehicleId,
            chargePointId,
            amount,
            currency != null ? currency : "USD",
            paymentMethodId,
            idempotencyKey,
            now,
            now);

    payment = paymentRepository.save(payment);

    // Step 1: Authorize
    log.info("Authorizing payment for session {}", sessionId);
    PaymentResult authResult =
        paymentProvider.authorize(
            payment.getCustomerId(),
            payment.getPaymentMethodId(),
            payment.getAmount(),
            payment.getCurrency(),
            idempotencyKey + ":auth");

    if (!authResult.success()) {
      payment.addAttempt(
          new PaymentAttempt(
              UUID.randomUUID(),
              1,
              PaymentAttemptStatus.FAILED,
              authResult.errorCode(),
              authResult.errorMessage(),
              Instant.now()));
      payment.markFailed();
      return paymentRepository.save(payment);
    }

    payment.addAttempt(
        new PaymentAttempt(
            UUID.randomUUID(),
            1,
            PaymentAttemptStatus.SUCCESS,
            null,
            "Authorization successful",
            Instant.now()));
    payment.markAuthorized(authResult.providerPaymentId());
    paymentRepository.save(payment);

    // Step 2: Capture
    log.info("Capturing payment for session {}", sessionId);
    PaymentResult captureResult =
        paymentProvider.capture(
            payment.getProviderPaymentId(),
            payment.getAmount(),
            payment.getCurrency(),
            idempotencyKey + ":capture");

    if (!captureResult.success()) {
      payment.addAttempt(
          new PaymentAttempt(
              UUID.randomUUID(),
              2,
              PaymentAttemptStatus.FAILED,
              captureResult.errorCode(),
              captureResult.errorMessage(),
              Instant.now()));
      log.warn(
          "Capture failed for session {}, initiating compensation (void authorization)", sessionId);
      paymentProvider.voidAuthorization(payment.getProviderPaymentId());
      payment.markFailed();
      return paymentRepository.save(payment);
    }

    payment.addAttempt(
        new PaymentAttempt(
            UUID.randomUUID(),
            2,
            PaymentAttemptStatus.SUCCESS,
            null,
            "Capture successful",
            Instant.now()));
    payment.markCaptured();
    return paymentRepository.save(payment);
  }
}
