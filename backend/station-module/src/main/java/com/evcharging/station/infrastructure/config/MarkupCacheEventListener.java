package com.evcharging.station.infrastructure.config;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.event.VendorMarkupChangedEvent;
import com.evcharging.station.domain.port.MarkupCachePort;

/** Event listener for cache invalidation. */
@Component
public class MarkupCacheEventListener {

  private final MarkupCachePort markupCachePort;

  public MarkupCacheEventListener(MarkupCachePort markupCachePort) {
    this.markupCachePort = markupCachePort;
  }

  @EventListener
  public void onVendorMarkupChanged(VendorMarkupChangedEvent event) {
    markupCachePort.evict(VendorId.of(event.vendorId()));
  }
}
