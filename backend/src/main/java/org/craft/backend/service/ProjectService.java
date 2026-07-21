package org.craft.backend.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.CreateProjectRequest;
import org.craft.backend.dto.EditProjectRequest;
import org.craft.backend.dto.ProjectResponse;
import org.craft.backend.enums.Status;
import org.craft.backend.exceptions.ProjectNotFoundException;
import org.craft.backend.model.Project;
import org.craft.backend.model.Tag;
import org.craft.backend.model.User;
import org.craft.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepository projectRepository;
    private final TagService tagService;

    public List<ProjectResponse> getProjectsForUser(User user) {
        return ProjectResponse.toResponses(projectRepository.findByUser(user));
    }

    public List<ProjectResponse> getProjectByName(User user, String query) {
        return ProjectResponse.toResponses(projectRepository.findByUserAndPatternNameContainingIgnoreCase(user, query));
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
                .status(Status.NOT_STARTED)
                .notes(request.getNotes())
                .imageUrl(request.getImageUrl())
                .tags(tags)
                .build();

        return ProjectResponse.toResponse(projectRepository.save(project));
    }

    public ProjectResponse updateStatus(User user, UUID uuid, Status status) {
        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));
        project.setStatus(status);

        return ProjectResponse.toResponse(projectRepository.save(project));
    }

    public ProjectResponse updateNotes(User user, UUID uuid, String notes) {
        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));
        project.setNotes(notes);

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

    public List<ProjectResponse> getByStatus(User user, Status status) {
        return ProjectResponse.toResponses(projectRepository.findByUserAndStatus(user, status));
    }

    public List<ProjectResponse> filterByTag(User user, Tag tag) {
        return ProjectResponse.toResponses(projectRepository.findByUserAndTag(user, tag));
    }

    public List<ProjectResponse> filterByTagName(User user, String tagName) {
        return tagService.getByName(tagName)
                .map(tag -> ProjectResponse.toResponses(projectRepository.findByUserAndTag(user, tag)))
                .orElse(List.of());
    }

    private List<Tag> getTagsByName(List<String> tagNames) {
        return tagNames.stream().map(tagService::getByNameOrCreate).toList();
    }
}
