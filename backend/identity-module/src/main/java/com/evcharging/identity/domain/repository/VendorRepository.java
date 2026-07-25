package com.evcharging.identity.domain.repository;

import com.evcharging.identity.domain.model.Vendor;
import java.util.Optional;
import java.util.UUID;

/** Domain port — persistence contract for Vendor aggregates. */
public interface VendorRepository {

  Vendor save(Vendor vendor);

  Optional<Vendor> findById(UUID id);

  Optional<Vendor> findByName(String name);

  boolean existsByName(String name);
}
