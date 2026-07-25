package com.evcharging.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.repository.VendorRepository;

/**
 * Infrastructure adapter implementing the domain {@link VendorRepository} port via Spring Data JPA.
 */
@Repository("identityVendorRepositoryAdapter")
class VendorRepositoryAdapter implements VendorRepository {

  private final SpringDataVendorRepository jpa;

  VendorRepositoryAdapter(SpringDataVendorRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public Vendor save(Vendor vendor) {
    boolean isNew = !jpa.existsById(vendor.getId());
    VendorDbEntity entity = VendorDbEntity.from(vendor, isNew);
    VendorDbEntity saved = jpa.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<Vendor> findById(UUID id) {
    return jpa.findById(id).map(VendorDbEntity::toDomain);
  }

  @Override
  public Optional<Vendor> findByName(String name) {
    return jpa.findByName(name).map(VendorDbEntity::toDomain);
  }

  @Override
  public boolean existsByName(String name) {
    return jpa.existsByName(name);
  }
}
