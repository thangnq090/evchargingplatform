package com.evcharging.payment.application.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.payment.application.service.PaymentOrchestrator;
import com.evcharging.session.application.events.SessionCompletedEvent;

@DisplayName("SessionCompletedEventListener")
@ExtendWith(MockitoExtension.class)
class SessionCompletedEventListenerTest {

  @Mock private PaymentOrchestrator orchestrator;

  private SessionCompletedEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new SessionCompletedEventListener(orchestrator);
  }

  @Nested
  @DisplayName("onSessionCompleted")
  class OnSessionCompleted {

    @Test
    @DisplayName("processes payment for completed session")
    void shouldProcessPayment() {
      UUID sessionId = UUID.randomUUID();
      SessionCompletedEvent event =
          new SessionCompletedEvent(
              sessionId, Instant.now(), new BigDecimal("50.0"),
              new BigDecimal("25.00"), "EUR");

      listener.onSessionCompleted(event);

      then(orchestrator).should().processPayment(
          eq(sessionId), isNull(), isNull(), isNull(),
          eq(new BigDecimal("25.00")), eq("EUR"), isNull());
    }

    @Test
    @DisplayName("does not propagate exceptions")
    void shouldNotPropagateExceptions() {
      UUID sessionId = UUID.randomUUID();
      SessionCompletedEvent event =
          new SessionCompletedEvent(
              sessionId, Instant.now(), new BigDecimal("10.0"),
              new BigDecimal("5.00"), "USD");

      willThrow(new RuntimeException("payment error"))
          .given(orchestrator).processPayment(
              any(), any(), any(), any(), any(), any(), any());

      // Should not throw
      listener.onSessionCompleted(event);
    }
  }
}
