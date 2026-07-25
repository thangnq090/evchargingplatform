package com.evcharging.identity.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.evcharging.identity.application.dto.ChangePasswordRequest;
import com.evcharging.identity.application.dto.PasswordResetResponse;
import com.evcharging.identity.domain.model.User;
import com.evcharging.identity.domain.repository.UserRepository;

/** Unit tests for {@link CredentialManagementApplicationService}. */
@ExtendWith(MockitoExtension.class)
@DisplayName("CredentialManagementApplicationService")
class CredentialManagementApplicationServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private PasswordEncoder passwordEncoder;

  private CredentialManagementApplicationService service;

  @BeforeEach
  void setUp() {
    service = new CredentialManagementApplicationService(userRepository, passwordEncoder);
  }

  @Nested
  @DisplayName("resetPassword()")
  class ResetPassword {

    @Test
    @DisplayName("generates temporary password and initiates reset")
    void generatesTempPasswordAndInitiatesReset() {
      UUID targetUserId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();
      User targetUser = User.createAdmin("Target User", "target@example.com", "oldHash");

      when(userRepository.findById(targetUserId)).thenReturn(Optional.of(targetUser));
      when(passwordEncoder.encode(any())).thenReturn("tempHash");
      when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      PasswordResetResponse response = service.resetPassword(targetUserId, adminId);

      assertNotNull(response);
      assertEquals(targetUser.getId(), response.userId());
      assertNotNull(response.temporaryPassword());
      assertTrue(response.mustChangePassword());
      assertTrue(response.message().contains("Temporary password issued"));

      verify(userRepository).save(argThat(user -> user.isMustChangePassword()));
    }

    @Test
    @DisplayName("throws exception when user not found")
    void throwsWhenUserNotFound() {
      UUID targetUserId = UUID.randomUUID();
      UUID adminId = UUID.randomUUID();

      when(userRepository.findById(targetUserId)).thenReturn(Optional.empty());

      assertThrows(
          IllegalArgumentException.class, () -> service.resetPassword(targetUserId, adminId));
    }
  }

  @Nested
  @DisplayName("changePassword()")
  class ChangePassword {

    @Test
    @DisplayName("changes password when current password matches")
    void changesPasswordWhenCurrentMatches() {
      UUID userId = UUID.randomUUID();
      User user = User.createAdmin("Test User", "test@example.com", "currentHash");
      ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "newPass123!");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("currentPass", "currentHash")).thenReturn(true);
      when(passwordEncoder.encode("newPass123!")).thenReturn("newHash");
      when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

      assertDoesNotThrow(() -> service.changePassword(userId, request));

      verify(userRepository).save(argThat(u -> !u.isMustChangePassword()));
    }

    @Test
    @DisplayName("throws when current password does not match")
    void throwsWhenCurrentPasswordDoesNotMatch() {
      UUID userId = UUID.randomUUID();
      User user = User.createAdmin("Test User", "test@example.com", "currentHash");
      ChangePasswordRequest request = new ChangePasswordRequest("wrongPass", "newPass123!");

      when(userRepository.findById(userId)).thenReturn(Optional.of(user));
      when(passwordEncoder.matches("wrongPass", "currentHash")).thenReturn(false);

      assertThrows(IllegalArgumentException.class, () -> service.changePassword(userId, request));

      verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("throws when user not found")
    void throwsWhenUserNotFound() {
      UUID userId = UUID.randomUUID();
      ChangePasswordRequest request = new ChangePasswordRequest("currentPass", "newPass123!");

      when(userRepository.findById(userId)).thenReturn(Optional.empty());

      assertThrows(IllegalArgumentException.class, () -> service.changePassword(userId, request));
    }
  }
}
