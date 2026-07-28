package com.evcharging.station.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.event.VendorMarkupChangedEvent;
import com.evcharging.station.domain.model.VendorView;
import com.evcharging.station.domain.service.MarkupDomainService;

@DisplayName("MarkupApplicationService")
@ExtendWith(MockitoExtension.class)
class MarkupApplicationServiceTest {

  @Mock private MarkupDomainService domainService;
  @Mock private ApplicationEventPublisher eventPublisher;

  private MarkupApplicationService service;

  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final UUID ADMIN_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    service = new MarkupApplicationService(domainService, eventPublisher);
  }

  @Nested
  @DisplayName("setMarkup")
  class SetMarkup {

    @Test
    @DisplayName("sets markup and publishes VendorMarkupChangedEvent")
    void shouldSetMarkup() {
      VendorView oldView = VendorView.reconstitute(VENDOR_UUID, "VC", MarkupPercentage.zero());
      VendorView newView = VendorView.reconstitute(VENDOR_UUID, "VC", MarkupPercentage.ofBasisPoints(1500));
      given(domainService.getVendorMarkup(VendorId.of(VENDOR_UUID))).willReturn(MarkupPercentage.zero());
      given(domainService.setVendorMarkup(VendorId.of(VENDOR_UUID), 1500)).willReturn(newView);

      MarkupPercentage result = service.setMarkup(VendorId.of(VENDOR_UUID), 1500, ADMIN_UUID);

      assertThat(result.getBasisPoints()).isEqualTo(1500);
      ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
      then(eventPublisher).should().publishEvent(captor.capture());
      assertThat(captor.getValue()).isInstanceOf(VendorMarkupChangedEvent.class);
    }
  }

  @Nested
  @DisplayName("getMarkup")
  class GetMarkup {

    @Test
    @DisplayName("returns markup for vendor")
    void shouldGetMarkup() {
      given(domainService.getVendorMarkup(VendorId.of(VENDOR_UUID)))
          .willReturn(MarkupPercentage.ofBasisPoints(2000));

      MarkupPercentage result = service.getMarkup(VendorId.of(VENDOR_UUID));
      assertThat(result.getBasisPoints()).isEqualTo(2000);
    }
  }
}
