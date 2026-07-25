package com.evcharging.identity.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.identity.VendorMarkupApi;
import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.repository.VendorRepository;
import com.evcharging.shared.kernel.MarkupPercentage;

/**
 * Implementation of {@link VendorMarkupApi} for inter-module access.
 *
 * <p>Owned by identity module — station module calls this through the published interface.
 */
@Service
@Transactional
public class VendorMarkupApplicationService implements VendorMarkupApi {

  private final VendorRepository vendorRepository;

  public VendorMarkupApplicationService(VendorRepository vendorRepository) {
    this.vendorRepository = vendorRepository;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<MarkupPercentage> getMarkup(UUID vendorId) {
    return vendorRepository.findById(vendorId).map(Vendor::getMarkupPercentage);
  }

  @Override
  public MarkupPercentage updateMarkup(UUID vendorId, int markupBasisPoints, UUID changedBy) {
    Vendor vendor =
        vendorRepository
            .findById(vendorId)
            .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

    MarkupPercentage newMarkup = MarkupPercentage.ofBasisPoints(markupBasisPoints);
    vendor.setMarkupPercentage(newMarkup);
    vendorRepository.save(vendor);

    // TODO: publish VendorMarkupChangedEvent for cross-module cache invalidation
    return newMarkup;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<String> getVendorName(UUID vendorId) {
    return vendorRepository.findById(vendorId).map(Vendor::getName);
  }
}
