package com.evcharging.station.domain.service;

import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.kernel.VendorId;
import com.evcharging.station.domain.model.VendorView;
import com.evcharging.station.domain.repository.VendorRepository;

/**
 * Domain service for vendor markup operations.
 *
 * <p>Pure Java — no Spring annotations. Instantiated via configuration.
 */
public class MarkupDomainService {

  private final VendorRepository vendorRepository;

  public MarkupDomainService(VendorRepository vendorRepository) {
    this.vendorRepository = vendorRepository;
  }

  /**
   * Sets the markup percentage for a vendor. Markup is stored as basis points (1 BP = 0.01%).
   *
   * @param vendorId vendor to update
   * @param markupBasisPoints markup in basis points (0-10000)
   * @return the new VendorView
   * @throws IllegalArgumentException if vendor not found or markup out of range
   */
  public VendorView setVendorMarkup(VendorId vendorId, int markupBasisPoints) {
    if (markupBasisPoints < 0 || markupBasisPoints > 10000) {
      throw new IllegalArgumentException(
          "Markup must be between 0 and 10000 basis points (0% - 100%)");
    }

    // Verify vendor exists
    VendorView existing =
        vendorRepository
            .findById(vendorId.getValue())
            .orElseThrow(() -> new IllegalArgumentException("Vendor not found: " + vendorId));

    MarkupPercentage newMarkup = MarkupPercentage.ofBasisPoints(markupBasisPoints);
    return vendorRepository.updateMarkup(vendorId.getValue(), newMarkup);
  }

  /** Gets the effective markup percentage for a vendor. */
  public MarkupPercentage getVendorMarkup(VendorId vendorId) {
    return vendorRepository
        .findById(vendorId.getValue())
        .map(VendorView::markupPercentage)
        .orElse(MarkupPercentage.zero());
  }
}
