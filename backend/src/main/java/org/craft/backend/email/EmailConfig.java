package org.craft.backend.email;

import com.resend.Resend;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {
    @Value("${resend.api.key}")
    private String KEY;

    @Bean
    public Resend emailSender() {
        return new Resend(KEY);
    }
}
