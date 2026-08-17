package org.craft.backend.config;

import lombok.RequiredArgsConstructor;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        UserDetails userDetails = (UserDetails) Objects.requireNonNull(SecurityContextHolder.getContext()
                        .getAuthentication())
                .getPrincipal();
    return userRepository
        .findByEmail(userDetails.getUsername())
        .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
