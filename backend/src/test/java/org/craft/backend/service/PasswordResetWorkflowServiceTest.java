package org.craft.backend.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

import org.craft.backend.email.EmailService;
import org.craft.backend.exceptions.ResourceNotFoundException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.PasswordResetToken;
import org.craft.backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PasswordResetWorkflowServiceTest {

    @Mock
    private EmailService emailService;
    @Mock
    private UserService userService;
    @Mock
    private PasswordResetService passwordResetService;

    private Clock clock;
    private PasswordResetWorkflowService passwordResetWorkflowService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-09-04T12:00:00Z"), ZoneOffset.UTC);
        passwordResetWorkflowService = new PasswordResetWorkflowService(emailService, userService,
                passwordResetService);
    }

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("encoded-password")
                .build();
    }

    private PasswordResetToken buildToken(User user) {
        return PasswordResetToken.builder()
                .token("test-token")
                .user(user)
                .expiryDate(Date.from(Instant.now(clock).plus(Duration.ofMinutes(10))))
                .build();
    }

    @Test
    void sendPasswordResetEmail_existingUser_generatesTokenAndSendsEmail() {
        User user = buildUser();
        PasswordResetToken token = buildToken(user);
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(userService.generatePasswordResetTokenForUser(user)).thenReturn(token);

        passwordResetWorkflowService.sendPasswordResetEmail(user.getEmail());

        verify(userService).getUserByEmail(user.getEmail());
        verify(userService).generatePasswordResetTokenForUser(user);
        verify(emailService).sendPasswordResetEmail(user.getEmail(), token);
    }

    @Test
    void sendPasswordResetEmail_resourceNotFound_swallowsAndDoesNotSendEmail() {
        String email = "missing@example.com";
        when(userService.getUserByEmail(email)).thenThrow(ResourceNotFoundException.class);

        assertThatCode(() -> passwordResetWorkflowService.sendPasswordResetEmail(email))
                .doesNotThrowAnyException();

        verify(userService).getUserByEmail(email);
        verify(emailService, never()).sendPasswordResetEmail(anyString(), any());
    }

    @Test
    void sendPasswordResetEmail_userNotFound_propagates() {
        String email = "missing@example.com";
        when(userService.getUserByEmail(email)).thenThrow(new UserNotFoundException("User not found"));

        assertThatThrownBy(() -> passwordResetWorkflowService.sendPasswordResetEmail(email))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");

        verify(emailService, never()).sendPasswordResetEmail(anyString(), any());
    }

    @Test
    void resetPassword_validToken_changesPasswordAndDeletesToken() {
        User user = buildUser();
        when(passwordResetService.validatePasswordResetTokenOrThrow("test-token")).thenReturn(true);
        when(passwordResetService.findUserIdByTokenOrThrow("test-token")).thenReturn(user.getId());
        when(userService.getUserById(user.getId())).thenReturn(user);

        passwordResetWorkflowService.resetPassword("test-token", "new-strong-password");

        verify(passwordResetService).validatePasswordResetTokenOrThrow("test-token");
        verify(passwordResetService).findUserIdByTokenOrThrow("test-token");
        verify(userService).getUserById(user.getId());
        verify(userService).changePassword(user, "new-strong-password");
        verify(passwordResetService).deleteByToken("test-token");
    }

    @Test
    void resetPassword_invalidToken_doesNotChangePasswordOrDeleteToken() {
        when(passwordResetService.validatePasswordResetTokenOrThrow("invalid-token"))
                .thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> passwordResetWorkflowService.resetPassword("invalid-token", "new-strong-password"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(passwordResetService, never()).findUserIdByTokenOrThrow(anyString());
        verify(userService, never()).changePassword(any(), anyString());
        verify(passwordResetService, never()).deleteByToken(anyString());
    }

    @Test
    void resetPassword_userNotFoundForToken_doesNotChangePasswordOrDeleteToken() {
        when(passwordResetService.validatePasswordResetTokenOrThrow("test-token")).thenReturn(true);
        when(passwordResetService.findUserIdByTokenOrThrow("test-token"))
                .thenThrow(new UserNotFoundException("User not found"));

        assertThatThrownBy(() -> passwordResetWorkflowService.resetPassword("test-token", "new-strong-password"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userService, never()).changePassword(any(), anyString());
        verify(passwordResetService, never()).deleteByToken(anyString());
    }

}

    
                