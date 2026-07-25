package com.evcharging.station.domain.port;

import java.util.Optional;

import com.evcharging.shared.kernel.VendorId;

/**
 * Port for caching vendor markup percentages.
 *
 * <p>Implementations can use in-memory cache (MVP) or Redis (distributed). The application layer
 * depends on this port, not on any specific cache technology.
 */
public interface MarkupCachePort {

  /** Gets cached markup basis points for a vendor. */
  Optional<Integer> getMarkupBasisPoints(VendorId vendorId);

  /** Caches markup basis points for a vendor. */
  void putMarkupBasisPoints(VendorId vendorId, int markupBasisPoints);

  /** Evicts cached markup for a vendor. */
  void evict(VendorId vendorId);
}
