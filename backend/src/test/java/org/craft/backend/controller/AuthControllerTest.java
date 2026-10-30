package org.craft.backend.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.craft.backend.dto.AuthResponse;
import org.craft.backend.dto.LoginRequest;
import org.craft.backend.dto.RegisterRequest;
import org.craft.backend.exceptions.EmailAlreadyTakenException;
import org.craft.backend.exceptions.NameAlreadyTakenException;
import org.craft.backend.model.User;
import org.craft.backend.config.SecurityConfig;
import org.craft.backend.service.AuthService;
import org.craft.backend.service.JwtService;
import org.craft.backend.support.TestUsers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {
    private ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthService authService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;

    private RegisterRequest registerRequest(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);

        return request;
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        return request;
    }

    @Nested
    class RegisterEndpoint {
        @Test
        void whenValidRequest_thenCreatesUserAndReturnsCreated() throws Exception {
            User user = TestUsers.user();
            RegisterRequest request = registerRequest(user.getName(), user.getEmail(), user.getPassword());
            AuthResponse response = TestUsers.authResponse(user, "jwt-token");
            when(authService.register(request)).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("user"))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.token").value("jwt-token"));

            verify(authService).register(request);
        }

        @Test
        void whenInvalidRequestValues_thenReturnsBadRequestWithoutCreatingUser() throws Exception {
            RegisterRequest request = registerRequest("abc", "bad@format", "short");

            mockMvc.perform(post("/api/v1/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }

        @Test
        void whenDuplicateEmail_thenReturnsConflict() throws Exception {
            User user = TestUsers.user();
            RegisterRequest request = registerRequest(user.getName(), user.getEmail(), user.getPassword());
            when(authService.register(request)).thenThrow(new EmailAlreadyTakenException("Email is already taken."));

            mockMvc.perform(post("/api/v1/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            verify(authService).register(request);
        }

        @Test
        void whenDuplicateName_thenReturnsConflict() throws Exception {
            User user = TestUsers.user();
            RegisterRequest request = registerRequest(user.getName(), user.getEmail(), user.getPassword());
            when(authService.register(request)).thenThrow(new NameAlreadyTakenException("Name is already taken."));

            mockMvc.perform(post("/api/v1/auth/register")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());

            verify(authService).register(request);
        }
    }

    @Nested
    class LoginEndpoint {
        @Test
        void whenValidRequest_thenLogsInAndReturnsOk() throws Exception {
            User user = TestUsers.user();
            LoginRequest request = loginRequest(user.getEmail(), user.getPassword());
            AuthResponse response = TestUsers.authResponse(user, "jwt-token");

            when(authService.login(request)).thenReturn(response);

            mockMvc.perform(post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("user"))
                    .andExpect(jsonPath("$.email").value("user@example.com"))
                    .andExpect(jsonPath("$.token").value("jwt-token"));

            verify(authService).login(request);
        }

        @Test
        void whenWrongCredentials_thenReturnsUnauthorized() throws Exception {
            LoginRequest request = loginRequest("wrong@email.com", "wrong-password");
            when(authService.login(request)).thenThrow(BadCredentialsException.class);

            mockMvc.perform(post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(authService).login(request);
        }

        @Test
        void whenInvalidCredentials_thenReturnsBadRequest() throws Exception {
            LoginRequest request = loginRequest("bad-format", "short");

            mockMvc.perform(post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authService);
        }
    }
}
