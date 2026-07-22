package org.craft.backend.dto;

import lombok.Data;
import org.craft.backend.model.Project;
import org.craft.backend.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String password;
    private List<Project> projects = new ArrayList<>();

    public static UserResponse toResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPassword(user.getPassword());
        response.setProjects(user.getProjects());
        return response;
    }
}
