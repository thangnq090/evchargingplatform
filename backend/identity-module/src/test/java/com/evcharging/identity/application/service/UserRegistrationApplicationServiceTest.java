package com.evcharging.identity.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.evcharging.identity.application.dto.AcceptInvitationRequest;
import com.evcharging.identity.application.dto.CreateVendorRequest;
import com.evcharging.identity.application.dto.CreateVendorResponse;
import com.evcharging.identity.application.dto.RegisterAdminRequest;
import com.evcharging.identity.application.dto.RegisterCustomerRequest;
import com.evcharging.identity.application.dto.UserResponse;
import com.evcharging.identity.domain.model.*;
import com.evcharging.identity.domain.repository.InvitationRepository;
import com.evcharging.identity.domain.repository.UserRepository;
import com.evcharging.identity.domain.repository.VendorRepository;
import com.evcharging.shared.kernel.MarkupPercentage;

@DisplayName("UserRegistrationApplicationService Tests")
@ExtendWith(MockitoExtension.class)
class UserRegistrationApplicationServiceTest {

  @Mock UserRepository userRepository;
  @Mock VendorRepository vendorRepository;
  @Mock InvitationRepository invitationRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock ApplicationEventPublisher eventPublisher;

  UserRegistrationApplicationService service;

  @BeforeEach
  void setUp() {
    service =
        new UserRegistrationApplicationService(
            userRepository,
            vendorRepository,
            invitationRepository,
            passwordEncoder,
            eventPublisher);
  }

  @Nested
  @DisplayName("registerAdmin")
  class RegisterAdmin {

    @Test
    @DisplayName("saves admin and publishes event when email is new")
    void shouldRegisterAdminSuccessfully() {
      RegisterAdminRequest req = new RegisterAdminRequest("Alice", "alice@example.com", "password");
      given(userRepository.existsByEmail("alice@example.com")).willReturn(false);
      given(passwordEncoder.encode("password")).willReturn("$bcrypt");

      User saved =
          User.reconstitute(
              UUID.randomUUID(),
              "Alice",
              "alice@example.com",
              "$bcrypt",
              Role.ADMIN,
              null,
              UserStatus.ACTIVE,
              Instant.now(),
              Instant.now());
      given(userRepository.save(any(User.class))).willReturn(saved);

      UserResponse result = service.registerAdmin(req);

      assertThat(result.email()).isEqualTo("alice@example.com");
      assertThat(result.role()).isEqualTo(Role.ADMIN);
      then(eventPublisher).should().publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("throws when email is already registered")
    void shouldThrowWhenEmailExists() {
      RegisterAdminRequest req = new RegisterAdminRequest("Alice", "alice@example.com", "password");
      given(userRepository.existsByEmail("alice@example.com")).willReturn(true);

      assertThatThrownBy(() -> service.registerAdmin(req))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("already registered");
    }
  }

  @Nested
  @DisplayName("registerCustomer")
  class RegisterCustomer {

    @Test
    @DisplayName("registers customer with auto-generated account number successfully")
    void shouldRegisterCustomerSuccessfully() {
      RegisterCustomerRequest req =
          new RegisterCustomerRequest("Dave", "dave@example.com", "password123", "+123456789");
      given(userRepository.existsByEmail("dave@example.com")).willReturn(false);
      given(userRepository.existsByAccountNumber(anyString())).willReturn(false);
      given(passwordEncoder.encode("password123")).willReturn("$bcrypt");

      User saved =
          User.reconstitute(
              UUID.randomUUID(),
              "Dave",
              "dave@example.com",
              "$bcrypt",
              "+123456789",
              Role.CUSTOMER,
              null,
              "ACC-A1B2C3D4",
              UserStatus.ACTIVE,
              Instant.now(),
              Instant.now());
      given(userRepository.save(any(User.class))).willReturn(saved);

      UserResponse result = service.registerCustomer(req);

      assertThat(result.email()).isEqualTo("dave@example.com");
      assertThat(result.role()).isEqualTo(Role.CUSTOMER);
      assertThat(result.accountNumber()).isEqualTo("ACC-A1B2C3D4");
      assertThat(result.phone()).isEqualTo("+123456789");
      then(eventPublisher).should().publishEvent(any(Object.class));
    }
  }

  @Nested
  @DisplayName("createVendorWithAdmin")
  class CreateVendorWithAdmin {

    @Test
    @DisplayName("creates vendor and invitation successfully")
    void shouldCreateVendorAndInvitation() {
      CreateVendorRequest req = new CreateVendorRequest("ACME Corp", "Bob", "bob@acme.com");

      given(vendorRepository.existsByName("ACME Corp")).willReturn(false);
      given(userRepository.existsByEmail("bob@acme.com")).willReturn(false);

      UUID vendorId = UUID.randomUUID();
      Vendor savedVendor =
          Vendor.reconstitute(
              vendorId,
              "ACME Corp",
              VendorStatus.ACTIVE,
              MarkupPercentage.zero(),
              Instant.now(),
              Instant.now());
      given(vendorRepository.save(any(Vendor.class))).willReturn(savedVendor);

      Invitation savedInvitation =
          Invitation.reconstitute(
              UUID.randomUUID(),
              "bob@acme.com",
              vendorId,
              Role.VENDOR_ADMIN,
              "secure-token",
              Instant.now().plus(48, ChronoUnit.HOURS),
              InvitationStatus.PENDING,
              Instant.now());
      given(invitationRepository.save(any(Invitation.class))).willReturn(savedInvitation);

      CreateVendorResponse result = service.createVendorWithAdmin(req);

      assertThat(result.vendorId()).isEqualTo(vendorId);
      assertThat(result.vendorName()).isEqualTo("ACME Corp");
      assertThat(result.invitedEmail()).isEqualTo("bob@acme.com");
      then(eventPublisher).should(times(2)).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("throws when vendor name already exists")
    void shouldThrowWhenVendorNameExists() {
      given(vendorRepository.existsByName("Existing")).willReturn(true);
      assertThatThrownBy(
              () ->
                  service.createVendorWithAdmin(
                      new CreateVendorRequest("Existing", "x", "x@x.com")))
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("already exists");
    }
  }

  @Nested
  @DisplayName("acceptInvitation")
  class AcceptInvitation {

    @Test
    @DisplayName("registers invited user when token is valid")
    void shouldAcceptValidInvitation() {
      UUID vendorId = UUID.randomUUID();
      Invitation invitation =
          Invitation.reconstitute(
              UUID.randomUUID(),
              "bob@acme.com",
              vendorId,
              Role.VENDOR_ADMIN,
              "valid-tok",
              Instant.now().plus(24, ChronoUnit.HOURS),
              InvitationStatus.PENDING,
              Instant.now());

      given(invitationRepository.findByToken("valid-tok")).willReturn(Optional.of(invitation));
      given(userRepository.existsByEmail("bob@acme.com")).willReturn(false);
      given(passwordEncoder.encode(any())).willReturn("$bcrypt");

      User saved =
          User.reconstitute(
              UUID.randomUUID(),
              "Bob",
              "bob@acme.com",
              "$bcrypt",
              Role.VENDOR_ADMIN,
              vendorId,
              UserStatus.ACTIVE,
              Instant.now(),
              Instant.now());
      given(userRepository.save(any())).willReturn(saved);
      given(invitationRepository.save(any())).willReturn(invitation);

      UserResponse result =
          service.acceptInvitation(new AcceptInvitationRequest("valid-tok", "Bob", "secret123"));

      assertThat(result.email()).isEqualTo("bob@acme.com");
      assertThat(result.role()).isEqualTo(Role.VENDOR_ADMIN);
    }

    @Test
    @DisplayName("throws when token is not found")
    void shouldThrowWhenTokenNotFound() {
      given(invitationRepository.findByToken("bad-tok")).willReturn(Optional.empty());

      assertThatThrownBy(
              () ->
                  service.acceptInvitation(
                      new AcceptInvitationRequest("bad-tok", "Bob", "secret123")))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Invalid invitation token");
    }
  }
}
