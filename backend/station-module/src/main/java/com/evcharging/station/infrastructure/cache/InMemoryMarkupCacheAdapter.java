package com.evcharging.station.infrastructure.cache;

import java.util.Optional;
import java.util.UUID;

import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.port.MarkupCachePort;
import com.github.benmanes.caffeine.cache.Cache;

/**
 * In-memory cache adapter for vendor markup (MVP).
 *
 * <p>Bean is provided by {@link com.evcharging.station.config.StationInfrastructureConfig}. Can be
 * swapped with a Redis-based adapter by adding the Redis dependency and config.
 */
public class InMemoryMarkupCacheAdapter implements MarkupCachePort {

  private final Cache<UUID, Integer> cache;

  public InMemoryMarkupCacheAdapter(Cache<UUID, Integer> cache) {
    this.cache = cache;
  }

  @Override
  public Optional<Integer> getMarkupBasisPoints(VendorId vendorId) {
    return Optional.ofNullable(cache.getIfPresent(vendorId.getValue()));
  }

  @Override
  public void putMarkupBasisPoints(VendorId vendorId, int markupBasisPoints) {
    cache.put(vendorId.getValue(), markupBasisPoints);
  }

  @Override
  public void evict(VendorId vendorId) {
    cache.invalidate(vendorId.getValue());
  }
}
