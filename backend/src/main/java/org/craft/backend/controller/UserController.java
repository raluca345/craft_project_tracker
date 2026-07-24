package org.craft.backend.controller;

import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.CreateUserRequest;
import org.craft.backend.dto.RenameUserRequest;
import org.craft.backend.dto.UserResponse;
import org.craft.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        UserResponse user = userService.getUser(uuid);
        return ResponseEntity.ok(user);
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody CreateUserRequest request) {
        UserResponse user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PatchMapping("{id}/rename")
    //when i implement auth, replace with /me/rename and get user from the auth principal
    public ResponseEntity<UserResponse> renameUser(@RequestBody RenameUserRequest request, @PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        UserResponse user = userService.renameUser(uuid, request);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}/edit")
    public ResponseEntity<UserResponse> editUser(@RequestBody CreateUserRequest request, @PathVariable String id) {
        UUID uuid = UUID.fromString(id);
        UserResponse user = userService.editUser(uuid, request);
        return ResponseEntity.ok(user);
    }
}
