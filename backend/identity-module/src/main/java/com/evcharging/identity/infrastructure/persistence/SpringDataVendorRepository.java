package com.evcharging.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA interface for {@link VendorDbEntity}. Package-private. */
interface SpringDataVendorRepository extends JpaRepository<VendorDbEntity, UUID> {

  Optional<VendorDbEntity> findByName(String name);

  boolean existsByName(String name);
}
