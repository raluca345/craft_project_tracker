package org.craft.backend.dto;

import lombok.Data;
import org.craft.backend.model.User;

import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String avatarKey;
    private String avatarUrl;

    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setAvatarKey(user.getAvatarKey());
        return response;
    }
}
