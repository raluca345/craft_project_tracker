package org.craft.backend.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.craft.backend.config.AuthHelper;
import org.craft.backend.dto.CreateProjectRequest;
import org.craft.backend.dto.EditProjectRequest;
import org.craft.backend.dto.ProjectResponse;
import org.craft.backend.dto.UpdateNotesRequest;
import org.craft.backend.dto.UpdateStatusRequest;
import org.craft.backend.enums.Status;
import org.craft.backend.model.User;
import org.craft.backend.service.JwtService;
import org.craft.backend.service.ProjectService;
import org.craft.backend.support.TestUsers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(ProjectController.class)
public class ProjectControllerTest {

    private final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ProjectService projectService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private AuthHelper authHelper;

    private User givenAuthenticatedUser() {
        User user = TestUsers.user();
        when(authHelper.getCurrentUser()).thenReturn(user);
        return user;
    }

    private ProjectResponse givenProjectResponse() {
        ProjectResponse response = new ProjectResponse();
        response.setId(UUID.randomUUID());
        response.setPatternName("Granny Square Blanket");
        response.setCraft("Crochet");
        response.setToolType("5mm Hook");
        response.setToolSize("5mm");
        response.setYarnWeightCategory("Worsted");
        response.setYarnUsed("1000g");
        response.setAmountUsed(500);
        response.setImageKey("projects/123.png");
        response.setNotes("A cozy blanket");
        response.setStatus(Status.IN_PROGRESS);
        response.setTags(List.of("blanket", "crochet"));
        return response;
    }

    @Nested
    class GetProjectsEndpoint {

        @Test
        @WithMockUser
        void whenAuthenticated_thenReturnsProjects() throws Exception {
            User user = givenAuthenticatedUser();
            List<ProjectResponse> projects = List.of(givenProjectResponse());
            when(projectService.getProjects(user)).thenReturn(projects);

            mockMvc.perform(get("/api/v1/me/projects")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].patternName").value("Granny Square Blanket"))
                    .andExpect(jsonPath("$[0].craft").value("Crochet"));

            verify(projectService).getProjects(user);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/me/projects"))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(projectService, authHelper);
        }
    }

    @Nested
    class SearchEndpoint {

        @Test
        @WithMockUser
        void whenValidQuery_thenReturnsSearchResults() throws Exception {
            User user = givenAuthenticatedUser();
            List<ProjectResponse> projects = List.of(givenProjectResponse());
            when(projectService.search(user, "blanket")).thenReturn(projects);

            mockMvc.perform(get("/api/v1/me/projects/search")
                    .param("query", "blanket")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].patternName").value("Granny Square Blanket"));

            verify(projectService).search(user, "blanket");
        }

        @Test
        @WithMockUser
        void whenMissingQuery_thenReturnsBadRequest() throws Exception {
            givenAuthenticatedUser();

            mockMvc.perform(get("/api/v1/me/projects/search")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectService);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/me/projects/search")
                    .param("query", "blanket"))
                    .andExpect(status().isUnauthorized());

            verifyNoInteractions(projectService, authHelper);
        }
    }

    @Nested
    class CreateProjectEndpoint {

        @Test
        @WithMockUser
        void whenValidRequest_thenReturnsCreatedProject() throws Exception {
            User user = givenAuthenticatedUser();
            CreateProjectRequest request = new CreateProjectRequest();
            request.setPatternName("Granny Square Blanket");
            request.setCraft("Crochet");
            request.setToolType("5mm Hook");
            request.setToolSize("5mm");
            request.setYarnWeightCategory("Worsted");
            request.setYarnUsed("1000g");
            request.setNotes("A cozy blanket");
            request.setTags(List.of("blanket", "crochet"));

            ProjectResponse response = givenProjectResponse();
            when(projectService.createProject(eq(user), any(CreateProjectRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(post("/api/v1/me/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patternName").value("Granny Square Blanket"))
                    .andExpect(jsonPath("$.craft").value("Crochet"));

            verify(projectService).createProject(user, request);
        }

        @Test
        @WithMockUser
        void whenMissingPatternName_thenReturnsBadRequest() throws Exception {
            givenAuthenticatedUser();
            CreateProjectRequest request = new CreateProjectRequest();

            mockMvc.perform(post("/api/v1/me/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectService);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            CreateProjectRequest request = new CreateProjectRequest();
            request.setPatternName("Test");

            mockMvc.perform(post("/api/v1/me/projects")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(projectService, authHelper);
        }
    }

    @Nested
    class UpdateStatusEndpoint {

        @Test
        @WithMockUser
        void whenValidRequest_thenReturnsUpdatedProject() throws Exception {
            User user = givenAuthenticatedUser();
            UUID id = UUID.randomUUID();
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(Status.FINISHED);

            ProjectResponse response = givenProjectResponse();
            response.setStatus(Status.FINISHED);
            when(projectService.updateStatus(eq(user), eq(id), any(UpdateStatusRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/v1/me/projects/{id}/status", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FINISHED"));

            verify(projectService).updateStatus(user, id, request);
        }

        @Test
        @WithMockUser
        void whenMissingStatus_thenReturnsBadRequest() throws Exception {
            givenAuthenticatedUser();
            UUID id = UUID.randomUUID();
            UpdateStatusRequest request = new UpdateStatusRequest();

            mockMvc.perform(patch("/api/v1/me/projects/{id}/status", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectService);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            UUID id = UUID.randomUUID();
            UpdateStatusRequest request = new UpdateStatusRequest();
            request.setStatus(Status.FINISHED);

            mockMvc.perform(patch("/api/v1/me/projects/{id}/status", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(projectService, authHelper);
        }
    }

    @Nested
    class UpdateNotesEndpoint {

        @Test
        @WithMockUser
        void whenValidRequest_thenReturnsUpdatedProject() throws Exception {
            User user = givenAuthenticatedUser();
            UUID id = UUID.randomUUID();
            UpdateNotesRequest request = new UpdateNotesRequest();
            request.setNotes("Updated notes");

            ProjectResponse response = givenProjectResponse();
            response.setNotes("Updated notes");
            when(projectService.updateNotes(eq(user), eq(id), any(UpdateNotesRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(patch("/api/v1/me/projects/{id}/notes", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.notes").value("Updated notes"));

            verify(projectService).updateNotes(user, id, request);
        }

        @Test
        @WithMockUser
        void whenMissingBody_thenReturnsBadRequest() throws Exception {
            givenAuthenticatedUser();
            UUID id = UUID.randomUUID();

            mockMvc.perform(patch("/api/v1/me/projects/{id}/notes", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("")
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectService);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            UUID id = UUID.randomUUID();
            UpdateNotesRequest request = new UpdateNotesRequest();
            request.setNotes("Updated notes");

            mockMvc.perform(patch("/api/v1/me/projects/{id}/notes", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(projectService, authHelper);
        }
    }

    @Nested
    class EditProjectEndpoint {

        @Test
        @WithMockUser
        void whenValidRequest_thenReturnsUpdatedProject() throws Exception {
            User user = givenAuthenticatedUser();
            UUID id = UUID.randomUUID();
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

            ProjectResponse response = givenProjectResponse();
            response.setPatternName("Updated Name");
            when(projectService.editProject(eq(user), eq(id), any(EditProjectRequest.class)))
                    .thenReturn(response);

            mockMvc.perform(put("/api/v1/me/projects/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.patternName").value("Updated Name"));

            verify(projectService).editProject(user, id, request);
        }

        @Test
        @WithMockUser
        void whenMissingPatternName_thenReturnsBadRequest() throws Exception {
            givenAuthenticatedUser();
            UUID id = UUID.randomUUID();
            EditProjectRequest request = new EditProjectRequest();

            mockMvc.perform(put("/api/v1/me/projects/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(projectService);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            UUID id = UUID.randomUUID();
            EditProjectRequest request = new EditProjectRequest();
            request.setPatternName("Test");

            mockMvc.perform(put("/api/v1/me/projects/{id}", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(projectService, authHelper);
        }
    }

    @Nested
    class DeleteProjectEndpoint {

        @Test
        @WithMockUser
        void whenAuthenticated_thenDeletesProjectAndReturnsNoContent() throws Exception {
            User user = givenAuthenticatedUser();
            UUID id = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/me/projects/{id}", id)
                    .with(csrf()))
                    .andExpect(status().isNoContent());

            verify(projectService).delete(user, id);
        }

        @Test
        void whenUnauthenticated_thenReturnsUnauthorized() throws Exception {
            UUID id = UUID.randomUUID();

            mockMvc.perform(delete("/api/v1/me/projects/{id}", id))
                    .andExpect(status().isForbidden());

            verifyNoInteractions(projectService, authHelper);
        }
    }
}

