package org.craft.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.stream.Stream;

import org.craft.backend.config.AuthHelper;
import org.craft.backend.dto.ChangeEmailRequest;
import org.craft.backend.dto.ChangePasswordRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.exceptions.ImageFileException;
import org.craft.backend.exceptions.UserNotFoundException;
import org.craft.backend.model.User;
import org.craft.backend.service.ImageService;
import org.craft.backend.service.JwtService;
import org.craft.backend.service.UserService;
import org.craft.backend.support.TestUsers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private ImageService imageService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsService userDetailsService;
    @MockitoBean
    private AuthHelper authHelper;

    private User givenAuthenticatedUser() {
        User user = TestUsers.user();
        when(authHelper.getCurrentUser()).thenReturn(user);
        return user;
    }

    @Nested
    class RenameEndpoint {
        @Test
        @WithMockUser
        void whenRenameRequestIsValid_thenReturnsRenamedUser() throws Exception {
            RenameUserRequest request = new RenameUserRequest();
            request.setName("test");

            User user = givenAuthenticatedUser();
            UserResponse response = TestUsers.userResponse(user);
            response.setName("test");

            when(userService.renameUser(eq(user.getId()), any(RenameUserRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/v1/me/rename")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("test"))
                    .andExpect(jsonPath("$.id").value(user.getId().toString()));

            verify(userService, times(1)).renameUser(eq(user.getId()),
                    argThat(renameRequest -> "test".equals(renameRequest.getName())));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidNames")
        @WithMockUser
        void whenRenameRequestIsInvalid_thenReturnsBadRequestWithoutCallingService(
                String ignoredDescription, String name) throws Exception {
            RenameUserRequest request = new RenameUserRequest();
            request.setName(name);

            mockMvc.perform(patch("/api/v1/me/rename")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService, authHelper);
        }

        private static Stream<Arguments> invalidNames() {
            return Stream.of(
                    Arguments.of("empty", ""),
                    Arguments.of("whitespace only", "   "),
                    Arguments.of("too short", "abc"),
                    Arguments.of("too long", "a".repeat(41)));
        }
    }

    @Nested
    class MeEndpoint {
        @Test
        @WithMockUser
        void whenAuthenticated_thenReturnsCurrentUser() throws Exception {
            User user = givenAuthenticatedUser();
            UserResponse response = TestUsers.userResponse(user);

            when(userService.getMe(user)).thenReturn(response);

            mockMvc.perform(get("/api/v1/me")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(user.getId().toString()))
                    .andExpect(jsonPath("$.name").value(user.getName()))
                    .andExpect(jsonPath("$.email").value(user.getEmail()));
        }

        @Test
        void whenNotLoggedIn_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/me"))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(authHelper, userService);
        }
    }

    @Nested
    class ChangeEmailEndpoint {
        @Test
        @WithMockUser
        void whenChangeEmailRequestIsValid_thenReturnsUpdatedUserAndRefreshedToken() throws Exception {
            User user = givenAuthenticatedUser();

            ChangeEmailRequest request = new ChangeEmailRequest();
            request.setEmail("test2@example.com");

            User updatedUser = TestUsers.user(
                    user.getId(),
                    user.getName(),
                    "test2@example.com");

            when(userService.changeEmail(
                    eq(user.getId()),
                    any(ChangeEmailRequest.class)))
                    .thenReturn(updatedUser);
            when(jwtService.generateToken(updatedUser))
                    .thenReturn("encoded-token-v2");

            mockMvc.perform(patch("/api/v1/me/email")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(user.getId().toString()))
                    .andExpect(jsonPath("$.email").value("test2@example.com"))
                    .andExpect(jsonPath("$.token").value("encoded-token-v2"));

            verify(userService).changeEmail(
                    eq(user.getId()),
                    argThat(changeRequest -> "test2@example.com".equals(changeRequest.getEmail())));
            verify(jwtService).generateToken(updatedUser);
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidEmails")
        @WithMockUser
        void whenChangeEmailRequestIsInvalid_thenReturnsBadRequestWithoutCallingService(
                String ignoredDescription, String email)
                throws Exception {
            ChangeEmailRequest request = new ChangeEmailRequest();
            request.setEmail(email);

            mockMvc.perform(patch("/api/v1/me/email")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService, authHelper);
        }

        private static Stream<Arguments> invalidEmails() {
            return Stream.of(
                    Arguments.of("empty", ""),
                    Arguments.of("bad format", "badFormat"),
                    Arguments.of("whitespace only", "   "),
                    Arguments.of("too long", "a".repeat(250) + "@example.com"));
        }

    }

    @Nested
    class ChangePasswordEndpoint {
        @Test
        @WithMockUser
        void whenPasswordChangeRequestIsValid_thenDelegatesAndReturnsNoContent() throws Exception {
            User user = givenAuthenticatedUser();

            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword(user.getPassword());
            request.setNewPassword("new-password");

            mockMvc.perform(patch("/api/v1/me/password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isNoContent())
                    .andExpect(content().string(""));

            verify(userService).changePassword(eq(user), eq("encoded-password"), eq("new-password"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidPasswords")
        @WithMockUser
        void whenNewPasswordIsInvalid_thenReturnsBadRequestWithoutCallingService(
                String ignoredDescription, String password) throws Exception {
            User user = givenAuthenticatedUser();
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword(user.getPassword());
            request.setNewPassword(password);

            mockMvc.perform(patch("/api/v1/me/password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(userService);
        }

        private static Stream<Arguments> invalidPasswords() {
            return Stream.of(
                    Arguments.of("empty", ""),
                    Arguments.of("too short", "short"),
                    Arguments.of("whitespace only", "   "),
                    Arguments.of("too long", "a".repeat(73)),
                    Arguments.of("null", null),
                    Arguments.of("only special characters", "!!!!!!!!"));
        }

        @Test
        @WithMockUser
        void whenCurrentPasswordIsIncorrect_thenReturnsUnauthorized() throws Exception {
            User user = givenAuthenticatedUser();
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("wrong-password");
            request.setNewPassword("irrelevant");

            doThrow(new BadCredentialsException("Bad credentials"))
                    .when(userService)
                    .changePassword(eq(user), anyString(), anyString());

            mockMvc.perform(patch("/api/v1/me/password")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());

            verify(userService).changePassword(
                    eq(user),
                    eq(request.getCurrentPassword()),
                    eq(request.getNewPassword()));
        }
    }

    @Nested
    class UploadAvatarEndpoint {
        @Test
        @WithMockUser
        void whenValidImageFile_thenSetsAsAvatarAndReturnsOk() throws Exception {
            User user = givenAuthenticatedUser();

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "avatar.png",
                    "image/png",
                    new byte[] { 1, 2, 3 });

            when(imageService.upload(eq(user), any(MultipartFile.class)))
                    .thenReturn(new ImageService.UploadedImage("user-id/avatar.png", "image/png"));
            when(imageService.presignUrl("user-id/avatar.png"))
                    .thenReturn("https://example.test/avatar.png");

            mockMvc.perform(multipart("/api/v1/me/avatar")
                    .with(csrf())
                    .file(file))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.avatarKey").value("user-id/avatar.png"))
                    .andExpect(jsonPath("$.avatarUrl").value("https://example.test/avatar.png"));

            verify(userService).save(user);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorizedWithoutUploadingAvatar() throws Exception {
            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "avatar.png",
                    "image/png",
                    new byte[] { 1, 2, 3 });

            mockMvc.perform(multipart("/api/v1/me/avatar")
                    .with(csrf())
                    .file(file))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(userService, imageService, authHelper);
        }

        @Test
        @WithMockUser
        void whenImageServiceRejectsUpload_thenReturnsBadRequestWithoutSavingAvatar() throws Exception {
            User user = givenAuthenticatedUser();

            MockMultipartFile file = new MockMultipartFile(
                    "file",
                    "avatar.png",
                    "text/plain",
                    "a, b, c".getBytes());

            when(imageService.upload(eq(user), any(MultipartFile.class)))
                    .thenThrow(new ImageFileException(
                            "Unsupported file type. Only PNG, JPEG and WebP images are accepted."));

            mockMvc.perform(multipart("/api/v1/me/avatar")
                    .with(csrf())
                    .file(file))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(
                            "Unsupported file type. Only PNG, JPEG and WebP images are accepted."));

            verify(imageService).upload(user, file);
            verifyNoInteractions(userService);
        }

        @Test
        @WithMockUser
        void whenFileIsMissing_thenReturnsBadRequestWithoutSavingAvater() throws Exception {

            mockMvc.perform(multipart("/api/v1/me/avatar")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(authHelper, imageService, userService);
        }
    }

    @Nested
    class DeleteAccountEndpoint {
        @Test
        @WithMockUser
        void whenAuthenticated_thenDeletesCurrentUserAndReturnsNoContent() throws Exception {
            User user = givenAuthenticatedUser();

            mockMvc.perform(delete("/api/v1/me")
                    .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(userService).delete(user.getId());
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(delete("/api/v1/me")
                    .with(csrf()))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(authHelper, userService);
        }

        @Test
        @WithMockUser
        void whenCurrentUserNoLongerExists_thenReturnsNotFound() throws Exception {
            // maybe the account was already deleted but the user is still logged in somehow
            User user = givenAuthenticatedUser();

            doThrow(new UserNotFoundException("User not found"))
                    .when(userService).delete(user.getId());

            mockMvc.perform(delete("/api/v1/me")
                    .with(csrf()))
                    .andExpect(status().isNotFound());

            verify(userService).delete(user.getId());
        }
    }
}
