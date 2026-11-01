package org.craft.backend.service;

import lombok.Data;
import org.craft.backend.dto.ChangeEmailRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.exceptions.EmailAlreadyTakenException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.PasswordResetToken;
import org.craft.backend.model.Project;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Data
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final PasswordResetService passwordResetService;
    private final R2Service r2Service;

    public UserResponse getMe(User user) {
        return toResponse(user);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public UserResponse renameUser(UUID id, RenameUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        user.setName(request.getName());

        userRepository.save(user);
        return toResponse(user);
    }

    public User changeEmail(UUID id, ChangeEmailRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));
        String newEmail = request.getEmail().toLowerCase();
        userRepository.findByEmail(newEmail).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new EmailAlreadyTakenException("That email is already in use.");
            }
        });
        user.setEmail(newEmail);
        user.setTokenVersion(user.getTokenVersion() + 1);

        userRepository.save(user);
        return user;
    }

    @Transactional
    public void delete(UUID id) {
        User user = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        // Snapshot object-storage keys before the cascade removes the rows that
        // reference them: the avatar plus every project image.
        List<String> keys = new ArrayList<>();
        if (user.getAvatarKey() != null) {
            keys.add(user.getAvatarKey());
        }
        for (Project project : user.getProjects()) {
            if (project.getImageKey() != null) {
                keys.add(project.getImageKey());
            }
        }

        // Cascade removes the user's projects (and their project_tags join rows)
        // plus the password reset token. Tags are global, so they are left alone.
        userRepository.delete(user);

        // Best-effort: orphaned R2 objects cost money but are harmless, so a
        // failed cleanup must not roll back an already-successful deletion.
        for (String key : keys) {
            try {
                r2Service.deleteObject(key);
            } catch (Exception e) {
                log.warn("Failed to delete R2 object {} after account deletion", key, e);
            }
        }
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public PasswordResetToken generatePasswordResetTokenForUser(User user) {
        String token = UUID.randomUUID().toString();

        return passwordResetService.saveToken(user, token);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    public void changePassword(User user, String oldPassword, String newPassword) {
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadCredentialsException("Invalid password.");
        }
        setPassword(user, newPassword);
    }

    public void setPassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserResponse toResponse(User user) {
        UserResponse response = UserResponse.toResponse(user);
        response.setAvatarUrl(imageService.presignUrl(user.getAvatarKey()));
        return response;
    }
}
