package com.evcharging.billing.application.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.evcharging.billing.application.service.BillingApplicationService;
import com.evcharging.session.application.events.SessionCompletedEvent;

/** Listener that captures SessionCompletedEvent to trigger invoicing. */
@Component("billingSessionCompletedEventListener")
public class SessionCompletedEventListener {

  private final BillingApplicationService billingApplicationService;

  public SessionCompletedEventListener(BillingApplicationService billingApplicationService) {
    this.billingApplicationService = billingApplicationService;
  }

  /**
   * Handles session completion events. Listens after commit to ensure session transactional
   * boundary is complete before starting the billing workflow.
   */
  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void onSessionCompleted(SessionCompletedEvent event) {
    billingApplicationService.generateInvoice(event.sessionId());
  }
}
