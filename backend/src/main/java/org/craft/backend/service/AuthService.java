package org.craft.backend.service;

import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.AuthResponse;
import org.craft.backend.dto.LoginRequest;
import org.craft.backend.dto.RegisterRequest;
import org.craft.backend.exceptions.EmailAlreadyTakenException;
import org.craft.backend.exceptions.NameAlreadyTakenException;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final ImageService imageService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyTakenException("Email is already taken.");
        }

        if (userRepository.findByName(request.getName()).isPresent()) {
            throw new NameAlreadyTakenException("Name is already taken.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        String token = jwtService.generateToken(user);
        return AuthResponse.fromUser(token, user, imageService.presignUrl(user.getAvatarKey()));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userService.getUserByEmail(request.getEmail());

        String token = jwtService.generateToken(user);
        return AuthResponse.fromUser(token, user, imageService.presignUrl(user.getAvatarKey()));
    }
}
