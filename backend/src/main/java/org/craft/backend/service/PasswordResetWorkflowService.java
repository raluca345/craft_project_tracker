package org.craft.backend.service;

import lombok.RequiredArgsConstructor;
import org.craft.backend.email.EmailService;
import org.craft.backend.exceptions.ResourceNotFoundException;
import org.craft.backend.model.PasswordResetToken;
import org.craft.backend.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetWorkflowService {
    private final EmailService emailService;
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        try {
            User user = userService.getUserByEmail(email);
            PasswordResetToken token = userService.generatePasswordResetTokenForUser(user);
            emailService.sendPasswordResetEmail(email, token);
        } catch (ResourceNotFoundException e) {
            // deliberately swallow to not reveal if an account exists
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        passwordResetService.validatePasswordResetTokenOrThrow(token);

        UUID userId = passwordResetService.findUserIdByTokenOrThrow(token);
        User user = userService.getUserById(userId);

        userService.changePassword(user, newPassword);

        passwordResetService.deleteByToken(token);
    }
}
