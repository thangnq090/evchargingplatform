package com.evcharging.identity.domain.repository;

import java.util.Optional;
import java.util.UUID;

import com.evcharging.identity.domain.model.User;

/** Domain port — persistence contract for User aggregates. */
public interface UserRepository {

  User save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  boolean existsByAccountNumber(String accountNumber);
}
