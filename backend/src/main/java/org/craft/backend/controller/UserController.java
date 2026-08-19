package org.craft.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.craft.backend.config.AuthHelper;
import org.craft.backend.dto.CreateUserRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.model.User;
import org.craft.backend.service.ImageService;
import org.craft.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserController {
    private final UserService userService;
    private final AuthHelper authHelper;
    private final ImageService imageService;

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable UUID id) {
        UserResponse user = userService.getUser(id);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PatchMapping("/users/{id}/rename")
    public ResponseEntity<UserResponse> renameUser(@Valid @RequestBody RenameUserRequest request, @PathVariable UUID id) {
        UserResponse user = userService.renameUser(id, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/users/{id}/edit")
    public ResponseEntity<UserResponse> editUser(@Valid @RequestBody CreateUserRequest request, @PathVariable UUID id) {
        UserResponse user = userService.editUser(id, request);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        User user = authHelper.getCurrentUser();
        ImageService.UploadedImage uploaded = imageService.upload(user, file);
        user.setAvatarKey(uploaded.key());
        userService.save(user);
        return ResponseEntity.ok(Map.of("avatarKey", uploaded.key()));
    }
}
