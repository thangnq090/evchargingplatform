package com.evcharging.identity.presentation;

import com.evcharging.identity.application.dto.AcceptInvitationRequest;
import com.evcharging.identity.application.dto.AddVendorUserRequest;
import com.evcharging.identity.application.dto.CreateVendorRequest;
import com.evcharging.identity.application.dto.CreateVendorResponse;
import com.evcharging.identity.application.dto.LoginRequest;
import com.evcharging.identity.application.dto.LoginResponse;
import com.evcharging.identity.application.dto.RegisterAdminRequest;
import com.evcharging.identity.application.dto.UserResponse;
import com.evcharging.identity.application.service.AuthenticationApplicationService;
import com.evcharging.identity.application.service.UserRegistrationApplicationService;
import com.evcharging.shared.api.ApiResponse;
import com.evcharging.shared.security.SecurityUtils;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Reactive REST controller for identity & access management (Spring WebFlux).
 *
 * <p>API versioning: {@code /api/v1/identity/}. All responses use the standard
 * {@link ApiResponse} envelope.
 */
@RestController
@RequestMapping("/api/v1/identity")
class IdentityController {

  private final UserRegistrationApplicationService registrationService;
  private final AuthenticationApplicationService authenticationService;

  IdentityController(
      UserRegistrationApplicationService registrationService,
      AuthenticationApplicationService authenticationService) {
    this.registrationService = registrationService;
    this.authenticationService = authenticationService;
  }

  /**
   * Register a new platform administrator.
   *
   * <p>Protected endpoint — requires {@code ROLE_ADMIN}.
   *
   * <p>{@code POST /api/v1/identity/auth/register-admin}
   */
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
   * Authenticate a user and obtain a JWT access token.
   *
   * <p>Public endpoint — no JWT required.
   *
   * <p>{@code POST /api/v1/identity/auth/login}
   */
  @PostMapping("/auth/login")
  Mono<ResponseEntity<ApiResponse<LoginResponse>>> login(
      @Valid @RequestBody LoginRequest request) {
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
   * Add a new VENDOR_USER to the calling VENDOR_ADMIN's vendor.
   *
   * <p>Requires {@code ROLE_VENDOR_ADMIN}. The vendorId in the path is validated against the
   * authenticated user's own vendorId to prevent IDOR.
   *
   * <p>{@code POST /api/v1/identity/vendors/{vendorId}/users}
   */
  @PostMapping("/vendors/{vendorId}/users")
  @PreAuthorize("hasRole('VENDOR_ADMIN')")
  Mono<ResponseEntity<ApiResponse<UserResponse>>> addVendorUser(
      @PathVariable UUID vendorId,
      @Valid @RequestBody AddVendorUserRequest request) {
    return Mono.fromCallable(
            () -> {
              UUID callerVendorId =
                  SecurityUtils.getCurrentVendorId()
                      .orElseThrow(
                          () -> new IllegalStateException("vendor_id claim missing from JWT"));

              if (!callerVendorId.equals(vendorId)) {
                throw new IllegalArgumentException("Cannot add users to a different vendor");
              }

              UUID callerId =
                  SecurityUtils.getCurrentUserId()
                      .orElseThrow(() -> new IllegalStateException("sub claim missing from JWT"));

              return registrationService.addVendorUser(callerId, request);
            })
        .map(
            user ->
                ResponseEntity.created(URI.create("/api/v1/identity/users/" + user.id()))
                    .body(ApiResponse.ok(user)));
  }
}
