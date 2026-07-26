package com.evcharging.payment.api.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.evcharging.payment.api.dto.PaymentResponse;
import com.evcharging.payment.domain.port.PaymentRepository;
import com.evcharging.shared.api.ApiResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentRepository paymentRepository;

  @GetMapping("/session/{sessionId}")
  public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentBySessionId(
      @PathVariable UUID sessionId) {
    return paymentRepository
        .findBySessionId(sessionId)
        .map(payment -> ResponseEntity.ok(ApiResponse.ok(PaymentResponse.from(payment))))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
