package org.craft.backend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.craft.backend.enums.Status;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditProjectRequest {
    @NotBlank
    @Size(max = 255, message = "Pattern name must not exceed 255 characters")
    private String patternName;

    @NotBlank
    @Size(max = 100, message = "Craft must not exceed 100 characters")
    private String craft;

    @NotBlank
    @Size(max = 100, message = "Tool type must not exceed 100 characters")
    private String toolType;

    @NotBlank
    @Size(max = 100, message = "Tool size must not exceed 100 characters")
    private String toolSize;

    @Size(max = 100, message = "Yarn weight category must not exceed 100 characters")
    private String yarnWeightCategory;

    @Size(max = 255, message = "Yarn used must not exceed 255 characters")
    private String yarnUsed;

    @Min(0)
    private int amountUsed;

    @NotNull
    private Status status;

    @Size(max = 2048, message = "Image key must not exceed 2048 characters")
    private String imageKey;

    private String notes;

    private List<String> tags = new ArrayList<>();
}
