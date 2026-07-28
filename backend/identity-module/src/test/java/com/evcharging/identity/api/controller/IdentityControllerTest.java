package com.evcharging.identity.api.controller;

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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.evcharging.identity.application.dto.AcceptInvitationRequest;
import com.evcharging.identity.application.dto.AddVendorUserRequest;
import com.evcharging.identity.application.dto.ChangePasswordRequest;
import com.evcharging.identity.application.dto.CreateVendorRequest;
import com.evcharging.identity.application.dto.CreateVendorResponse;
import com.evcharging.identity.application.dto.LoginRequest;
import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.dto.PasswordResetResponse;
import com.evcharging.identity.application.dto.RefreshTokenRequest;
import com.evcharging.identity.application.dto.RegisterAdminRequest;
import com.evcharging.identity.application.dto.RegisterCustomerRequest;
import com.evcharging.identity.application.dto.UserResponse;
import com.evcharging.identity.application.service.AuthenticationApplicationService;
import com.evcharging.identity.application.service.CredentialManagementApplicationService;
import com.evcharging.identity.application.service.RefreshTokenApplicationService;
import com.evcharging.identity.application.service.UserRegistrationApplicationService;
import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.UserStatus;
import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.model.VendorStatus;
import com.evcharging.identity.domain.repository.UserRepository;
import com.evcharging.identity.domain.repository.VendorRepository;
import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.kernel.MarkupPercentage;
import com.evcharging.shared.pagination.PaginatedList;
import com.evcharging.shared.security.SecurityUtils;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@DisplayName("IdentityController")
@ExtendWith(MockitoExtension.class)
class IdentityControllerTest {

  @Mock private UserRegistrationApplicationService registrationService;
  @Mock private AuthenticationApplicationService authenticationService;
  @Mock private CredentialManagementApplicationService credentialService;
  @Mock private RefreshTokenApplicationService refreshTokenService;
  @Mock private VendorRepository vendorRepository;
  @Mock private UserRepository userRepository;

  private IdentityController controller;
  private static final UUID USER_UUID = UUID.randomUUID();
  private static final UUID VENDOR_UUID = UUID.randomUUID();
  private static final UUID ADMIN_UUID = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    controller =
        new IdentityController(
            registrationService, authenticationService, credentialService,
            refreshTokenService, vendorRepository, userRepository);
  }

  private UserResponse createUserResponse() {
    return new UserResponse(
        USER_UUID, "Test User", "test@test.com", "1234567890",
        Role.CUSTOMER, null, null, UserStatus.ACTIVE, Instant.now());
  }

  @Nested
  @DisplayName("registerAdmin")
  class RegisterAdmin {

    @Test
    @DisplayName("registers admin and returns 201")
    void shouldRegisterAdmin() {
      RegisterAdminRequest request = new RegisterAdminRequest("Admin", "admin@test.com", "password123");
      given(registrationService.registerAdmin(request)).willReturn(createUserResponse());

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(ADMIN_UUID));

        StepVerifier.create(controller.registerAdmin(request))
            .assertNext(res -> {
              assertThat(res.getStatusCode().value()).isEqualTo(201);
              assertThat(res.getHeaders().getLocation()).isNotNull();
              ApiResponse<?> body = res.getBody();
              assertThat(body).isNotNull();
              assertThat(body.success()).isTrue();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("registerCustomer")
  class RegisterCustomer {

    @Test
    @DisplayName("registers customer and returns 201")
    void shouldRegisterCustomer() {
      RegisterCustomerRequest request =
          new RegisterCustomerRequest("John", "john@test.com", "password123", "1234567890");
      given(registrationService.registerCustomer(request)).willReturn(createUserResponse());

      StepVerifier.create(controller.registerCustomer(request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().value()).isEqualTo(201);
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("returns login response")
    void shouldLogin() {
      LoginRequest request = new LoginRequest("test@test.com", "password123");
      LoginResponse loginResponse =
          new LoginResponse("jwt-token", 900, USER_UUID, "CUSTOMER", null, "refresh", false);
      given(authenticationService.login(request)).willReturn(loginResponse);

      StepVerifier.create(controller.login(request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            ApiResponse<?> body = res.getBody();
            assertThat(body).isNotNull();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("acceptInvitation")
  class AcceptInvitation {

    @Test
    @DisplayName("accepts invitation and returns 201")
    void shouldAcceptInvitation() {
      AcceptInvitationRequest request =
          new AcceptInvitationRequest("token123", "John", "password123");
      given(registrationService.acceptInvitation(request)).willReturn(createUserResponse());

      StepVerifier.create(controller.acceptInvitation(request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().value()).isEqualTo(201);
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("createVendor")
  class CreateVendor {

    @Test
    @DisplayName("creates vendor and returns 201")
    void shouldCreateVendor() {
      CreateVendorRequest request =
          new CreateVendorRequest("ACME", "Admin", "admin@acme.com");
      CreateVendorResponse response =
          new CreateVendorResponse(VENDOR_UUID, "ACME", UUID.randomUUID(), "token", "admin@acme.com");
      given(registrationService.createVendorWithAdmin(request)).willReturn(response);

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(ADMIN_UUID));

        StepVerifier.create(controller.createVendor(request))
            .assertNext(res -> {
              assertThat(res.getStatusCode().value()).isEqualTo(201);
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("listVendors")
  class ListVendors {

    @Test
    @DisplayName("returns paginated vendor list")
    void shouldListVendors() {
      Vendor vendor = Vendor.reconstitute(
          VENDOR_UUID, "ACME", VendorStatus.ACTIVE,
          MarkupPercentage.zero(), Instant.now(), Instant.now());
      PaginatedList<Vendor> page = PaginatedList.of(List.of(vendor), 20, null, false);
      given(vendorRepository.findAll(20, null)).willReturn(page);

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(ADMIN_UUID));

        StepVerifier.create(controller.listVendors(20, null))
            .assertNext(res -> {
              assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
              ApiResponse<?> body = res.getBody();
              assertThat(body).isNotNull();
            })
            .verifyComplete();
      }
    }

    @Test
    @DisplayName("decodes cursor")
    void shouldDecodeCursor() {
      UUID cursorId = UUID.randomUUID();
      PaginatedList<Vendor> page = PaginatedList.of(List.of(), 20, null, false);
      given(vendorRepository.findAll(20, cursorId)).willReturn(page);

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(ADMIN_UUID));

        String encodedCursor = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(cursorId.toString().getBytes());

        StepVerifier.create(controller.listVendors(20, encodedCursor))
            .assertNext(res -> {
              assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("listUsers")
  class ListUsers {

    @Test
    @DisplayName("returns all users")
    void shouldListUsers() {
      User user = User.reconstitute(
          USER_UUID, "Test", "test@test.com", "$2a$hash", null, Role.CUSTOMER,
          null, null, UserStatus.ACTIVE, false, Instant.now(), Instant.now());
      given(userRepository.findAll()).willReturn(List.of(user));

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(ADMIN_UUID));

        StepVerifier.create(controller.listUsers())
            .assertNext(res -> {
              assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
              ApiResponse<?> body = res.getBody();
              assertThat(body).isNotNull();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("resetPassword")
  class ResetPassword {

    @Test
    @DisplayName("resets password")
    void shouldResetPassword() {
      PasswordResetResponse response =
          new PasswordResetResponse(USER_UUID, "temp123", true, "Password reset");
      given(credentialService.resetPassword(USER_UUID, ADMIN_UUID)).willReturn(response);

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(ADMIN_UUID));

        StepVerifier.create(controller.resetPassword(USER_UUID))
            .assertNext(res -> {
              assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("changePassword")
  class ChangePassword {

    @Test
    @DisplayName("changes password")
    void shouldChangePassword() {
      ChangePasswordRequest request = new ChangePasswordRequest("old123", "new123456");

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(USER_UUID));
        willDoNothing().given(credentialService).changePassword(eq(USER_UUID), any());

        StepVerifier.create(controller.changePassword(request))
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("refreshToken")
  class RefreshToken {

    @Test
    @DisplayName("refreshes token")
    void shouldRefreshToken() {
      RefreshTokenRequest request = new RefreshTokenRequest("old-refresh-token");
      LoginResponse loginResponse =
          new LoginResponse("new-jwt", 900, USER_UUID, "CUSTOMER", null, "new-refresh", false);
      given(refreshTokenService.refresh("old-refresh-token", null, null))
          .willReturn(loginResponse);

      StepVerifier.create(controller.refreshToken(request))
          .assertNext(res -> {
            assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
          })
          .verifyComplete();
    }
  }

  @Nested
  @DisplayName("logout")
  class Logout {

    @Test
    @DisplayName("logs out and returns 204")
    void shouldLogout() {
      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(USER_UUID));

        StepVerifier.create(controller.logout())
            .assertNext(res -> {
              assertThat(res.getStatusCode().value()).isEqualTo(204);
              assertThat(res.getBody()).isNull();
            })
            .verifyComplete();
      }
    }
  }

  @Nested
  @DisplayName("addVendorUser")
  class AddVendorUser {

    @Test
    @DisplayName("adds vendor user successfully")
    void shouldAddVendorUser() {
      UUID vendorId = UUID.randomUUID();
      AddVendorUserRequest request = new AddVendorUserRequest("New User", "new@test.com", Role.VENDOR_USER);
      UserResponse userResponse = new UserResponse(
          UUID.randomUUID(), "New User", "new@test.com", null, Role.VENDOR_USER,
          vendorId, "VEN-001", UserStatus.ACTIVE, Instant.now());

      given(registrationService.addVendorUser(USER_UUID, request)).willReturn(userResponse);

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveVendorId).thenReturn(Mono.just(vendorId));
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(USER_UUID));

        StepVerifier.create(controller.addVendorUser(vendorId, request))
            .assertNext(res -> {
              assertThat(res.getStatusCode().value()).isEqualTo(201);
              assertThat(res.getBody()).isNotNull();
            })
            .verifyComplete();
      }
    }

    @Test
    @DisplayName("rejects when vendor ID mismatch")
    void shouldRejectVendorMismatch() {
      UUID vendorId = UUID.randomUUID();
      UUID differentVendorId = UUID.randomUUID();
      AddVendorUserRequest request = new AddVendorUserRequest("New User", "new@test.com", Role.VENDOR_USER);

      try (MockedStatic<SecurityUtils> mocked = mockStatic(SecurityUtils.class)) {
        mocked.when(SecurityUtils::getReactiveVendorId).thenReturn(Mono.just(vendorId));
        mocked.when(SecurityUtils::getReactiveUserId).thenReturn(Mono.just(USER_UUID));

        StepVerifier.create(controller.addVendorUser(differentVendorId, request))
            .expectError(IllegalArgumentException.class)
            .verify();
      }
    }
  }
}
