package org.craft.backend.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.ForgotPasswordRequest;
import org.craft.backend.dto.ResetPasswordRequest;
import org.craft.backend.service.PasswordResetService;
import org.craft.backend.service.PasswordResetWorkflowService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PasswordResetController {
    private final PasswordResetWorkflowService passwordResetWorkflowService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/auth/password-reset/request")
    public ResponseEntity<?> sendPasswordResetEmail(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetWorkflowService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/auth/password-reset/verify")
    public ResponseEntity<?> verifyToken(@RequestParam("token") String token) {
        passwordResetService.validatePasswordResetTokenOrThrow(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/password-reset/confirm")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ResetPasswordRequest newPassword) {
        passwordResetWorkflowService.resetPassword(newPassword.getToken(), newPassword.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
