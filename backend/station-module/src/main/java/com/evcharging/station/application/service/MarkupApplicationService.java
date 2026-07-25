package com.evcharging.station.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.service.MarkupDomainService;

/** Application service for vendor markup use cases. */
@Service
@Transactional
public class MarkupApplicationService {

  private final MarkupDomainService domainService;
  private final ApplicationEventPublisher eventPublisher;

  public MarkupApplicationService(
      MarkupDomainService domainService, ApplicationEventPublisher eventPublisher) {
    this.domainService = domainService;
    this.eventPublisher = eventPublisher;
  }

  /** Sets the markup percentage for a vendor (admin only). */
  public MarkupPercentage setMarkup(VendorId vendorId, int markupBasisPoints, UUID adminId) {
    // Read old markup before update for event payload
    MarkupPercentage oldMarkup = domainService.getVendorMarkup(vendorId);

    // Perform the update
    MarkupPercentage newMarkup =
        domainService.setVendorMarkup(vendorId, markupBasisPoints).markupPercentage();

    // Publish event for cache invalidation and audit
    eventPublisher.publishEvent(
        new com.evcharging.station.domain.event.VendorMarkupChangedEvent(
            vendorId.getValue(),
            oldMarkup.getBasisPoints(),
            newMarkup.getBasisPoints(),
            adminId,
            Instant.now()));

    return newMarkup;
  }

  /** Gets the current markup for a vendor. */
  @Transactional(readOnly = true)
  public MarkupPercentage getMarkup(VendorId vendorId) {
    return domainService.getVendorMarkup(vendorId);
  }
}
