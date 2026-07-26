package com.evcharging.identity.application.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/** Response payload for {@code GET /api/v1/identity/vendors}. */
public record VendorListResponse(List<VendorSummary> vendors) {

  public record VendorSummary(
      UUID id,
      String name,
      String status,
      int markupBasisPoints,
      Instant createdAt,
      Instant updatedAt) {}
}
