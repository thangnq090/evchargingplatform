package com.evcharging.station.infrastructure.cache;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.evcharging.shared.kernel.VendorId;
import com.github.benmanes.caffeine.cache.Caffeine;

@DisplayName("InMemoryMarkupCacheAdapter")
class InMemoryMarkupCacheAdapterTest {

  private InMemoryMarkupCacheAdapter cache;

  private static final VendorId VENDOR_ID = VendorId.generate();

  @BeforeEach
  void setUp() {
    cache = new InMemoryMarkupCacheAdapter(Caffeine.newBuilder().maximumSize(100).build());
  }

  @Nested
  @DisplayName("getMarkupBasisPoints")
  class GetMarkupBasisPoints {

    @Test
    @DisplayName("returns empty when not cached")
    void shouldReturnEmptyWhenNotCached() {
      assertThat(cache.getMarkupBasisPoints(VENDOR_ID)).isEmpty();
    }

    @Test
    @DisplayName("returns cached value")
    void shouldReturnCachedValue() {
      cache.putMarkupBasisPoints(VENDOR_ID, 1500);
      assertThat(cache.getMarkupBasisPoints(VENDOR_ID)).hasValue(1500);
    }
  }

  @Nested
  @DisplayName("putMarkupBasisPoints")
  class PutMarkupBasisPoints {

    @Test
    @DisplayName("stores markup value")
    void shouldStoreMarkup() {
      cache.putMarkupBasisPoints(VENDOR_ID, 2000);
      assertThat(cache.getMarkupBasisPoints(VENDOR_ID)).hasValue(2000);
    }
  }

  @Nested
  @DisplayName("evict")
  class Evict {

    @Test
    @DisplayName("removes cached markup")
    void shouldEvictMarkup() {
      cache.putMarkupBasisPoints(VENDOR_ID, 1500);
      cache.evict(VENDOR_ID);
      assertThat(cache.getMarkupBasisPoints(VENDOR_ID)).isEmpty();
    }
  }
}
