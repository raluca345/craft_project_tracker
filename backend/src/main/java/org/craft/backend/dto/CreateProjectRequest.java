package org.craft.backend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CreateProjectRequest {
    private String patternName;
    private String craft;
    private String toolType;
    private String yarnWeightCategory;
    private String yarnUsed;
    private String imageUrl;
    private String notes;
    private List<String> tags = new ArrayList<>();
}
