package org.craft.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.craft.backend.enums.Status;

@Data
public class UpdateStatusRequest {
    @NotNull
    private Status status;
}
