package org.craft.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RenameUserRequest {
    @NotBlank
    @Size(min = 4, max = 40)
    private String name;
}
