package org.craft.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.craft.backend.validation.StrongPassword;

@Data
public class ResetPasswordRequest {
    @NotBlank
    private String token;

    @NotBlank
    @StrongPassword
    @Size(max = 72, message = "Password must not exceed 72 characters")
    private String newPassword;
}
