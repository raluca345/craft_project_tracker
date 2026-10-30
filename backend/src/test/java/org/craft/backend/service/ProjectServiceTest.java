package org.craft.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TagService tagService;
    @Mock
    private R2Service r2Service;
    @Mock
    private ImageService imageService;

    private ProjectService projectService;

    @BeforeEach
    void setUp() {
        projectService = new ProjectService(projectRepository, tagService, r2Service, imageService);
    }

    private User buildUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        return user;
    }

    private Project buildProject(UUID id, User user, String patternName) {
        Project project = new Project();
        project.setId(id);
        project.setUser(user);
        project.setPatternName(patternName);
        project.setCraft("Crochet");
        project.setToolType("5mm Hook");
        project.setToolSize("5mm");
        project.setStatus(Status.TO_DO);
        project.setTags(new java.util.ArrayList<>());
        return project;
    }

    private CreateProjectRequest buildCreateRequest(String patternName, List<String> tags) {
        CreateProjectRequest request = new CreateProjectRequest();
        request.setPatternName(patternName);
        request.setCraft("Crochet");
        request.setToolType("5mm Hook");
        request.setToolSize("5mm");
        request.setTags(tags != null ? tags : new java.util.ArrayList<>());
        return request;
    }

    @Nested
    class CreateProjectTests {

        @Test
        void savesProjectWithToDoStatusAndResolvesTags() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            UUID projectId = UUID.randomUUID();

            Tag tag1 = new Tag("crochet");
            Tag tag2 = new Tag("blanket");
            when(tagService.getByNameOrCreate("crochet")).thenReturn(tag1);
            when(tagService.getByNameOrCreate("blanket")).thenReturn(tag2);

            CreateProjectRequest request = buildCreateRequest("Granny Square Blanket", List.of("crochet", "blanket"));

            Project savedProject = buildProject(projectId, user, "Granny Square Blanket");
            savedProject.setTags(List.of(tag1, tag2));
            when(projectRepository.save(org.mockito.ArgumentMatchers.any(Project.class))).thenReturn(savedProject);
            when(imageService.presignUrl(null)).thenReturn(null);

            ProjectResponse response = projectService.createProject(user, request);

            assertNotNull(response);
            assertEquals("Granny Square Blanket", response.getPatternName());
            assertEquals(Status.TO_DO, response.getStatus());
            assertEquals(2, response.getTags().size());
            verify(tagService).getByNameOrCreate("crochet");
            verify(tagService).getByNameOrCreate("blanket");
            verify(projectRepository).save(org.mockito.ArgumentMatchers.any(Project.class));
        }

        @Test
        void withNoTags_savesProjectWithEmptyTagList() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            UUID projectId = UUID.randomUUID();

            CreateProjectRequest request = buildCreateRequest("Simple Project", List.of());

            Project savedProject = buildProject(projectId, user, "Simple Project");
            when(projectRepository.save(org.mockito.ArgumentMatchers.any(Project.class))).thenReturn(savedProject);
            when(imageService.presignUrl(null)).thenReturn(null);

            ProjectResponse response = projectService.createProject(user, request);

            assertNotNull(response);
            assertEquals("Simple Project", response.getPatternName());
            assertEquals(Status.TO_DO, response.getStatus());
            verify(projectRepository).save(org.mockito.ArgumentMatchers.any(Project.class));
        }
    }

    @Nested
    class GetProjectsTests {

        @Test
        void returnsOnlyProjectsForSuppliedUser() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            UUID projectId1 = UUID.randomUUID();
            UUID projectId2 = UUID.randomUUID();

            Project project1 = buildProject(projectId1, user, "Project 1");
            Project project2 = buildProject(projectId2, user, "Project 2");
            when(projectRepository.findByUser(user)).thenReturn(List.of(project1, project2));
            when(imageService.presignUrl(null)).thenReturn(null);

            List<ProjectResponse> responses = projectService.getProjects(user);

            assertEquals(2, responses.size());
            assertEquals("Project 1", responses.get(0).getPatternName());
            assertEquals("Project 2", responses.get(1).getPatternName());
            verify(projectRepository).findByUser(user);
        }

        @Test
        void noProjects_returnsEmptyList() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            when(projectRepository.findByUser(user)).thenReturn(List.of());

            List<ProjectResponse> responses = projectService.getProjects(user);

            assertEquals(0, responses.size());
        }
    }

    @Nested
    class SearchTests {

        @Test
        void textOnlyQuery_callsFindByUserAndPatternNameContainingIgnoreCase() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            UUID projectId = UUID.randomUUID();

            Project project = buildProject(projectId, user, "Blanket Pattern");
            when(projectRepository.findByUserAndPatternNameContainingIgnoreCase(user, "blanket"))
                    .thenReturn(List.of(project));
            when(imageService.presignUrl(null)).thenReturn(null);

            List<ProjectResponse> responses = projectService.search(user, "blanket");

            assertEquals(1, responses.size());
            assertEquals("Blanket Pattern", responses.get(0).getPatternName());
            verify(projectRepository).findByUserAndPatternNameContainingIgnoreCase(user, "blanket");
        }

        @Test
        void tagOnlyQuery_callsSearchByUserAndNameAndAllTags() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            UUID projectId = UUID.randomUUID();

            Project project = buildProject(projectId, user, "My Project");
            when(projectRepository.searchByUserAndNameAndAllTags(user, "", List.of("crochet"), 1))
                    .thenReturn(List.of(project));
            when(imageService.presignUrl(null)).thenReturn(null);

            List<ProjectResponse> responses = projectService.search(user, "#crochet");

            assertEquals(1, responses.size());
            verify(projectRepository).searchByUserAndNameAndAllTags(user, "", List.of("crochet"), 1);
        }

        @Test
        void mixedQuery_separatesNameAndTagTokens() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            UUID projectId = UUID.randomUUID();

            Project project = buildProject(projectId, user, "Blanket Pattern");
            when(projectRepository.searchByUserAndNameAndAllTags(user, "blanket", List.of("crochet"), 1))
                    .thenReturn(List.of(project));
            when(imageService.presignUrl(null)).thenReturn(null);

            List<ProjectResponse> responses = projectService.search(user, "blanket #crochet");

            assertEquals(1, responses.size());
            verify(projectRepository).searchByUserAndNameAndAllTags(user, "blanket", List.of("crochet"), 1);
        }

        @Test
        void multipleTags_passesAllTagNamesAndCount() {
            UUID userId = UUID.randomUUID();
            User user = buildUser(userId);
            UUID projectId = UUID.randomUUID();

            Project project = buildProject(projectId, user, "My Project");
            when(projectRepository.searchByUserAndNameAndAllTags(user, "", List.of("crochet", "blanket"), 2))
                    .thenReturn(List.of(project));
            when(imageService.presignUrl(null)).thenReturn(null);

            List<ProjectResponse> responses = projectService.search(user, "#crochet #blanket");

            assertEquals(1, responses.size());
            verify(projectRepository).searchByUserAndNameAndAllTags(user, "", List.of("crochet", "blanket"), 2);
        }
    }

    @Nested
    class UpdateStatusTests {

        @Test
        void existingProject_updatesStatusAndReturnsResponse() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);
            Project project = buildProject(projectId, user, "My Project");

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(Status.FINISHED);

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);
            when(imageService.presignUrl(null)).thenReturn(null);

            ProjectResponse response = projectService.updateStatus(user, projectId, request);

            assertNotNull(response);
            assertEquals(Status.FINISHED, project.getStatus());
            verify(projectRepository).save(project);
        }

        @Test
        void nonExistingProject_throwsProjectNotFoundException() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);

            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(Status.FINISHED);

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class,
                    () -> projectService.updateStatus(user, projectId, request));
        }
    }

    @Nested
    class UpdateNotesTests {

        @Test
        void existingProject_updatesNotesAndReturnsResponse() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);
            Project project = buildProject(projectId, user, "My Project");

            UpdateNotesRequest request = new UpdateNotesRequest();
            request.setNotes("Updated notes");

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);
            when(imageService.presignUrl(null)).thenReturn(null);

            ProjectResponse response = projectService.updateNotes(user, projectId, request);

            assertNotNull(response);
            assertEquals("Updated notes", project.getNotes());
            verify(projectRepository).save(project);
        }

        @Test
        void nonExistingProject_throwsProjectNotFoundException() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);

            UpdateNotesRequest request = new UpdateNotesRequest();
            request.setNotes("Updated notes");

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class,
                    () -> projectService.updateNotes(user, projectId, request));
        }
    }

    @Nested
    class EditProjectTests {

        @Test
        void existingProject_replacesAllEditableFieldsAndTags() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);
            Project project = buildProject(projectId, user, "Old Name");

            Tag tag1 = new Tag("knitting");
            Tag tag2 = new Tag("scarf");
            when(tagService.getByNameOrCreate("knitting")).thenReturn(tag1);
            when(tagService.getByNameOrCreate("scarf")).thenReturn(tag2);

            EditProjectRequest request = new EditProjectRequest();
            request.setPatternName("Updated Name");
            request.setCraft("Knitting");
            request.setToolType("4mm Needles");
            request.setToolSize("4mm");
            request.setYarnWeightCategory("DK");
            request.setYarnUsed("800g");
            request.setAmountUsed(300);
            request.setStatus(Status.FINISHED);
            request.setImageKey("projects/456.png");
            request.setNotes("Updated notes");
            request.setTags(List.of("knitting", "scarf"));

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.of(project));
            when(projectRepository.save(project)).thenReturn(project);
            when(imageService.presignUrl("projects/456.png")).thenReturn("https://presigned.url");

            ProjectResponse response = projectService.editProject(user, projectId, request);

            assertNotNull(response);
            assertEquals("Updated Name", project.getPatternName());
            assertEquals("Knitting", project.getCraft());
            assertEquals("4mm Needles", project.getToolType());
            assertEquals("4mm", project.getToolSize());
            assertEquals("DK", project.getYarnWeightCategory());
            assertEquals("800g", project.getYarnUsed());
            assertEquals(300, project.getAmountUsed());
            assertEquals(Status.FINISHED, project.getStatus());
            assertEquals("projects/456.png", project.getImageKey());
            assertEquals("Updated notes", project.getNotes());
            assertEquals(2, project.getTags().size());
            verify(tagService).getByNameOrCreate("knitting");
            verify(tagService).getByNameOrCreate("scarf");
            verify(projectRepository).save(project);
        }

        @Test
        void nonExistingProject_throwsProjectNotFoundException() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);

            EditProjectRequest request = new EditProjectRequest();
            request.setPatternName("Updated Name");
            request.setCraft("Knitting");
            request.setToolType("4mm Needles");
            request.setToolSize("4mm");
            request.setStatus(Status.FINISHED);

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class,
                    () -> projectService.editProject(user, projectId, request));
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void existingProject_deletesProject() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);
            Project project = buildProject(projectId, user, "My Project");

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.of(project));

            projectService.delete(user, projectId);

            verify(projectRepository).delete(project);
        }

        @Test
        void existingProjectWithImage_deletesImageAndProject() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);
            Project project = buildProject(projectId, user, "My Project");
            project.setImageKey("projects/test.png");

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.of(project));

            projectService.delete(user, projectId);

            verify(r2Service).deleteObject("projects/test.png");
            verify(projectRepository).delete(project);
        }

        @Test
        void r2CleanupFails_stillDeletesProject() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);
            Project project = buildProject(projectId, user, "My Project");
            project.setImageKey("projects/test.png");

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.of(project));
            doThrow(new RuntimeException("R2 unavailable")).when(r2Service).deleteObject("projects/test.png");

            projectService.delete(user, projectId);

            verify(r2Service).deleteObject("projects/test.png");
            verify(projectRepository).delete(project);
        }

        @Test
        void nonExistingProject_throwsProjectNotFoundException() {
            UUID userId = UUID.randomUUID();
            UUID projectId = UUID.randomUUID();
            User user = buildUser(userId);

            when(projectRepository.findByIdAndUser(projectId, user)).thenReturn(Optional.empty());

            assertThrows(ProjectNotFoundException.class,
                    () -> projectService.delete(user, projectId));
        }
    }
}
