package com.evcharging.station.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.evcharging.identity.VendorMarkupApi;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.station.domain.model.VendorView;
import com.evcharging.station.domain.repository.VendorRepository;

/**
 * Infrastructure adapter implementing the domain {@link VendorRepository} port.
 *
 * <p>Delegates read/write operations to the identity module's published {@link VendorMarkupApi}
 * interface. This avoids cross-schema database access (ADR-004) and maintains module boundaries
 * (ADR-003). When splitting to microservices, this adapter calls a gRPC/HTTP client instead.
 */
@Repository("stationVendorRepositoryAdapter")
public class VendorRepositoryAdapter implements VendorRepository {

  private final VendorMarkupApi identityVendorMarkupApi;

  public VendorRepositoryAdapter(VendorMarkupApi identityVendorMarkupApi) {
    this.identityVendorMarkupApi = identityVendorMarkupApi;
  }

  @Override
  public Optional<VendorView> findById(UUID vendorId) {
    return identityVendorMarkupApi
        .getMarkup(vendorId)
        .map(markup -> VendorView.reconstitute(vendorId, null, markup));
  }

  @Override
  public List<VendorView> findAll() {
    // For MVP, station module only needs individual vendor lookups
    // Full vendor listing is available through identity module
    throw new UnsupportedOperationException(
        "Not implemented — use identity module directly for vendor listing");
  }

  @Override
  public boolean existsByName(String name) {
    // Station module doesn't need this - identity owns vendor creation
    throw new UnsupportedOperationException(
        "Not implemented — use identity module for vendor existence checks");
  }

  @Override
  public VendorView updateMarkup(UUID vendorId, MarkupPercentage newMarkup) {
    MarkupPercentage updated =
        identityVendorMarkupApi.updateMarkup(vendorId, newMarkup.getBasisPoints(), vendorId);
    return VendorView.reconstitute(vendorId, null, updated);
  }
}
