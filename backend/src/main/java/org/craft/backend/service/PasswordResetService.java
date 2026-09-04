package org.craft.backend.service;

import lombok.RequiredArgsConstructor;
import org.craft.backend.exceptions.PasswordResetTokenExpiredException;
import org.craft.backend.exceptions.ResourceNotFoundException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.PasswordResetToken;
import org.craft.backend.model.User;
import org.craft.backend.repository.PasswordResetTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final Clock clock;

    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    public PasswordResetToken saveToken(User user, String token) {
        Date expirationDate = Date.from(Instant.now(clock).plus(10, ChronoUnit.MINUTES));
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByUserId(user.getId())
                .orElseGet(() -> PasswordResetToken.builder().user(user).build());
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(expirationDate);

        return passwordResetTokenRepository.save(passwordResetToken);
    }

    public UUID findUserIdByTokenOrThrow(String token) {
        return passwordResetTokenRepository.findUserIdByToken(token).orElseThrow(() -> new UserNotFoundException(
                "User not found"));
    }

    @Transactional(readOnly = true)
    public boolean validatePasswordResetTokenOrThrow(String token) {
        PasswordResetToken prt = findByToken(token);
        if (prt == null) {
            throw new ResourceNotFoundException("Token not found");
        }
        if (prt.getExpiryDate() == null || prt.getExpiryDate().before(Date.from(Instant.now(clock)))) {
            throw new PasswordResetTokenExpiredException("Token expired");
        }
        return true;
    }

    public void deleteByToken(String token) {
        passwordResetTokenRepository.deleteByToken(token);
    }
}
