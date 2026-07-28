package com.evcharging.identity.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.MarkupPercentage;

@DisplayName("Vendor (additional)")
class VendorAdditionalTest {

  @Nested
  @DisplayName("reconstitute")
  class Reconstitute {

    @Test
    @DisplayName("reconstitutes from persistence")
    void shouldReconstitute() {
      UUID id = UUID.randomUUID();
      Instant now = Instant.now();
      MarkupPercentage markup = MarkupPercentage.ofBasisPoints(1500);

      Vendor vendor = Vendor.reconstitute(
          id, "Test Vendor", VendorStatus.SUSPENDED, markup, now, now);

      assertThat(vendor.getId()).isEqualTo(id);
      assertThat(vendor.getName()).isEqualTo("Test Vendor");
      assertThat(vendor.getStatus()).isEqualTo(VendorStatus.SUSPENDED);
      assertThat(vendor.getMarkupPercentage().getBasisPoints()).isEqualTo(1500);
      assertThat(vendor.getCreatedAt()).isEqualTo(now);
      assertThat(vendor.getUpdatedAt()).isEqualTo(now);
    }
  }

  @Nested
  @DisplayName("suspend")
  class Suspend {

    @Test
    @DisplayName("suspends active vendor")
    void shouldSuspendActiveVendor() {
      Vendor vendor = Vendor.create("Test");

      vendor.suspend();

      assertThat(vendor.getStatus()).isEqualTo(VendorStatus.SUSPENDED);
    }

    @Test
    @DisplayName("throws when already suspended")
    void shouldThrowWhenAlreadySuspended() {
      Vendor vendor = Vendor.create("Test");
      vendor.suspend();

      assertThatThrownBy(vendor::suspend)
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("ACTIVE");
    }
  }

  @Nested
  @DisplayName("setMarkupPercentage")
  class SetMarkupPercentage {

    @Test
    @DisplayName("updates markup and timestamp")
    void shouldUpdateMarkup() {
      Vendor vendor = Vendor.create("Test");
      Instant beforeUpdate = vendor.getUpdatedAt();

      vendor.setMarkupPercentage(MarkupPercentage.ofBasisPoints(2000));

      assertThat(vendor.getMarkupPercentage().getBasisPoints()).isEqualTo(2000);
      assertThat(vendor.getUpdatedAt()).isAfterOrEqualTo(beforeUpdate);
    }
  }
}
