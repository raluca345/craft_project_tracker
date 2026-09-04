package org.craft.backend.controller;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.craft.backend.dto.ForgotPasswordRequest;
import org.craft.backend.dto.ResetPasswordRequest;
import org.craft.backend.email.EmailService;
import org.craft.backend.exceptions.PasswordResetTokenExpiredException;
import org.craft.backend.exceptions.ResourceNotFoundException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.service.JwtService;
import org.craft.backend.service.PasswordResetService;
import org.craft.backend.service.PasswordResetWorkflowService;
import org.craft.backend.service.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PasswordResetController.class)
public class PasswordResetControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private PasswordResetWorkflowService resetWorkflowService;
    @MockitoBean
    private PasswordResetService passwordResetService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private EmailService emailService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private final String BACKEND_BASE_URL = "http://localhost:8080/api/v1";

    @Nested
    class RequestPasswordResetEmailTests {

        @Test
        @WithMockUser
        void whenValidUserEmail_thenSendPasswordResetLink() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("test@example.com");

            mockMvc.perform(post("/api/v1/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isAccepted());

            verify(resetWorkflowService, times(1)).sendPasswordResetEmail("test@example.com");
        }

        @Test
        @WithMockUser
        void whenInvalidUserEmail_thenReturnBadRequest() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("test");

            mockMvc.perform(post("/api/v1/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isBadRequest());
            verifyNoInteractions(userService, emailService);
        }

        @Test
        @WithMockUser
        void whenEmailNotFound_silentlySwallows() throws Exception {
            ForgotPasswordRequest request = new ForgotPasswordRequest();
            request.setEmail("ghost@example.com");
            when(userService.getUserByEmail(request.getEmail()))
                    .thenThrow(new UserNotFoundException(
                            "User with the email ghost@example.com doesn't exist"));

            mockMvc.perform(post("/api/v1/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isAccepted());

            // should not throw, should not generate token or send email
            assertThatCode(() -> resetWorkflowService.sendPasswordResetEmail("ghost@example.com"))
                    .doesNotThrowAnyException();

            verify(userService, never()).generatePasswordResetTokenForUser(any());
            verify(emailService, never()).sendPasswordResetEmail(anyString(), any());
        }
    }

    @Nested
    class VerifyPasswordResetTokenTests {

        String verifyUrl = BACKEND_BASE_URL + "/auth/password-reset/verify?token=123";

        @Test
        @WithMockUser
        void whenValidToken_thenReturnOk() throws Exception {
            when(passwordResetService.validatePasswordResetTokenOrThrow("123")).thenReturn(true);

            mockMvc.perform(get(verifyUrl)
                    .with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        void whenInvalidToken_thenReturnNotFound() throws Exception {
            when(passwordResetService.validatePasswordResetTokenOrThrow("123"))
                    .thenThrow(ResourceNotFoundException.class);

            mockMvc.perform(get(verifyUrl)
                    .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @WithMockUser
        void whenExpiredToken_thenReturnGone() throws Exception {
            when(passwordResetService.validatePasswordResetTokenOrThrow("123"))
                    .thenThrow(PasswordResetTokenExpiredException.class);

            mockMvc.perform(get(verifyUrl)
                    .with(csrf()))
                    .andExpect(status().isGone());
        }
    }

    @Nested
    class ConfirmPasswordResetTests {

        String confirmUrl = "/api/v1/auth/password-reset/confirm";

        @Test
        @WithMockUser
        void whenValidTokenAndPassword_thenReturnNoContent() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("valid-token");
            request.setNewPassword("strongp@assword");

            String json = mapper.writeValueAsString(request);
            doNothing().when(resetWorkflowService).resetPassword(request.getToken(), request.getNewPassword());

            mockMvc.perform(post(confirmUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(resetWorkflowService, times(1)).resetPassword(request.getToken(), request.getNewPassword());
        }

        @Test
        @WithMockUser
        void whenExpiredToken_thenReturnGone() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("invalid-token");
            request.setNewPassword("irrelevant");

            doThrow(PasswordResetTokenExpiredException.class).when(resetWorkflowService)
                    .resetPassword(request.getToken(), request.getNewPassword());

            String json = mapper.writeValueAsString(request);

            mockMvc.perform(post(confirmUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(csrf()))
                    .andExpect(status().isGone());

            verify(resetWorkflowService, times(1)).resetPassword(request.getToken(), request.getNewPassword());
        }

        @Test
        @WithMockUser
        void whenInvalidToken_thenReturnNotFound() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("invalid-token");
            request.setNewPassword("irrelevant");
            String json = mapper.writeValueAsString(request);

            doThrow(ResourceNotFoundException.class).when(resetWorkflowService)
                    .resetPassword(request.getToken(), request.getNewPassword());

            mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(resetWorkflowService, times(1)).resetPassword(request.getToken(), request.getNewPassword());
        }

        @Test
        @WithMockUser
        void whenGenericFailure_thenReturnBadRequest() throws Exception {
            ResetPasswordRequest request = new ResetPasswordRequest();
            request.setToken("some-token");
            request.setNewPassword("badpassword");
            String json = mapper.writeValueAsString(request);

            doThrow(IllegalStateException.class).when(resetWorkflowService)
                    .resetPassword(request.getToken(), request.getNewPassword());

            mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(csrf()))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Internal server error")));

            verify(resetWorkflowService, times(1)).resetPassword(request.getToken(), request.getNewPassword());
        }

        @Test
        @WithMockUser
        void whenPasswordIsTooLong_thenReturnBadRequest() throws Exception {
            ResetPasswordRequest dto = new ResetPasswordRequest();
            dto.setToken("some-token");
            dto.setNewPassword("a".repeat(73));
            String json = mapper.writeValueAsString(dto);

            mockMvc.perform(post("/api/v1/auth/password-reset/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(json)
                    .with(csrf()))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("Password must not exceed 72 characters")));

            verify(resetWorkflowService, never()).resetPassword(any(), any());
        }
    }
}
