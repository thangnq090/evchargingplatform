package com.evcharging.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA interface for {@link UserDbEntity}. Package-private. */
interface SpringDataUserRepository extends JpaRepository<UserDbEntity, UUID> {

  Optional<UserDbEntity> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByAccountNumber(String accountNumber);

  java.util.List<UserDbEntity> findByVendorId(UUID vendorId);

  java.util.List<UserDbEntity> findByRole(com.evcharging.identity.domain.model.Role role);
}
