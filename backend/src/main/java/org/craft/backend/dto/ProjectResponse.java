package org.craft.backend.dto;

import lombok.Data;
import org.craft.backend.enums.Status;
import org.craft.backend.model.Project;
import org.craft.backend.model.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class ProjectResponse {
    private UUID id;
    private String patternName;
    private String craft;
    private String toolType;
    private String yarnWeightCategory;
    private String yarnUsed;
    private int amountUsed;
    private String imageUrl;
    private String notes;
    private Status status;
    private List<String> tags = new ArrayList<>();

    public static ProjectResponse toResponse(Project project) {
        ProjectResponse response = new ProjectResponse();
        response.setId(project.getId());
        response.setPatternName(project.getPatternName());
        response.setCraft(project.getCraft());
        response.setToolType(project.getToolType());
        response.setYarnWeightCategory(project.getYarnWeightCategory());
        response.setYarnUsed(project.getYarnUsed());
        response.setAmountUsed(project.getAmountUsed());
        response.setImageUrl(project.getImageUrl());
        response.setNotes(project.getNotes());
        response.setStatus(project.getStatus());
        response.setTags(project.getTags().stream().map(Tag::getName).toList());
        return response;
    }

    public static List<ProjectResponse> toResponses(List<Project> projects) {
        return projects.stream().map(ProjectResponse::toResponse).toList();
    }
}
