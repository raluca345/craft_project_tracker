package org.craft.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.craft.backend.config.AuthHelper;
import org.craft.backend.dto.AuthResponse;
import org.craft.backend.dto.ChangeEmailRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.model.User;
import org.craft.backend.service.ImageService;
import org.craft.backend.service.JwtService;
import org.craft.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final AuthHelper authHelper;
    private final ImageService imageService;
    private final JwtService jwtService;

    @PatchMapping("/me/rename")
    public ResponseEntity<UserResponse> rename(@Valid @RequestBody RenameUserRequest request) {
        User user = authHelper.getCurrentUser();
        UserResponse updated = userService.renameUser(user.getId(), request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        User user = authHelper.getCurrentUser();
        return ResponseEntity.ok(userService.getMe(user));
    }

    @PatchMapping("/me/email")
    public ResponseEntity<AuthResponse> changeEmail(@Valid @RequestBody ChangeEmailRequest request) {
        User user = authHelper.getCurrentUser();
        User updated = userService.changeEmail(user.getId(), request);
        String token = jwtService.generateToken(updated);
        return ResponseEntity.ok(AuthResponse.fromUser(token, updated, imageService.presignUrl(updated.getAvatarKey())));
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        User user = authHelper.getCurrentUser();
        ImageService.UploadedImage uploaded = imageService.upload(user, file);
        user.setAvatarKey(uploaded.key());
        userService.save(user);
        return ResponseEntity.ok(Map.of(
                "avatarKey", uploaded.key(),
                "avatarUrl", imageService.presignUrl(uploaded.key())));
    }
}
