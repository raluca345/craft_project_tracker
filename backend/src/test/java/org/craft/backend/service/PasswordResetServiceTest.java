package org.craft.backend.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import org.craft.backend.exceptions.PasswordResetTokenExpiredException;
import org.craft.backend.exceptions.ResourceNotFoundException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.PasswordResetToken;
import org.craft.backend.model.User;
import org.craft.backend.repository.PasswordResetTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    private Clock clock;
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(Instant.parse("2026-09-04T12:00:00Z"), ZoneOffset.UTC);
        passwordResetService = new PasswordResetService(passwordResetTokenRepository, clock);
    }

    private User buildUser() {
        return User.builder()
                .id(UUID.randomUUID())
                .name("Test User")
                .email("test@example.com")
                .password("encoded-password")
                .build();
    }

    private PasswordResetToken buildToken(User user, Instant expiry) {
        return PasswordResetToken.builder()
                .token("test-token")
                .user(user)
                .expiryDate(Date.from(expiry))
                .build();
    }

    @Test
    void findByToken_existingTokenAndUser_returnsToken() {
        User user = buildUser();
        PasswordResetToken token = buildToken(user, Instant.now(clock).plusSeconds(600));
        when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(token);

        PasswordResetToken result = passwordResetService.findByToken("test-token");

        assertNotNull(result);
        assertEquals("test-token", result.getToken());
        assertEquals(user.getId(), result.getUser().getId());
    }

    @Test
    void findByToken_nonExistingToken_returnsNull() {
        when(passwordResetTokenRepository.findByToken("non-existing-token")).thenReturn(null);

        PasswordResetToken result = passwordResetService.findByToken("non-existing-token");

        assertEquals(null, result);
    }

    @Test
    void findUserIdByTokenOrThrow_existingToken_returnsUserId() {
        User user = buildUser();
        when(passwordResetTokenRepository.findUserIdByToken("test-token")).thenReturn(Optional.of(user.getId()));

        UUID result = passwordResetService.findUserIdByTokenOrThrow("test-token");

        assertEquals(user.getId(), result);
    }

    @Test
    void findUserIdByTokenOrThrow_nonExistingToken_throwsUserNotFoundException() {
        when(passwordResetTokenRepository.findUserIdByToken("non-existing-token")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> passwordResetService.findUserIdByTokenOrThrow("non-existing-token"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void deleteByToken_existingToken_deletesToken() {
        passwordResetService.deleteByToken("test-token");

        verify(passwordResetTokenRepository).deleteByToken("test-token");
    }

    @Test
    void saveToken_existingToken_reusesExistingToken() {
        User user = buildUser();
        PasswordResetToken existingToken = buildToken(user, Instant.now(clock).plusSeconds(600));
        when(passwordResetTokenRepository.findByUserId(user.getId())).thenReturn(Optional.of(existingToken));
        when(passwordResetTokenRepository.save(existingToken)).thenReturn(existingToken);

        ArgumentCaptor<PasswordResetToken> captor = ArgumentCaptor.forClass(PasswordResetToken.class);
        PasswordResetToken result = passwordResetService.saveToken(user, "new-token");

        assertNotNull(result);
        verify(passwordResetTokenRepository).save(captor.capture());
        PasswordResetToken savedToken = captor.getValue();
        // the existing token should be reused instead of creating a new one
        assertSame(existingToken, savedToken);
        assertSame(existingToken, result);
        assertEquals("new-token", savedToken.getToken());
        assertEquals(Date.from(Instant.now(clock).plusSeconds(600)), savedToken.getExpiryDate());
        assertEquals(user.getId(), savedToken.getUser().getId());
    }

    @Test
    void validatePasswordResetTokenOrThrow_validToken_returnsTrue() {
        User user = buildUser();
        PasswordResetToken token = buildToken(user, Instant.now(clock).plusSeconds(600));
        when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(token);

        boolean result = passwordResetService.validatePasswordResetTokenOrThrow("test-token");

        assertTrue(result);
    }

    @Test
    void validatePasswordResetTokenOrThrow_nonExistingToken_throwsResourceNotFoundException() {
        when(passwordResetTokenRepository.findByToken("non-existing-token")).thenReturn(null);

        assertThatThrownBy(() -> passwordResetService.validatePasswordResetTokenOrThrow("non-existing-token"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Token not found");
    }

    @Test
    void validatePasswordResetTokenOrThrow_expiredToken_throwsPasswordResetTokenExpiredException() {
        User user = buildUser();
        PasswordResetToken token = buildToken(user, Instant.now(clock).minusSeconds(60));
        when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(token);

        assertThatThrownBy(() -> passwordResetService.validatePasswordResetTokenOrThrow("test-token"))
                .isInstanceOf(PasswordResetTokenExpiredException.class)
                .hasMessageContaining("Token expired");
    }

    @Test
    void validatePasswordResetTokenOrThrow_tokenWithNullExpiry_throwsPasswordResetTokenExpiredException() {
        User user = buildUser();
        PasswordResetToken token = buildToken(user, Instant.now(clock).plusSeconds(600));
        token.setExpiryDate(null);
        when(passwordResetTokenRepository.findByToken("test-token")).thenReturn(token);

        assertThatThrownBy(() -> passwordResetService.validatePasswordResetTokenOrThrow("test-token"))
                .isInstanceOf(PasswordResetTokenExpiredException.class);
    }
}
