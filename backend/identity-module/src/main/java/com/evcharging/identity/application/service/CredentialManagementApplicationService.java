package com.evcharging.identity.application.service;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evcharging.identity.application.dto.ChangePasswordRequest;
import com.evcharging.identity.application.dto.PasswordResetResponse;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.repository.UserRepository;

@Service
public class CredentialManagementApplicationService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private static final String CHAR_POOL =
      "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^*()";
  private static final SecureRandom RANDOM = new SecureRandom();

  public CredentialManagementApplicationService(
      UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional
  public PasswordResetResponse resetPassword(UUID targetUserId, UUID adminId) {
    User target =
        userRepository
            .findById(targetUserId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    String tempPassword = generateTempPassword();
    String hash = passwordEncoder.encode(tempPassword);

    target.initiatePasswordReset(hash);
    userRepository.save(target);

    return new PasswordResetResponse(
        target.getId(),
        tempPassword,
        true,
        "Temporary password issued. User must change on next login.");
  }

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequest request) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new IllegalArgumentException("Invalid current password");
    }

    String newHash = passwordEncoder.encode(request.newPassword());
    user.changePassword(newHash);
    userRepository.save(user);
  }

  private String generateTempPassword() {
    StringBuilder sb = new StringBuilder(10);
    for (int i = 0; i < 10; i++) {
      sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
    }
    return sb.toString();
  }
}
