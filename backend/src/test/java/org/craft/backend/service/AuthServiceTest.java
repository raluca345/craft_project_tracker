package org.craft.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.craft.backend.dto.AuthResponse;
import org.craft.backend.dto.LoginRequest;
import org.craft.backend.dto.RegisterRequest;
import org.craft.backend.exceptions.EmailAlreadyTakenException;
import org.craft.backend.exceptions.NameAlreadyTakenException;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private ImageService imageService;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest(String name, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
    @Test
    void register_validRequest_encodesPasswordSavesUserAndReturnsAuthResponse() {
        String name = "Test User";
        String email = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = "encoded-password";
        String token = "jwt-token";

        RegisterRequest request = registerRequest(name, email, rawPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByName(name)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(jwtService.generateToken(any(User.class))).thenReturn(token);
        when(imageService.presignUrl(null)).thenReturn(null);

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals(name, response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(token, response.getToken());

        verify(passwordEncoder).encode(rawPassword);
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void register_duplicateEmail_throwsEmailAlreadyTakenException() {
        String name = "Test User";
        String email = "existing@example.com";
        RegisterRequest request = registerRequest(name, email, "password123");

        User existingUser = new User();
        existingUser.setEmail(email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(EmailAlreadyTakenException.class,
                () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void register_duplicateName_throwsNameAlreadyTakenException() {
        String name = "Existing Name";
        String email = "new@example.com";
        RegisterRequest request = registerRequest(name, email, "password123");

        User existingUser = new User();
        existingUser.setName(name);
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(userRepository.findByName(name)).thenReturn(Optional.of(existingUser));

        assertThrows(NameAlreadyTakenException.class,
                () -> authService.register(request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_validCredentials_authenticatesAndReturnsAuthResponse() {
        String email = "test@example.com";
        String password = "password123";
        String token = "jwt-token";

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        User user = new User();
        user.setName("Test User");
        user.setEmail(email);
        user.setPassword("encoded-password");

        when(userService.getUserByEmail(email)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(token);
        when(imageService.presignUrl(null)).thenReturn(null);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertEquals(email, response.getEmail());
        assertEquals(token, response.getToken());

        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
        verify(userService).getUserByEmail(email);
        verify(jwtService).generateToken(user);
    }

    @Test
    void login_wrongCredentials_propagatesBadCredentialsException() {
        String email = "test@example.com";
        String password = "wrong-password";

        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(
                        new UsernamePasswordAuthenticationToken(email, password));

        assertThrows(BadCredentialsException.class,
                () -> authService.login(request));

        verify(jwtService, never()).generateToken(any());
    }
}