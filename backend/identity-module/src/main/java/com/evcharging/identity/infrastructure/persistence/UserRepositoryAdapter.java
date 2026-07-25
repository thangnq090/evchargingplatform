package com.evcharging.identity.infrastructure.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.repository.UserRepository;

/**
 * Infrastructure adapter implementing the domain {@link UserRepository} port via Spring Data JPA.
 */
@Repository
class UserRepositoryAdapter implements UserRepository {

  private final SpringDataUserRepository jpa;

  UserRepositoryAdapter(SpringDataUserRepository jpa) {
    this.jpa = jpa;
  }

  @Override
  public User save(User user) {
    boolean isNew = !jpa.existsById(user.getId());
    UserDbEntity entity = UserDbEntity.from(user, isNew);
    UserDbEntity saved = jpa.save(entity);
    return saved.toDomain();
  }

  @Override
  public Optional<User> findById(UUID id) {
    return jpa.findById(id).map(UserDbEntity::toDomain);
  }

  @Override
  public Optional<User> findByEmail(String email) {
    return jpa.findByEmail(email).map(UserDbEntity::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {
    return jpa.existsByEmail(email);
  }

  @Override
  public boolean existsByAccountNumber(String accountNumber) {
    return jpa.existsByAccountNumber(accountNumber);
  }

  @Override
  public java.util.List<User> findAllByVendorId(UUID vendorId) {
    return jpa.findByVendorId(vendorId).stream()
        .map(UserDbEntity::toDomain)
        .collect(java.util.stream.Collectors.toList());
  }

  @Override
  public java.util.List<User> findAllByRole(com.evcharging.identity.domain.model.Role role) {
    return jpa.findByRole(role).stream()
        .map(UserDbEntity::toDomain)
        .collect(java.util.stream.Collectors.toList());
  }
}
