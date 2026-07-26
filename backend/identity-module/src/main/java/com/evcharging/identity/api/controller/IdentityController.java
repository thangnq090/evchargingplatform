package com.evcharging.identity.api.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
import com.evcharging.identity.application.dto.VendorListResponse;
import com.evcharging.identity.application.service.AuthenticationApplicationService;
import com.evcharging.identity.application.service.CredentialManagementApplicationService;
import com.evcharging.identity.application.service.RefreshTokenApplicationService;
import com.evcharging.identity.application.service.UserRegistrationApplicationService;
import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.repository.UserRepository;
import com.evcharging.identity.domain.repository.VendorRepository;
import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.pagination.PaginatedList;
import com.evcharging.shared.security.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

/**
 * Reactive REST controller for identity & access management (Spring WebFlux).
 *
 * <p>API versioning: {@code /api/v1/identity/}. All responses use the standard {@link ApiResponse}
 * envelope.
 */
@Tag(
    name = "Identity & Access Management",
    description =
        "Endpoints for user registration, authentication, vendor management, and credentials")
@RestController
@RequestMapping("/api/v1/identity")
public class IdentityController {

  private final UserRegistrationApplicationService registrationService;
  private final AuthenticationApplicationService authenticationService;
  private final CredentialManagementApplicationService credentialService;
  private final RefreshTokenApplicationService refreshTokenService;
  private final VendorRepository vendorRepository;
  private final UserRepository userRepository;

  IdentityController(
      UserRegistrationApplicationService registrationService,
      AuthenticationApplicationService authenticationService,
      CredentialManagementApplicationService credentialService,
      RefreshTokenApplicationService refreshTokenService,
      VendorRepository vendorRepository,
      UserRepository userRepository) {
    this.registrationService = registrationService;
    this.authenticationService = authenticationService;
    this.credentialService = credentialService;
    this.refreshTokenService = refreshTokenService;
    this.vendorRepository = vendorRepository;
    this.userRepository = userRepository;
  }

  /**
   * Register a new platform administrator.
   *
   * <p>Protected endpoint — requires {@code ROLE_ADMIN}.
   *
   * <p>{@code POST /api/v1/identity/auth/register-admin}
   */
  @Operation(
      summary = "Register Platform Administrator",
      description =
          "Registers a new platform administrator account. Protected endpoint requiring ROLE_ADMIN privilege.")
  @PostMapping("/auth/register-admin")
  @PreAuthorize("hasRole('ADMIN')")
  Mono<ResponseEntity<ApiResponse<UserResponse>>> registerAdmin(
      @Valid @RequestBody RegisterAdminRequest request) {
    return Mono.fromCallable(() -> registrationService.registerAdmin(request))
        .map(
            user ->
                ResponseEntity.created(URI.create("/api/v1/identity/users/" + user.id()))
                    .body(ApiResponse.ok(user)));
  }

  /**
   * Register a new customer user.
   *
   * <p>Public endpoint — no JWT required.
   *
   * <p>{@code POST /api/v1/identity/auth/register-customer}
   */
  @Operation(
      summary = "Register Customer User",
      description =
          "Public registration endpoint for new EV drivers/customers to create an account.")
  @PostMapping("/auth/register-customer")
  Mono<ResponseEntity<ApiResponse<UserResponse>>> registerCustomer(
      @Valid @RequestBody RegisterCustomerRequest request) {
    return Mono.fromCallable(() -> registrationService.registerCustomer(request))
        .map(
            user ->
                ResponseEntity.created(URI.create("/api/v1/identity/users/" + user.id()))
                    .body(ApiResponse.ok(user)));
  }

  /**
   * Authenticate a user and obtain a JWT access token.
   *
   * <p>Public endpoint — no JWT required.
   *
   * <p>{@code POST /api/v1/identity/auth/login}
   */
  @Operation(
      summary = "User Authentication Login",
      description =
          "Authenticates user credentials (email & password) and returns a Bearer JWT access token with refresh token.")
  @PostMapping("/auth/login")
  Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(@Valid @RequestBody LoginRequest request) {
    return Mono.fromCallable(() -> authenticationService.login(request))
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }

  /**
   * Accept a vendor invitation and register the invited user.
   *
   * <p>Public endpoint — no JWT required. The invitation token serves as authentication.
   *
   * <p>{@code POST /api/v1/identity/auth/invitations/accept}
   */
  @Operation(
      summary = "Accept Vendor User Invitation",
      description =
          "Accepts a vendor invitation token to complete user account registration for a vendor portal.")
  @PostMapping("/auth/invitations/accept")
  Mono<ResponseEntity<ApiResponse<UserResponse>>> acceptInvitation(
      @Valid @RequestBody AcceptInvitationRequest request) {
    return Mono.fromCallable(() -> registrationService.acceptInvitation(request))
        .map(
            user ->
                ResponseEntity.created(URI.create("/api/v1/identity/users/" + user.id()))
                    .body(ApiResponse.ok(user)));
  }

  /**
   * Create a new Vendor and issue a VENDOR_ADMIN invitation.
   *
   * <p>Requires {@code ROLE_ADMIN}.
   *
   * <p>{@code POST /api/v1/identity/vendors}
   */
  @Operation(
      summary = "Create Charging Station Vendor",
      description =
          "Creates a new vendor entity and issues an invitation to the vendor administrator. Requires ROLE_ADMIN.")
  @PostMapping("/vendors")
  @PreAuthorize("hasRole('ADMIN')")
  Mono<ResponseEntity<ApiResponse<CreateVendorResponse>>> createVendor(
      @Valid @RequestBody CreateVendorRequest request) {
    return Mono.fromCallable(() -> registrationService.createVendorWithAdmin(request))
        .map(
            res ->
                ResponseEntity.created(URI.create("/api/v1/identity/vendors/" + res.vendorId()))
                    .body(ApiResponse.ok(res)));
  }

  /**
   * List vendors with cursor-based pagination.
   *
   * <p>Requires {@code ROLE_ADMIN}. Returns vendors newest first.
   *
   * <p>{@code GET /api/v1/identity/vendors?limit=20&cursor=...}
   */
  @Operation(
      summary = "List Vendors (Paginated)",
      description =
          "Retrieves a paginated list of registered charging station vendors. Requires ROLE_ADMIN.")
  @GetMapping("/vendors")
  @PreAuthorize("hasRole('ADMIN')")
  Mono<ResponseEntity<ApiResponse<PaginatedList<VendorListResponse.VendorSummary>>>> listVendors(
      @RequestParam(defaultValue = "20") int limit, @RequestParam(required = false) String cursor) {

    UUID decoded = PaginatedList.decode(cursor);
    return Mono.fromCallable(
            () -> {
              PaginatedList<Vendor> page = vendorRepository.findAll(limit, decoded);
              List<VendorListResponse.VendorSummary> items =
                  page.items().stream()
                      .map(
                          v ->
                              new VendorListResponse.VendorSummary(
                                  v.getId(),
                                  v.getName(),
                                  v.getStatus().name(),
                                  v.getMarkupPercentage().getBasisPoints(),
                                  v.getCreatedAt(),
                                  v.getUpdatedAt()))
                      .toList();
              return new PaginatedList<>(items, page.pagination());
            })
        .map(list -> ResponseEntity.ok(ApiResponse.ok(list)));
  }

  /**
   * List all users (admin only).
   *
   * <p>{@code GET /api/v1/identity/users}
   */
  @Operation(
      summary = "List All Registered Users",
      description =
          "Retrieves all registered platform users across all roles. Requires ROLE_ADMIN.")
  @GetMapping("/users")
  @PreAuthorize("hasRole('ADMIN')")
  Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> listUsers() {
    return Mono.fromCallable(
            () ->
                userRepository.findAll().stream()
                    .map(
                        u ->
                            new UserResponse(
                                u.getId(),
                                u.getName(),
                                u.getEmail(),
                                u.getPhone(),
                                u.getRole(),
                                u.getVendorId(),
                                u.getAccountNumber(),
                                u.getStatus(),
                                u.getCreatedAt()))
                    .toList())
        .map(list -> ResponseEntity.ok(ApiResponse.ok(list)));
  }

  /**
   * Add a new VENDOR_USER to the calling VENDOR_ADMIN's vendor.
   *
   * <p>Requires {@code ROLE_VENDOR_ADMIN}. The vendorId in the path is validated against the
   * authenticated user's own vendorId to prevent IDOR.
   *
   * <p>{@code POST /api/v1/identity/vendors/{vendorId}/users}
   */
  @Operation(
      summary = "Add Vendor Team Member",
      description =
          "Allows a vendor admin to invite and add a team user to their vendor account. Requires ROLE_VENDOR_ADMIN.")
  @PostMapping("/vendors/{vendorId}/users")
  @PreAuthorize("hasRole('VENDOR_ADMIN')")
  Mono<ResponseEntity<ApiResponse<UserResponse>>> addVendorUser(
      @PathVariable UUID vendorId, @Valid @RequestBody AddVendorUserRequest request) {
    return SecurityUtils.getReactiveVendorId()
        .switchIfEmpty(Mono.error(new IllegalStateException("vendor_id claim missing from JWT")))
        .flatMap(
            callerVendorId -> {
              if (!callerVendorId.equals(vendorId)) {
                return Mono.error(
                    new IllegalArgumentException("Cannot add users to a different vendor"));
              }
              return SecurityUtils.getReactiveUserId()
                  .switchIfEmpty(
                      Mono.error(new IllegalStateException("sub claim missing from JWT")));
            })
        .flatMap(
            callerId ->
                Mono.fromCallable(() -> registrationService.addVendorUser(callerId, request)))
        .map(
            user ->
                ResponseEntity.created(URI.create("/api/v1/identity/users/" + user.id()))
                    .body(ApiResponse.ok(user)));
  }

  // ==================== RBAC & Credential Management Endpoints ====================

  /**
   * Reset a user's password (Admin only).
   *
   * <p>Requires {@code ROLE_ADMIN}. Generates a temporary password and forces the user to change it
   * on next login.
   *
   * <p>{@code POST /api/v1/identity/users/{userId}/password/reset}
   */
  @Operation(
      summary = "Admin Reset User Password",
      description =
          "Resets password for a given user ID and forces password update on next login. Requires ROLE_ADMIN.")
  @PostMapping("/users/{userId}/password/reset")
  @PreAuthorize("hasRole('ADMIN')")
  Mono<ResponseEntity<ApiResponse<PasswordResetResponse>>> resetPassword(
      @PathVariable UUID userId) {
    return SecurityUtils.getReactiveUserId()
        .switchIfEmpty(Mono.error(new IllegalStateException("sub claim missing from JWT")))
        .flatMap(
            adminId -> Mono.fromCallable(() -> credentialService.resetPassword(userId, adminId)))
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }

  /**
   * Change own password.
   *
   * <p>Any authenticated user can change their own password.
   *
   * <p>{@code POST /api/v1/identity/users/me/password}
   */
  @Operation(
      summary = "Change Current User Password",
      description = "Allows an authenticated user to update their current password.")
  @PostMapping("/users/me/password")
  Mono<ResponseEntity<ApiResponse<Void>>> changePassword(
      @Valid @RequestBody ChangePasswordRequest request) {
    return SecurityUtils.getReactiveUserId()
        .switchIfEmpty(Mono.error(new IllegalStateException("sub claim missing from JWT")))
        .flatMap(
            userId ->
                Mono.fromCallable(
                    () -> {
                      credentialService.changePassword(userId, request);
                      return null;
                    }))
        .map(response -> ResponseEntity.ok(ApiResponse.ok(null)));
  }

  /**
   * Refresh access token.
   *
   * <p>Public endpoint — no JWT required. The refresh token serves as the credential. Implements
   * token rotation with reuse detection.
   *
   * <p>{@code POST /api/v1/identity/auth/refresh}
   */
  @Operation(
      summary = "Refresh Access Token",
      description =
          "Exchanges a valid refresh token for a new JWT access token and rotated refresh token.")
  @PostMapping("/auth/refresh")
  Mono<ResponseEntity<ApiResponse<LoginResponse>>> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    return Mono.fromCallable(
            () ->
                refreshTokenService.refresh(
                    request.refreshToken(),
                    null, // userAgent - would need ServerHttpRequest
                    null)) // ipAddress
        .map(response -> ResponseEntity.ok(ApiResponse.ok(response)));
  }

  /**
   * Logout and revoke all refresh tokens.
   *
   * <p>Authenticated endpoint — revokes all active refresh tokens for the calling user.
   *
   * <p>{@code POST /api/v1/identity/auth/logout}
   */
  @Operation(
      summary = "User Logout",
      description = "Revokes all active refresh tokens for the current authenticated user session.")
  @PostMapping("/auth/logout")
  Mono<ResponseEntity<Void>> logout() {
    return SecurityUtils.getReactiveUserId()
        .switchIfEmpty(Mono.error(new IllegalStateException("sub claim missing from JWT")))
        .flatMap(userId -> Mono.fromRunnable(() -> refreshTokenService.logout(userId)))
        .then(Mono.just(ResponseEntity.noContent().<Void>build()));
  }
}
