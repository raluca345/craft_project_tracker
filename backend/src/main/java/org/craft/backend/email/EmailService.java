package org.craft.backend.email;

import org.craft.backend.model.PasswordResetToken;

public interface EmailService {
    void sendPasswordResetEmail(String recipient, PasswordResetToken token);
}
