package com.evcharging.identity.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.shared.pagination.PaginatedList;

/** Domain port — persistence contract for Vendor aggregates. */
public interface VendorRepository {

  Vendor save(Vendor vendor);

  Optional<Vendor> findById(UUID id);

  Optional<Vendor> findByName(String name);

  /** Returns all vendors (unpaginated). */
  List<Vendor> findAll();

  /** Returns a cursor-paginated page of vendors, newest first. */
  PaginatedList<Vendor> findAll(int limit, UUID cursor);

  boolean existsByName(String name);
}
