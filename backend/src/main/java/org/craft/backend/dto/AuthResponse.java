package org.craft.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.craft.backend.model.User;

import java.util.UUID;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UUID id;
    private String name;
    private String email;
    private String avatarKey;
    private String avatarUrl;

    public static AuthResponse fromUser(String token, User user, String avatarUrl) {
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getAvatarKey(), avatarUrl);
    }
}
