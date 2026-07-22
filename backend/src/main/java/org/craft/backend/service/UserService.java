package org.craft.backend.service;

import lombok.Data;
import org.craft.backend.dto.ChangeEmailRequest;
import org.craft.backend.dto.CreateUserRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Data
public class UserService {
    private final UserRepository userRepository;

    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        return UserResponse.toResponse(user);
    }

    public UserResponse getUserByName(String name) {
        User user = userRepository.findByName(name).orElseThrow(() -> new UserNotFoundException("User " + name + " " +
                "not found"));
        return UserResponse.toResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with the " +
                "email " +
                "address " + email + " was not found"));
        return UserResponse.toResponse(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));

        userRepository.save(user);
        return UserResponse.toResponse(user);
    }

    public UserResponse renameUser(UUID id, RenameUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        user.setName(request.getName());

        userRepository.save(user);
        return UserResponse.toResponse(user);
    }

    public UserResponse changeEmail(UUID id, ChangeEmailRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        user.setEmail(request.getEmail());

        userRepository.save(user);
        return UserResponse.toResponse(user);
    }

    public UserResponse editUser(UUID id, CreateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(new BCryptPasswordEncoder().encode(request.getPassword()));

        userRepository.save(user);
        return UserResponse.toResponse(user);
    }

    public void delete(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userRepository.deleteById(id);
    }

    //TODO: change password method
    //TODO: user profile pictures, including adding, changing and maybe removing
    //TODO: real auth eventually. for now just seed one user and scope everything to that one
}
