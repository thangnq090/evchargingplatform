package com.evcharging.identity.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.repository.VendorRepository;
import com.evcharging.shared.pagination.PaginatedList;

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
  public List<Vendor> findAll() {
    return jpa.findAll().stream().map(VendorDbEntity::toDomain).toList();
  }

  @Override
  public PaginatedList<Vendor> findAll(int limit, UUID cursor) {
    int clamped = Math.min(Math.max(limit, 1), 100);
    var page =
        cursor != null
            ? jpa.findByIdLessThanOrderByCreatedAtDesc(cursor, PageRequest.of(0, clamped + 1))
            : jpa.findByOrderByCreatedAtDesc(PageRequest.of(0, clamped + 1));
    List<Vendor> vendors = page.stream().map(VendorDbEntity::toDomain).toList();
    boolean hasMore = vendors.size() > clamped;
    List<Vendor> items = hasMore ? vendors.subList(0, clamped) : vendors;
    UUID lastId = items.isEmpty() ? null : items.get(items.size() - 1).getId();
    return PaginatedList.of(items, clamped, lastId, hasMore);
  }

  @Override
  public boolean existsByName(String name) {
    return jpa.existsByName(name);
  }
}
