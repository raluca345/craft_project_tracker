package org.craft.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.craft.backend.validation.StrongPassword;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String currentPassword;

    @NotBlank
    @StrongPassword
    @Size(max = 72)
    private String newPassword;
}
