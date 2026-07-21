package org.craft.backend.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class EditProjectRequest {
    private String patternName;
    private String craft;
    private String toolType;
    private String yarnWeightCategory;
    private String yarnUsed;
    private int amountUsed;
    private String status;
    private String imageUrl;
    private String notes;
    private List<String> tags = new ArrayList<>();
}
