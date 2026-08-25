package org.craft.backend.service;

import lombok.Data;
import org.craft.backend.dto.ChangeEmailRequest;
import org.craft.backend.dto.CreateUserRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.exceptions.EmailAlreadyTakenException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Data
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;

    public UserResponse getMe(User user) {
        return toResponse(user);
    }

    public UserResponse getUser(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        return toResponse(user);
    }

    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with the " +
                "email " +
                "address " + email + " was not found"));
        return toResponse(user);
    }

    public UserResponse createUser(CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return toResponse(user);
    }

    public UserResponse renameUser(UUID id, RenameUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        user.setName(request.getName());

        userRepository.save(user);
        return toResponse(user);
    }

    public User changeEmail(UUID id, ChangeEmailRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        String newEmail = request.getEmail().toLowerCase();
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new EmailAlreadyTakenException("Unable to update email");
            }
        });
        user.setEmail(newEmail);

        userRepository.save(user);
        return user;
    }

    public UserResponse editUser(UUID id, CreateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " " +
                "not found"));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        return toResponse(user);
    }

    public void delete(UUID id) {
        userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));
        userRepository.deleteById(id);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    //TODO: change password method

    private UserResponse toResponse(User user) {
        UserResponse response = UserResponse.toResponse(user);
        response.setAvatarUrl(imageService.presignUrl(user.getAvatarKey()));
        return response;
    }
}
