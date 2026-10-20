package org.craft.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    @Email
    @Size(max = 254, message = "Email must not exceed 254 characters")
    private String email;

    @NotBlank
    private String password;
}
