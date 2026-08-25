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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private static final Logger log = LoggerFactory.getLogger(ProjectService.class);
    private final ProjectRepository projectRepository;
    private final TagService tagService;
    private final R2Service r2Service;
    private final ImageService imageService;

    public List<ProjectResponse> getProjects(User user) {
        return projectRepository.findByUser(user).stream().map(this::toResponse).toList();
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

        return projects.stream().map(this::toResponse).toList();
    }

    public ProjectResponse createProject(User user, CreateProjectRequest request) {
        List<Tag> tags = getTagsByName(request.getTags());

        Project project = Project.builder()
                .user(user)
                .patternName(request.getPatternName())
                .craft(request.getCraft())
                .toolType(request.getToolType())
                .toolSize(request.getToolSize())
                .yarnWeightCategory(request.getYarnWeightCategory())
                .yarnUsed(request.getYarnUsed())
                .status(Status.TO_DO)
                .notes(request.getNotes())
                .imageKey(request.getImageKey())
                .tags(tags)
                .build();

        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse updateStatus(User user, UUID uuid, UpdateStatusRequest request) {
        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));
        project.setStatus(request.getStatus());

        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse updateNotes(User user, UUID uuid, UpdateNotesRequest request) {
        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));
        project.setNotes(request.getNotes());

        return toResponse(projectRepository.save(project));
    }

    public ProjectResponse editProject(User user, UUID uuid, EditProjectRequest  request) {

        Project project = projectRepository.findByIdAndUser(uuid, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + uuid + " not found"));

        List<Tag> tags = getTagsByName(request.getTags());

        project.setPatternName(request.getPatternName());
        project.setCraft(request.getCraft());
        project.setToolType(request.getToolType());
        project.setToolSize(request.getToolSize());
        project.setYarnWeightCategory(request.getYarnWeightCategory());
        project.setYarnUsed(request.getYarnUsed());
        project.setAmountUsed(request.getAmountUsed());
        project.setImageKey(request.getImageKey());
        project.setStatus(request.getStatus());
        project.setNotes(request.getNotes());
        project.setTags(tags);

        return toResponse(projectRepository.save(project));
    }

    @Transactional
    public void delete(User user, UUID id) {
        Project project = projectRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ProjectNotFoundException("Project with id " + id + " not found"));

        if (project.getImageKey() != null) {
            try {
                r2Service.deleteObject(project.getImageKey());
            } catch (Exception e) {
                log.warn("Failed to delete image {} for project {}", project.getImageKey(), id, e);
            }
        }

        projectRepository.delete(project);
    }

    private List<Tag> getTagsByName(List<String> tagNames) {
        return new ArrayList<>(tagNames.stream().map(tagService::getByNameOrCreate).toList());
    }

    private ProjectResponse toResponse(Project project) {
        ProjectResponse response = ProjectResponse.toResponse(project);
        response.setImageUrl(imageService.presignUrl(project.getImageKey()));
        return response;
    }
}
