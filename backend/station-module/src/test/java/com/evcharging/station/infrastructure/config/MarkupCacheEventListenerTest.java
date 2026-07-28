package com.evcharging.station.infrastructure.config;

import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.event.VendorMarkupChangedEvent;
import com.evcharging.station.domain.port.MarkupCachePort;

@DisplayName("MarkupCacheEventListener")
@ExtendWith(MockitoExtension.class)
class MarkupCacheEventListenerTest {

  @Mock private MarkupCachePort markupCachePort;

  private MarkupCacheEventListener listener;

  @BeforeEach
  void setUp() {
    listener = new MarkupCacheEventListener(markupCachePort);
  }

  @Test
  @DisplayName("evicts cache when markup changes")
  void shouldEvictCacheOnMarkupChanged() {
    UUID vendorId = UUID.randomUUID();
    VendorMarkupChangedEvent event =
        new VendorMarkupChangedEvent(vendorId, 1000, 1500, UUID.randomUUID(), Instant.now());

    listener.onVendorMarkupChanged(event);

    then(markupCachePort).should().evict(VendorId.of(vendorId));
  }
}
