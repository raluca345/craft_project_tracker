package org.craft.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.craft.backend.validation.StrongPassword;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 4, max = 40, message = "Name must be between 4 and 40 characters")
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @StrongPassword
    @Size(max = 72, message = "Password must not exceed 72 characters")
    private String password;
}
