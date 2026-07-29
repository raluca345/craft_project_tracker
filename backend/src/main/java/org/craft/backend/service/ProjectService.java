package org.craft.backend.service;

import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.CreateProjectRequest;
import org.craft.backend.dto.EditProjectRequest;
import org.craft.backend.dto.ProjectResponse;
import org.craft.backend.dto.UpdateNotesRequest;
import org.craft.backend.dto.UpdateStatusRequest;
import org.craft.backend.enums.Status;
import org.craft.backend.exceptions.ProjectNotFoundException;
import org.craft.backend.model.Project;
import org.craft.backend.model.Tag;
import org.craft.backend.model.User;
import org.craft.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TagService tagService;

    public List<ProjectResponse> getProjects(User user) {
        return ProjectResponse.toResponses(projectRepository.findByUser(user));
    }

    public List<ProjectResponse> search(User user, String query) {
        String[] tokens = query.trim().split("\\s+");

        List<String> tagNames = new ArrayList<>();
        List<String> nameTokens = new ArrayList<>();

        for (String token : tokens) {
            if (token.startsWith("#") && token.length() > 1) {
                tagNames.add(token.substring(1));
            } else {
                nameTokens.add(token);
            }
        }

        String nameQuery = String.join(" ", nameTokens);

        List<Project> projects;
        if (!tagNames.isEmpty()) {
            projects = projectRepository.searchByUserAndNameAndAllTags(user, nameQuery, tagNames, tagNames.size());
        } else {
            projects = projectRepository.findByUserAndPatternNameContainingIgnoreCase(user, nameQuery);
        }

        return ProjectResponse.toResponses(projects);
    }

    public ProjectResponse createProject(User user, CreateProjectRequest request) {
        List<Tag> tags = getTagsByName(request.getTags());

        Project project = Project.builder()
                .user(user)
                .patternName(request.getPatternName())
                .craft(request.getCraft())
                .toolType(request.getToolType())
                .yarnWeightCategory(request.getYarnWeightCategory())
                .yarnUsed(request.getYarnUsed())
                .status(Status.TO_DO)
                .notes(request.getNotes())
                .imageUrl(request.getImageUrl())
                .tags(tags)
                .build();

        return ProjectResponse.toResponse(projectRepository.save(project));
    }

    public ProjectResponse updateStatus(User user, UUID uuid, UpdateStatusRequest request) {
        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));
        project.setStatus(request.getStatus());

        return ProjectResponse.toResponse(projectRepository.save(project));
    }

    public ProjectResponse updateNotes(User user, UUID uuid, UpdateNotesRequest request) {
        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));
        project.setNotes(request.getNotes());

        return ProjectResponse.toResponse(projectRepository.save(project));
    }

    public ProjectResponse editProject(User user, UUID uuid, EditProjectRequest  request) {

        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));

        List<Tag> tags = getTagsByName(request.getTags());

        project.setPatternName(request.getPatternName());
        project.setCraft(request.getCraft());
        project.setToolType(request.getToolType());
        project.setYarnWeightCategory(request.getYarnWeightCategory());
        project.setYarnUsed(request.getYarnUsed());
        project.setAmountUsed(request.getAmountUsed());
        project.setImageUrl(request.getImageUrl());
        project.setStatus(request.getStatus());
        project.setNotes(request.getNotes());
        project.setTags(tags);

        return ProjectResponse.toResponse(projectRepository.save(project));
    }

    public void delete(User user, UUID id) {
        projectRepository.deleteByUserAndId(user, id);
    }

    private List<Tag> getTagsByName(List<String> tagNames) {
        return tagNames.stream().map(tagService::getByNameOrCreate).toList();
    }
}
