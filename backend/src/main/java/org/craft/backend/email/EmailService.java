package org.craft.backend.email;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.core.exception.ResendException;
import lombok.RequiredArgsConstructor;
import org.craft.backend.model.PasswordResetToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final Resend resend;

    @Value("${app.email.from}")
    private String from;

    @Value("${app.frontend.url}")
    private String FRONTEND_BASE_URL;

    public void sendPasswordResetEmail(String recipient, PasswordResetToken token) {
        String url = FRONTEND_BASE_URL + "/auth/password-reset/verify?token=" + token.getToken();
        String subject = "Reset your password";

        CreateEmailOptions options = CreateEmailOptions.builder()
                .from(from)
                .to(recipient)
                .subject(subject)
                .text("Click the link below to reset your password: " + url)
                .html("<p>Click the button below to reset your password:</p>"
                        + "<a href=\"" + url + "\" style=\"display:inline-block;padding:10px 20px;"
                        + "background-color:#2563eb;color:#ffffff;text-decoration:none;border-radius:4px;\">"
                        + "Reset password</a>")
                .build();

        try {
            resend.emails().send(options);
        } catch (ResendException e) {
            log.error("Failed to send email to {}", recipient, e);
        }
    }
}
