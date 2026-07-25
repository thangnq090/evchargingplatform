package com.evcharging.identity.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.evcharging.identity.domain.model.Vendor;

/** Domain port — persistence contract for Vendor aggregates. */
public interface VendorRepository {

  Vendor save(Vendor vendor);

  Optional<Vendor> findById(UUID id);

  Optional<Vendor> findByName(String name);

  boolean existsByName(String name);
}
