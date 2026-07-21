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
    @Size(max = 255)
    private String patternName;

    @NotBlank
    @Size(max = 100)
    private String craft;

    @NotBlank
    @Size(max = 100)
    private String toolType;

    @Size(max = 100)
    private String yarnWeightCategory;

    @Size(max = 255)
    private String yarnUsed;

    @Min(0)
    private int amountUsed;

    @NotNull
    private Status status;

    @Size(max = 2048)
    private String imageUrl;

    private String notes;

    private List<String> tags = new ArrayList<>();
}
