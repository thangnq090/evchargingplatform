package com.evcharging.payment.infrastructure.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.payment.domain.port.PaymentProvider.PaymentResult;

@DisplayName("MockPaymentAdapter")
class MockPaymentAdapterTest {

  private MockPaymentAdapter adapter;

  @BeforeEach
  void setUp() {
    adapter = new MockPaymentAdapter();
  }

  @Nested
  @DisplayName("authorize")
  class Authorize {

    @Test
    @DisplayName("returns success with provider id")
    void shouldReturnSuccess() {
      PaymentResult result =
          adapter.authorize(
              UUID.randomUUID(), UUID.randomUUID(), new BigDecimal("10.00"), "EUR", "key-1");

      assertThat(result.success()).isTrue();
      assertThat(result.providerPaymentId()).startsWith("mock_auth_");
      assertThat(result.errorCode()).isNull();
      assertThat(result.errorMessage()).isNull();
    }
  }

  @Nested
  @DisplayName("capture")
  class Capture {

    @Test
    @DisplayName("returns success with provider id")
    void shouldReturnSuccess() {
      PaymentResult result =
          adapter.capture("mock_auth_123", new BigDecimal("10.00"), "EUR", "key-1");

      assertThat(result.success()).isTrue();
      assertThat(result.providerPaymentId()).isEqualTo("mock_auth_123");
    }

    @Test
    @DisplayName("generates mock id when null provider id")
    void shouldGenerateMockId() {
      PaymentResult result =
          adapter.capture(null, new BigDecimal("10.00"), "EUR", "key-1");

      assertThat(result.success()).isTrue();
      assertThat(result.providerPaymentId()).startsWith("mock_cap_");
    }
  }

  @Nested
  @DisplayName("voidAuthorization")
  class VoidAuthorization {

    @Test
    @DisplayName("returns success")
    void shouldReturnSuccess() {
      PaymentResult result = adapter.voidAuthorization("mock_auth_123");

      assertThat(result.success()).isTrue();
      assertThat(result.providerPaymentId()).isEqualTo("mock_auth_123");
    }
  }

  @Nested
  @DisplayName("refund")
  class Refund {

    @Test
    @DisplayName("returns success")
    void shouldReturnSuccess() {
      PaymentResult result =
          adapter.refund("mock_auth_123", new BigDecimal("10.00"), "EUR", "customer request");

      assertThat(result.success()).isTrue();
      assertThat(result.providerPaymentId()).isEqualTo("mock_auth_123");
    }
  }
}
