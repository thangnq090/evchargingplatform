package com.evcharging.station.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.station.domain.model.VendorView;

/**
 * Port for accessing Vendor data from the station module.
 *
 * <p>Returns a read-only {@link VendorView} projection to avoid cross-module domain dependency on
 * identity's Vendor aggregate. Markup updates are coordinated through this port as a write
 * operation.
 */
public interface VendorRepository {

  /** Finds a vendor by ID. */
  Optional<VendorView> findById(UUID vendorId);

  /** Finds all vendors. */
  List<VendorView> findAll();

  /** Checks if a vendor with the given name exists. */
  boolean existsByName(String name);

  /**
   * Updates the markup percentage for a vendor.
   *
   * @param vendorId target vendor
   * @param newMarkup new markup value
   * @return updated VendorView
   */
  VendorView updateMarkup(UUID vendorId, MarkupPercentage newMarkup);
}
