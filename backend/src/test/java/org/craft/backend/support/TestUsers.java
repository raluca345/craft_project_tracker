package org.craft.backend.support;

import java.util.UUID;

import org.craft.backend.dto.AuthResponse;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.model.User;

public final class TestUsers {
    private TestUsers() {
    }

    public static User user() {
        return user(UUID.randomUUID(), "user", "user@example.com");
    }

    public static User user(UUID id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded-password");
        return user;
    }

    public static UserResponse userResponse(User user) {
        return UserResponse.toResponse(user);
    }

    public static AuthResponse authResponse(User user, String token) {
        return AuthResponse.fromUser(token, user, null);
    }
}
