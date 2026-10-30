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
import java.util.UUID;

import org.craft.backend.dto.ChangeEmailRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.exceptions.EmailAlreadyTakenException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.Project;
import org.craft.backend.model.User;
import org.craft.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ImageService imageService;
    @Mock
    private PasswordResetService passwordResetService;
    @Mock
    private R2Service r2Service;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder, imageService, passwordResetService, r2Service);
    }

    private User buildUser(UUID id, String name, String email) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setPassword("encoded-password");
        return user;
    }

    void getMe_returnsResponseWithPresignedAvatarUrl() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Test User", "test@example.com");
        user.setAvatarKey("avatars/test.png");
        when(imageService.presignUrl("avatars/test.png")).thenReturn("https://presigned.url");

        UserResponse response = userService.getMe(user);

        assertNotNull(response);
        assertEquals("Test User", response.getName());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("https://presigned.url", response.getAvatarUrl());
        verify(imageService).presignUrl("avatars/test.png");
    }

    @Test
    void renameUser_renamesUserAndReturnsResponseWithPresignedAvatarUrl() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Old Name", "test@example.com");
        user.setAvatarKey("avatars/test.png");

        RenameUserRequest request = new RenameUserRequest();
        request.setName("New Name");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(imageService.presignUrl("avatars/test.png")).thenReturn("https://presigned.url");

        UserResponse response = userService.renameUser(id, request);

        assertNotNull(response);
        assertEquals("New Name", response.getName());
        assertEquals("https://presigned.url", response.getAvatarUrl());
        verify(userRepository).save(user);
    }

    @Test
    void renameUser_userNotFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        RenameUserRequest request = new RenameUserRequest();
        request.setName("New Name");

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.renameUser(id, request));
    }

    @Test
    void changeEmail_normalizesAddressAndIncrementsTokenVersion() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Test User", "old@example.com");
        user.setTokenVersion(0);

        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setEmail("NEW@Example.COM");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeEmail(id, request);

        assertEquals("new@example.com", result.getEmail());
        assertEquals(1, result.getTokenVersion());
        verify(userRepository).save(user);
    }

    @Test
    void changeEmail_addressOwnedByAnotherUser_throwsEmailAlreadyTakenException() {
        UUID id = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        User user = buildUser(id, "Test User", "old@example.com");
        User otherUser = buildUser(otherId, "Other User", "taken@example.com");

        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setEmail("taken@example.com");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("taken@example.com")).thenReturn(Optional.of(otherUser));

        assertThrows(EmailAlreadyTakenException.class, () -> userService.changeEmail(id, request));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changeEmail_sameUserKeepsEmail_doesNotThrow() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Test User", "same@example.com");
        user.setTokenVersion(2);

        ChangeEmailRequest request = new ChangeEmailRequest();
        request.setEmail("SAME@Example.COM");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("same@example.com")).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.changeEmail(id, request);

        assertEquals("same@example.com", result.getEmail());
        assertEquals(3, result.getTokenVersion());
    }

    @Test
    void changePassword_wrongCurrentPassword_throwsBadCredentialsException() {
        User user = buildUser(UUID.randomUUID(), "Test User", "test@example.com");
        user.setPassword("encoded-old-password");

        when(passwordEncoder.matches("wrong-password", "encoded-old-password")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> userService.changePassword(user, "wrong-password", "new-password"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_correctCurrentPassword_savesEncodedReplacement() {
        User user = buildUser(UUID.randomUUID(), "Test User", "test@example.com");
        user.setPassword("encoded-old-password");

        when(passwordEncoder.matches("old-password", "encoded-old-password")).thenReturn(true);
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(userRepository.save(user)).thenReturn(user);

        userService.changePassword(user, "old-password", "new-password");

        assertEquals("encoded-new-password", user.getPassword());
        verify(passwordEncoder).encode("new-password");
        verify(userRepository).save(user);
    }

    @Test
    void delete_removesUserAndCleansUpR2Objects() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Test User", "test@example.com");
        user.setAvatarKey("avatars/test.png");
        Project project = new Project();
        project.setImageKey("projects/test.png");
        user.getProjects().add(project);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        userService.delete(id);

        verify(userRepository).delete(user);
        verify(r2Service).deleteObject("avatars/test.png");
        verify(r2Service).deleteObject("projects/test.png");
    }

    @Test
    void delete_r2CleanupFails_stillCompletesDeletion() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Test User", "test@example.com");
        user.setAvatarKey("avatars/test.png");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("R2 unavailable")).when(r2Service).deleteObject("avatars/test.png");

        userService.delete(id);

        verify(userRepository).delete(user);
        verify(r2Service).deleteObject("avatars/test.png");
    }

    @Test
    void delete_userNotFound_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.delete(id));
    }

    @Test
    void getUserByEmail_existingEmail_returnsUser() {
        String email = "test@example.com";
        User user = buildUser(UUID.randomUUID(), "Test User", email);
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User result = userService.getUserByEmail(email);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
    }

    @Test
    void getUserByEmail_nonExistingEmail_throwsUserNotFoundException() {
        String email = "missing@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByEmail(email));
    }

    @Test
    void getUserById_existingId_returnsUser() {
        UUID id = UUID.randomUUID();
        User user = buildUser(id, "Test User", "test@example.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        User result = userService.getUserById(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    void getUserById_nonExistingId_throwsUserNotFoundException() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(id));
    }

    @Test
    void setPassword_encodesAndSaves() {
        User user = buildUser(UUID.randomUUID(), "Test User", "test@example.com");
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");
        when(userRepository.save(user)).thenReturn(user);

        userService.setPassword(user, "new-password");

        assertEquals("encoded-new-password", user.getPassword());
        verify(passwordEncoder).encode("new-password");
        verify(userRepository).save(user);
    }
}
