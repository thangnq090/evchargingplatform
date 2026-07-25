package com.evcharging.identity;

import java.util.Optional;
import java.util.UUID;

import com.evcharging.shared.kernel.MarkupPercentage;

/**
 * Published interface for the station module to access and update vendor markup.
 *
 * <p>This is the inter-module contract per ADR-005. Station module depends on this interface,
 * identity module owns the implementation and data. This avoids cross-schema table access.
 *
 * <p>When splitting into microservices, this becomes a gRPC/HTTP API call.
 */
public interface VendorMarkupApi {

  /** Returns the current markup percentage for a vendor. */
  Optional<MarkupPercentage> getMarkup(UUID vendorId);

  /** Returns the name of a vendor. */
  Optional<String> getVendorName(UUID vendorId);

  /**
   * Updates the markup percentage for a vendor.
   *
   * @return the new markup percentage
   * @throws IllegalArgumentException if vendor not found
   */
  MarkupPercentage updateMarkup(UUID vendorId, int markupBasisPoints, UUID changedBy);
}
