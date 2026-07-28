package com.evcharging.identity.infrastructure.persistence;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;

@DisplayName("UserRepositoryAdapter")
@ExtendWith(MockitoExtension.class)
class UserRepositoryAdapterTest {

  @Mock private SpringDataUserRepository jpa;

  private UserRepositoryAdapter adapter;

  private static final UUID USER_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    adapter = new UserRepositoryAdapter(jpa);
  }

  private User createUser() {
    return User.reconstitute(
        USER_UUID, "Test User", "test@test.com", "$2a$hash", "1234567890",
        Role.CUSTOMER, null, null, UserStatus.ACTIVE, false,
        Instant.now(), Instant.now());
  }

  private UserDbEntity createEntity() {
    return UserDbEntity.from(createUser(), true);
  }

  @Nested
  @DisplayName("save")
  class Save {

    @Test
    @DisplayName("saves new user")
    void shouldSaveNewUser() {
      given(jpa.existsById(USER_UUID)).willReturn(false);
      given(jpa.save(any(UserDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      User result = adapter.save(createUser());

      assertThat(result).isNotNull();
      then(jpa).should().save(any(UserDbEntity.class));
    }

    @Test
    @DisplayName("updates existing user")
    void shouldUpdateExistingUser() {
      given(jpa.existsById(USER_UUID)).willReturn(true);
      given(jpa.save(any(UserDbEntity.class))).willAnswer(inv -> inv.getArgument(0));

      User result = adapter.save(createUser());

      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("findById")
  class FindById {

    @Test
    @DisplayName("returns user when found")
    void shouldReturnUser() {
      given(jpa.findById(USER_UUID)).willReturn(Optional.of(createEntity()));

      Optional<User> result = adapter.findById(USER_UUID);
      assertThat(result).isPresent();
      assertThat(result.get().getId()).isEqualTo(USER_UUID);
    }

    @Test
    @DisplayName("returns empty when not found")
    void shouldReturnEmpty() {
      given(jpa.findById(USER_UUID)).willReturn(Optional.empty());

      assertThat(adapter.findById(USER_UUID)).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByEmail")
  class FindByEmail {

    @Test
    @DisplayName("delegates to JPA")
    void shouldDelegate() {
      given(jpa.findByEmail("test@test.com")).willReturn(Optional.of(createEntity()));

      Optional<User> result = adapter.findByEmail("test@test.com");
      assertThat(result).isPresent();
    }
  }

  @Nested
  @DisplayName("existsByEmail")
  class ExistsByEmail {

    @Test
    @DisplayName("delegates to JPA")
    void shouldDelegate() {
      given(jpa.existsByEmail("test@test.com")).willReturn(true);

      assertThat(adapter.existsByEmail("test@test.com")).isTrue();
    }
  }

  @Nested
  @DisplayName("existsByAccountNumber")
  class ExistsByAccountNumber {

    @Test
    @DisplayName("delegates to JPA")
    void shouldDelegate() {
      given(jpa.existsByAccountNumber("CUST-001")).willReturn(true);

      assertThat(adapter.existsByAccountNumber("CUST-001")).isTrue();
    }
  }

  @Nested
  @DisplayName("findAll")
  class FindAll {

    @Test
    @DisplayName("returns all users")
    void shouldReturnAll() {
      given(jpa.findAll()).willReturn(List.of(createEntity()));

      List<User> result = adapter.findAll();
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("findAllByVendorId")
  class FindAllByVendorId {

    @Test
    @DisplayName("returns users for vendor")
    void shouldReturnUsersForVendor() {
      UUID vendorId = UUID.randomUUID();
      given(jpa.findByVendorId(vendorId)).willReturn(List.of(createEntity()));

      List<User> result = adapter.findAllByVendorId(vendorId);
      assertThat(result).hasSize(1);
    }
  }

  @Nested
  @DisplayName("findAllByRole")
  class FindAllByRole {

    @Test
    @DisplayName("returns users by role")
    void shouldReturnUsersByRole() {
      given(jpa.findByRole(Role.ADMIN)).willReturn(List.of(createEntity()));

      List<User> result = adapter.findAllByRole(Role.ADMIN);
      assertThat(result).hasSize(1);
    }
  }
}
