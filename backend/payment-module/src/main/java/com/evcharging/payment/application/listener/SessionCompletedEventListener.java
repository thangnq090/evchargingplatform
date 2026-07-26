package com.evcharging.payment.application.listener;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.evcharging.payment.application.service.PaymentOrchestrator;
import com.evcharging.session.application.events.SessionCompletedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCompletedEventListener {

  private final PaymentOrchestrator paymentOrchestrator;

  @EventListener
  public void onSessionCompleted(SessionCompletedEvent event) {
    log.info("Received SessionCompletedEvent for session {}", event.sessionId());
    try {
      paymentOrchestrator.processPayment(
          event.sessionId(),
          null,
          null,
          null,
          event.totalAmountValue(),
          event.totalAmountCurrency(),
          null);
    } catch (Exception e) {
      log.error("Error processing payment for completed session {}", event.sessionId(), e);
    }
  }
}
