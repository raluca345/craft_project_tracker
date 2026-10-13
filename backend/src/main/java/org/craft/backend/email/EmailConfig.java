package org.craft.backend.email;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {
    @Value("${app.email.resend-api-key}")
    private String KEY;

    // Resend (default) bean definitions - active when provider is "resend" or unset.
    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "resend", matchIfMissing = true)
    public Resend emailSender() {
        return new Resend(KEY);
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "resend", matchIfMissing = true)
    public EmailService resendEmailService(Resend resend,
                                            @Value("${app.email.from}") String from,
                                            @Value("${app.frontend.url}") String frontendBaseUrl) {
        return new ResendEmailService(resend, from, frontendBaseUrl);
    }

    // MailHog bean definitions - active only when provider is "mailhog".
    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "mailhog", matchIfMissing = false)
    public JavaMailSender mailSender(
            @Value("${mail.host}") String host,
            @Value("${mail.port}") int port) {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);
        mailSender.setJavaMailProperties(new Properties());
        return mailSender;
    }

    @Bean
    @ConditionalOnProperty(name = "app.email.provider", havingValue = "mailhog", matchIfMissing = false)
    public EmailService mailhogEmailService(JavaMailSender mailSender,
                                             @Value("${app.email.from}") String from,
                                             @Value("${app.frontend.url}") String frontendBaseUrl) {
        return new MailHogEmailService(mailSender, from, frontendBaseUrl);
    }
}
