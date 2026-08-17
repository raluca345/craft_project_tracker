package org.craft.backend.handler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import software.amazon.awssdk.core.exception.SdkException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Test
    void sdkExceptionReturns503WithFriendlyMessage() {
        SdkException ex = SdkException.builder().message("Connection refused").build();

        ResponseEntity<Map<String, Object>> response = handler.handleStorageUnavailable(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).containsEntry("status", 503);
        assertThat(response.getBody()).containsEntry("error", "Service Unavailable");
        assertThat(response.getBody()).containsEntry("message",
                "We couldn't save your image right now. Please try again in a moment.");
    }
}
