package org.craft.backend.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AllArgsConstructor;
import org.craft.backend.model.PasswordResetToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@AllArgsConstructor
public class MailHogEmailService implements EmailService {
    private static final Logger log = LoggerFactory.getLogger(MailHogEmailService.class);
    private final JavaMailSender mailSender;
    private final String from;
    private final String frontendBaseUrl;

    @Override
    public void sendPasswordResetEmail(String recipient, PasswordResetToken token) {
        String url = frontendBaseUrl + "/reset-password?token=" + token.getToken();
        String subject = "Reset your password";

        String html = "<p>Click the button below to reset your password:</p>"
                + "<a href=\"" + url + "\" style=\"display:inline-block;padding:10px 20px;\""
                + "background-color:#2563eb;color:#ffffff;text-decoration:none;border-radius:4px;\">"
                + "Reset password</a>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText("Click the link below to reset your password: " + url, html);
            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}", recipient, e);
        }
    }
}