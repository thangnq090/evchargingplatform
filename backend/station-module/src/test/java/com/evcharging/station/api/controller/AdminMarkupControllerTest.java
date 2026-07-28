package com.evcharging.station.api.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.shared.security.SecurityUtils;
import com.evcharging.station.application.service.MarkupApplicationService;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("AdminMarkupController")
@ExtendWith(MockitoExtension.class)
class AdminMarkupControllerTest {

  @Mock private MarkupApplicationService service;

  private AdminMarkupController controller;

  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final UUID ADMIN_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller = new AdminMarkupController(service);
  }

  @Nested
  @DisplayName("getMarkup")
  class GetMarkup {

    @Test
    @DisplayName("returns markup for vendor")
    void shouldReturnMarkup() {
      MarkupPercentage markup = MarkupPercentage.ofBasisPoints(500);
      given(service.getMarkup(VendorId.of(VENDOR_UUID))).willReturn(markup);

      StepVerifier.create(controller.getMarkup(VENDOR_UUID))
          .assertNext(response -> {
            assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
            ApiResponse<AdminMarkupController.MarkupResponse> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.success()).isTrue();
            assertThat(body.data().markupBasisPoints()).isEqualTo(500);
          })
          .verifyComplete();
    }

    @Test
    @DisplayName("returns 404 when vendor not found")
    void shouldReturn404WhenNotFound() {
      given(service.getMarkup(VendorId.of(VENDOR_UUID)))
          .willThrow(new IllegalArgumentException("Vendor not found"));

      StepVerifier.create(controller.getMarkup(VENDOR_UUID))
          .expectError(IllegalArgumentException.class)
          .verify();
    }
  }

  @Nested
  @DisplayName("setMarkup")
  class SetMarkup {

    @Test
    @DisplayName("sets markup for vendor as admin")
    void shouldSetMarkup() {
      MarkupPercentage newMarkup = MarkupPercentage.ofBasisPoints(1500);
      given(service.setMarkup(VendorId.of(VENDOR_UUID), 1500, ADMIN_UUID)).willReturn(newMarkup);

      try (MockedStatic<SecurityUtils> mockedStatic = mockStatic(SecurityUtils.class)) {
        mockedStatic
            .when(SecurityUtils::getReactiveUserId)
            .thenReturn(Mono.just(ADMIN_UUID));

        AdminMarkupController.SetMarkupRequest request =
            new AdminMarkupController.SetMarkupRequest(1500);

        StepVerifier.create(controller.setMarkup(VENDOR_UUID, request))
            .assertNext(response -> {
              assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
              ApiResponse<AdminMarkupController.MarkupResponse> body = response.getBody();
              assertThat(body).isNotNull();
              assertThat(body.success()).isTrue();
              assertThat(body.data().markupBasisPoints()).isEqualTo(1500);
            })
            .verifyComplete();
      }
    }
  }
}
