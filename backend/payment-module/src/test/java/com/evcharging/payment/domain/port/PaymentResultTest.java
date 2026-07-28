package com.evcharging.payment.domain.port;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.evcharging.payment.domain.port.PaymentProvider.PaymentResult;

@DisplayName("PaymentResult")
class PaymentResultTest {

  @Test
  @DisplayName("success factory creates success result")
  void shouldCreateSuccess() {
    PaymentResult result = PaymentResult.success("prov_123");

    assertThat(result.success()).isTrue();
    assertThat(result.providerPaymentId()).isEqualTo("prov_123");
    assertThat(result.errorCode()).isNull();
    assertThat(result.errorMessage()).isNull();
  }

  @Test
  @DisplayName("failure factory creates failure result")
  void shouldCreateFailure() {
    PaymentResult result = PaymentResult.failure("ERR_500", "Declined");

    assertThat(result.success()).isFalse();
    assertThat(result.providerPaymentId()).isNull();
    assertThat(result.errorCode()).isEqualTo("ERR_500");
    assertThat(result.errorMessage()).isEqualTo("Declined");
  }
}
