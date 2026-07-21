package org.craft.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateProjectRequest {
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

    @Size(max = 2048)
    private String imageUrl;

    private String notes;

    private List<String> tags = new ArrayList<>();
}
