package org.craft.backend.controller;

import lombok.RequiredArgsConstructor;
import org.craft.backend.dto.CreateProjectRequest;
import org.craft.backend.dto.EditProjectRequest;
import org.craft.backend.dto.ProjectResponse;
import org.craft.backend.dto.UpdateNotesRequest;
import org.craft.backend.dto.UpdateStatusRequest;
import org.craft.backend.model.User;
import org.craft.backend.service.ProjectService;
import org.craft.backend.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/me/projects")
public class ProjectController {
    private final ProjectService projectService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getProjects() {
        User user = userService.getFirst();
        List<ProjectResponse> projects = projectService.getProjects(user);
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProjectResponse>> search(@RequestParam String query) {
        User user = userService.getFirst();
        List<ProjectResponse> projects = projectService.search(user, query);
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        User user = userService.getFirst();
        ProjectResponse project = projectService.createProject(user, request);
        return ResponseEntity.ok(project);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ProjectResponse> updateStatus(@PathVariable UUID id,
                                                        @Valid @RequestBody UpdateStatusRequest request) {
        User user = userService.getFirst();
        ProjectResponse project = projectService.updateStatus(user, id, request);
        return ResponseEntity.ok(project);
    }

    @PatchMapping("/{id}/notes")
    public ResponseEntity<ProjectResponse> updateNotes(@PathVariable UUID id, @Valid @RequestBody UpdateNotesRequest request) {
        User user = userService.getFirst();
        ProjectResponse project = projectService.updateNotes(user, id, request);
        return ResponseEntity.ok(project);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> editProject(@PathVariable UUID id, @Valid @RequestBody EditProjectRequest request) {
        User user = userService.getFirst();
        ProjectResponse project = projectService.editProject(user, id, request);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        User user = userService.getFirst();
        projectService.delete(user, id);
        return ResponseEntity.ok().build();
    }
}
