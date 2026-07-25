package com.evcharging.identity.application.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.identity.application.dto.AcceptInvitationRequest;
import com.evcharging.identity.application.dto.AddVendorUserRequest;
import com.evcharging.identity.application.dto.CreateVendorRequest;
import com.evcharging.identity.application.dto.CreateVendorResponse;
import com.evcharging.identity.application.dto.RegisterAdminRequest;
import com.evcharging.identity.application.dto.RegisterCustomerRequest;
import com.evcharging.identity.application.dto.UserResponse;
import com.evcharging.identity.domain.event.AdminRegisteredEvent;
import com.evcharging.identity.domain.event.CustomerRegisteredEvent;
import com.evcharging.identity.domain.event.VendorCreatedEvent;
import com.evcharging.identity.domain.event.VendorInvitationAcceptedEvent;
import com.evcharging.identity.domain.event.VendorInvitationIssuedEvent;
import com.evcharging.identity.domain.event.VendorUserCreatedEvent;
import com.evcharging.identity.domain.model.Invitation;
import com.evcharging.identity.domain.model.Role;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.model.Vendor;
import com.evcharging.identity.domain.repository.InvitationRepository;
import com.evcharging.identity.domain.repository.UserRepository;
import com.evcharging.identity.domain.repository.VendorRepository;

/**
 * Application service coordinating user registration and vendor onboarding use cases.
 *
 * <p>All business orchestration lives here. Keeps domain objects decoupled from HTTP and
 * infrastructure concerns.
 */
@Service
public class UserRegistrationApplicationService {

  private static final Logger log =
      LoggerFactory.getLogger(UserRegistrationApplicationService.class);

  /** Invitation tokens expire after 48 hours. */
  private static final long INVITATION_TTL_HOURS = 48;

  private final UserRepository userRepository;
  private final VendorRepository vendorRepository;
  private final InvitationRepository invitationRepository;
  private final PasswordEncoder passwordEncoder;
  private final ApplicationEventPublisher eventPublisher;

  public UserRegistrationApplicationService(
      UserRepository userRepository,
      VendorRepository vendorRepository,
      InvitationRepository invitationRepository,
      PasswordEncoder passwordEncoder,
      ApplicationEventPublisher eventPublisher) {
    this.userRepository = userRepository;
    this.vendorRepository = vendorRepository;
    this.invitationRepository = invitationRepository;
    this.passwordEncoder = passwordEncoder;
    this.eventPublisher = eventPublisher;
  }

  /**
   * Register a new platform administrator.
   *
   * @param request registration details
   * @return the created user
   * @throws IllegalStateException if the email is already registered
   */
  @Transactional
  public UserResponse registerAdmin(RegisterAdminRequest request) {
    String email = request.email().toLowerCase();

    if (userRepository.existsByEmail(email)) {
      throw new IllegalStateException("Email already registered: " + email);
    }

    String passwordHash = passwordEncoder.encode(request.password());
    User admin = User.createAdmin(request.name(), email, passwordHash);
    User saved = userRepository.save(admin);

    log.info("Admin registered: userId={}, email={}", saved.getId(), saved.getEmail());
    eventPublisher.publishEvent(
        new AdminRegisteredEvent(saved.getId(), saved.getEmail(), saved.getName(), Instant.now()));

    return UserResponse.from(saved);
  }

  /**
   * Register a new customer with auto-generated account number.
   *
   * @param request customer registration details
   * @return the created customer user
   * @throws IllegalStateException if the email is already registered
   */
  @Transactional
  public UserResponse registerCustomer(RegisterCustomerRequest request) {
    String email = request.email().toLowerCase();

    if (userRepository.existsByEmail(email)) {
      throw new IllegalStateException("Email already registered: " + email);
    }

    String accountNumber = generateAccountNumber();
    while (userRepository.existsByAccountNumber(accountNumber)) {
      accountNumber = generateAccountNumber();
    }

    String passwordHash = passwordEncoder.encode(request.password());
    User customer =
        User.createCustomer(request.name(), email, passwordHash, request.phone(), accountNumber);
    User saved = userRepository.save(customer);

    log.info(
        "Customer registered: userId={}, email={}, accountNumber={}",
        saved.getId(),
        saved.getEmail(),
        saved.getAccountNumber());
    eventPublisher.publishEvent(
        new CustomerRegisteredEvent(
            saved.getId(),
            saved.getEmail(),
            saved.getName(),
            saved.getAccountNumber(),
            Instant.now()));

    return UserResponse.from(saved);
  }

  /**
   * Create a new Vendor and issue a VENDOR_ADMIN invitation to the specified email.
   *
   * @param request vendor creation details
   * @return vendor and invitation details
   * @throws IllegalStateException if vendor name already exists or admin email is already
   *     registered
   */
  @Transactional
  public CreateVendorResponse createVendorWithAdmin(CreateVendorRequest request) {
    String adminEmail = request.adminEmail().toLowerCase();

    if (vendorRepository.existsByName(request.vendorName())) {
      throw new IllegalStateException("Vendor name already exists: " + request.vendorName());
    }
    if (userRepository.existsByEmail(adminEmail)) {
      throw new IllegalStateException("Email already registered: " + adminEmail);
    }

    Vendor vendor = Vendor.create(request.vendorName());
    Vendor savedVendor = vendorRepository.save(vendor);

    String token = generateSecureToken();
    Instant expiresAt = Instant.now().plus(INVITATION_TTL_HOURS, ChronoUnit.HOURS);
    Invitation invitation =
        Invitation.create(adminEmail, savedVendor.getId(), Role.VENDOR_ADMIN, token, expiresAt);
    Invitation savedInvitation = invitationRepository.save(invitation);

    log.info(
        "Vendor created: vendorId={}, invitationId={}",
        savedVendor.getId(),
        savedInvitation.getId());

    eventPublisher.publishEvent(
        new VendorCreatedEvent(savedVendor.getId(), savedVendor.getName(), Instant.now()));
    eventPublisher.publishEvent(
        new VendorInvitationIssuedEvent(
            savedInvitation.getId(),
            savedVendor.getId(),
            savedInvitation.getEmail(),
            savedInvitation.getToken(),
            savedInvitation.getExpiresAt()));

    return new CreateVendorResponse(
        savedVendor.getId(),
        savedVendor.getName(),
        savedInvitation.getId(),
        savedInvitation.getToken(),
        savedInvitation.getEmail());
  }

  /**
   * Accept a vendor invitation and register the invited user.
   *
   * @param request acceptance details containing invitation token and new password
   * @return the newly registered vendor user
   * @throws IllegalStateException if token is invalid, expired, or email is already registered
   */
  @Transactional
  public UserResponse acceptInvitation(AcceptInvitationRequest request) {
    Invitation invitation =
        invitationRepository
            .findByToken(request.token())
            .orElseThrow(() -> new IllegalArgumentException("Invalid invitation token"));

    // Validates PENDING status and expiry — throws if not valid
    invitation.accept();

    String email = invitation.getEmail();
    if (userRepository.existsByEmail(email)) {
      throw new IllegalStateException("Email already registered: " + email);
    }

    String passwordHash = passwordEncoder.encode(request.password());
    User vendorUser =
        User.createVendorUser(
            request.name(), email, passwordHash, invitation.getRole(), invitation.getVendorId());
    User saved = userRepository.save(vendorUser);

    // Persist the invitation status change
    invitationRepository.save(invitation);

    log.info(
        "Invitation accepted: invitationId={}, userId={}, vendorId={}",
        invitation.getId(),
        saved.getId(),
        saved.getVendorId());

    eventPublisher.publishEvent(
        new VendorInvitationAcceptedEvent(
            invitation.getId(),
            saved.getId(),
            saved.getEmail(),
            saved.getVendorId(),
            saved.getRole()));

    return UserResponse.from(saved);
  }

  /**
   * Add a new VENDOR_USER to the vendor of the currently authenticated VENDOR_ADMIN.
   *
   * @param vendorAdminId the ID of the calling VENDOR_ADMIN (extracted from JWT by controller)
   * @param request new user details
   * @return the created vendor user
   * @throws IllegalStateException if email is already registered
   * @throws IllegalArgumentException if the requested role is not VENDOR_USER
   */
  @Transactional
  public UserResponse addVendorUser(UUID vendorAdminId, AddVendorUserRequest request) {
    if (request.role() != Role.VENDOR_USER) {
      throw new IllegalArgumentException("Only VENDOR_USER role can be assigned via this endpoint");
    }

    User vendorAdmin =
        userRepository
            .findById(vendorAdminId)
            .orElseThrow(() -> new IllegalArgumentException("Vendor admin not found"));

    String email = request.email().toLowerCase();
    if (userRepository.existsByEmail(email)) {
      throw new IllegalStateException("Email already registered: " + email);
    }

    // Generate a temporary random password — user will reset on first login
    String tempPasswordHash = passwordEncoder.encode(generateSecureToken());
    User newUser =
        User.createVendorUser(
            request.name(), email, tempPasswordHash, Role.VENDOR_USER, vendorAdmin.getVendorId());
    User saved = userRepository.save(newUser);

    log.info("Vendor user created: userId={}, vendorId={}", saved.getId(), saved.getVendorId());

    eventPublisher.publishEvent(
        new VendorUserCreatedEvent(
            saved.getId(), saved.getVendorId(), saved.getEmail(), saved.getRole(), Instant.now()));

    return UserResponse.from(saved);
  }

  /** Generates an account number in format ACC-XXXXXXXX. */
  private static String generateAccountNumber() {
    String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    return "ACC-" + suffix;
  }

  /** Generates a cryptographically secure random 32-byte URL-safe Base64 token. */
  private static String generateSecureToken() {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
}
