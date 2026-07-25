package com.evcharging.station.domain.model;

import java.util.UUID;

import com.evcharging.shared.kernel.MarkupPercentage;

/**
 * Read-only projection of a Vendor for use within the station module.
 *
 * <p>This avoids a cross-module domain dependency on identity's Vendor aggregate. The full Vendor
 * is owned by the identity module; station only needs ID, name, and markup.
 */
public record VendorView(UUID id, String name, MarkupPercentage markupPercentage) {

  /** Reconstitute a VendorView from data. */
  public static VendorView reconstitute(UUID id, String name, MarkupPercentage markupPercentage) {
    return new VendorView(id, name, markupPercentage);
  }
}
