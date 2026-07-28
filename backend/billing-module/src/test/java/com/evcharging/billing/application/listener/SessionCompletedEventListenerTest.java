package com.evcharging.billing.application.listener;

import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.billing.application.service.BillingApplicationService;
import com.evcharging.session.application.events.SessionCompletedEvent;

@DisplayName("SessionCompletedEventListener")
@ExtendWith(MockitoExtension.class)
class SessionCompletedEventListenerTest {

  @Mock private BillingApplicationService billingApplicationService;

  private SessionCompletedEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new SessionCompletedEventListener(billingApplicationService);
  }

  @Test
  @DisplayName("delegates to billing service")
  void shouldDelegateToBillingService() {
    UUID sessionId = UUID.randomUUID();
    SessionCompletedEvent event = new SessionCompletedEvent(
        sessionId, Instant.now(), null, null, "EUR");

    listener.onSessionCompleted(event);

    then(billingApplicationService).should().generateInvoice(sessionId);
  }
}
