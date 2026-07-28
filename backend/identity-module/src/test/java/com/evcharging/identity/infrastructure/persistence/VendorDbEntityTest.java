package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;

import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.model.VendorStatus;
import com.evcharging.shared.kernel.MarkupPercentage;

@DisplayName("VendorDbEntity")
class VendorDbEntityTest {

  private Vendor createVendor() {
    return Vendor.reconstitute(
        java.util.UUID.randomUUID(), "ACME Charging", VendorStatus.ACTIVE,
        MarkupPercentage.ofBasisPoints(500), Instant.now(), Instant.now());
  }

  @Nested
  @DisplayName("from")
  class From {

    @Test
    @DisplayName("round-trips domain to entity and back")
    void shouldRoundTrip() {
      Vendor vendor = createVendor();

      VendorDbEntity entity = VendorDbEntity.from(vendor, true);
      Vendor domain = entity.toDomain();

      assertThat(domain.getId()).isEqualTo(vendor.getId());
      assertThat(domain.getName()).isEqualTo("ACME Charging");
      assertThat(domain.getStatus()).isEqualTo(VendorStatus.ACTIVE);
      assertThat(domain.getMarkupPercentage().getBasisPoints()).isEqualTo(500);
      assertThat(entity.isNew()).isTrue();
    }

    @Test
    @DisplayName("marks as not new")
    void shouldMarkAsNotNew() {
      VendorDbEntity entity = VendorDbEntity.from(createVendor(), false);
      assertThat(entity.isNew()).isFalse();
    }
  }
}
