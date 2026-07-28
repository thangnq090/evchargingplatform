package com.evcharging.identity.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.repository.VendorRepository;
import com.evcharging.shared.kernel.MarkupPercentage;

@DisplayName("VendorMarkupApplicationService")
@ExtendWith(MockitoExtension.class)
class VendorMarkupApplicationServiceTest {

  @Mock private VendorRepository vendorRepository;

  private VendorMarkupApplicationService service;

  @BeforeEach
  void setUp() {
    service = new VendorMarkupApplicationService(vendorRepository);
  }

  @Nested
  @DisplayName("getMarkup")
  class GetMarkup {

    @Test
    @DisplayName("returns markup when vendor found")
    void shouldReturnMarkup() {
      UUID vendorId = UUID.randomUUID();
      Vendor vendor = Vendor.create("Test Vendor");
      given(vendorRepository.findById(vendorId)).willReturn(Optional.of(vendor));

      Optional<MarkupPercentage> result = service.getMarkup(vendorId);

      assertThat(result).isPresent();
    }

    @Test
    @DisplayName("returns empty when vendor not found")
    void shouldReturnEmpty() {
      UUID vendorId = UUID.randomUUID();
      given(vendorRepository.findById(vendorId)).willReturn(Optional.empty());

      Optional<MarkupPercentage> result = service.getMarkup(vendorId);

      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("updateMarkup")
  class UpdateMarkup {

    @Test
    @DisplayName("updates markup for vendor")
    void shouldUpdateMarkup() {
      UUID vendorId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      Vendor vendor = Vendor.create("Test Vendor");
      given(vendorRepository.findById(vendorId)).willReturn(Optional.of(vendor));

      MarkupPercentage result = service.updateMarkup(vendorId, 1500, adminId);

      assertThat(result.getBasisPoints()).isEqualTo(1500);
      then(vendorRepository).should().save(vendor);
    }

    @Test
    @DisplayName("throws when vendor not found")
    void shouldThrowWhenNotFound() {
      UUID vendorId = UUID.randomUUID();
      given(vendorRepository.findById(vendorId)).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.updateMarkup(vendorId, 1500, UUID.randomUUID()))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Vendor not found");
    }
  }

  @Nested
  @DisplayName("getVendorName")
  class GetVendorName {

    @Test
    @DisplayName("returns vendor name when found")
    void shouldReturnName() {
      UUID vendorId = UUID.randomUUID();
      Vendor vendor = Vendor.create("ACME Charging");
      given(vendorRepository.findById(vendorId)).willReturn(Optional.of(vendor));

      Optional<String> result = service.getVendorName(vendorId);

      assertThat(result).isPresent();
      assertThat(result.get()).isEqualTo("ACME Charging");
    }

    @Test
    @DisplayName("returns empty when vendor not found")
    void shouldReturnEmpty() {
      UUID vendorId = UUID.randomUUID();
      given(vendorRepository.findById(vendorId)).willReturn(Optional.empty());

      assertThat(service.getVendorName(vendorId)).isEmpty();
    }
  }
}
