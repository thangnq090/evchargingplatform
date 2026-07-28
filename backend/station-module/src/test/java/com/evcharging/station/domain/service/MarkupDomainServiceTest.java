package com.evcharging.station.domain.service;

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

import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.model.VendorView;
import com.evcharging.station.domain.repository.VendorRepository;

@DisplayName("MarkupDomainService")
@ExtendWith(MockitoExtension.class)
class MarkupDomainServiceTest {

  @Mock private VendorRepository vendorRepository;

  private MarkupDomainService service;

  private static final UUID VENDOR_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    service = new MarkupDomainService(vendorRepository);
  }

  @Nested
  @DisplayName("setVendorMarkup")
  class SetVendorMarkup {

    @Test
    @DisplayName("sets markup successfully")
    void shouldSetMarkup() {
      VendorView vendor = VendorView.reconstitute(VENDOR_UUID, "GreenCharge", MarkupPercentage.zero());
      VendorView updated = VendorView.reconstitute(VENDOR_UUID, "GreenCharge", MarkupPercentage.ofBasisPoints(1500));
      given(vendorRepository.findById(VENDOR_UUID)).willReturn(Optional.of(vendor));
      given(vendorRepository.updateMarkup(VENDOR_UUID, MarkupPercentage.ofBasisPoints(1500))).willReturn(updated);

      VendorView result = service.setVendorMarkup(VendorId.of(VENDOR_UUID), 1500);

      assertThat(result.markupPercentage().getBasisPoints()).isEqualTo(1500);
      then(vendorRepository).should().updateMarkup(VENDOR_UUID, MarkupPercentage.ofBasisPoints(1500));
    }

    @Test
    @DisplayName("throws when markup is negative")
    void shouldThrowWhenNegative() {
      assertThatThrownBy(() -> service.setVendorMarkup(VendorId.of(VENDOR_UUID), -1))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("0 and 10000");
    }

    @Test
    @DisplayName("throws when markup exceeds 10000")
    void shouldThrowWhenExceedsMax() {
      assertThatThrownBy(() -> service.setVendorMarkup(VendorId.of(VENDOR_UUID), 10001))
          .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("throws when vendor not found")
    void shouldThrowWhenVendorNotFound() {
      given(vendorRepository.findById(VENDOR_UUID)).willReturn(Optional.empty());

      assertThatThrownBy(() -> service.setVendorMarkup(VendorId.of(VENDOR_UUID), 100))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Vendor not found");
    }
  }

  @Nested
  @DisplayName("getVendorMarkup")
  class GetVendorMarkup {

    @Test
    @DisplayName("returns markup for vendor")
    void shouldGetMarkup() {
      VendorView vendor = VendorView.reconstitute(VENDOR_UUID, "VC", MarkupPercentage.ofBasisPoints(2000));
      given(vendorRepository.findById(VENDOR_UUID)).willReturn(Optional.of(vendor));

      MarkupPercentage result = service.getVendorMarkup(VendorId.of(VENDOR_UUID));
      assertThat(result.getBasisPoints()).isEqualTo(2000);
    }

    @Test
    @DisplayName("returns zero markup when vendor not found")
    void shouldReturnZeroWhenVendorNotFound() {
      given(vendorRepository.findById(VENDOR_UUID)).willReturn(Optional.empty());

      MarkupPercentage result = service.getVendorMarkup(VendorId.of(VENDOR_UUID));
      assertThat(result.getBasisPoints()).isEqualTo(0);
    }
  }
}
