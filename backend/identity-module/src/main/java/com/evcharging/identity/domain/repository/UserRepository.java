package com.evcharging.identity.domain.repository;

import com.evcharging.identity.domain.model.User;
import java.util.Optional;
import java.util.UUID;

/** Domain port — persistence contract for User aggregates. */
public interface UserRepository {

  User save(User user);

  Optional<User> findById(UUID id);

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}
