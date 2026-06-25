package com.conductor.workflow.api;

import com.conductor.workflow.service.WorkflowDefinitionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({WorkflowDefinitionController.class, WorkflowExceptionHandler.class})
class WorkflowExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private WorkflowDefinitionService workflowDefinitionService;
    @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void unknownWorkflow_returns404WithProblemDetail() throws Exception {
        when(workflowDefinitionService.findByIdAndTenant(any(), any()))
                .thenThrow(new IllegalArgumentException("Workflow not found"));

        mockMvc.perform(get("/api/v1/workflows/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.type").value("https://conductor.io/errors/not-found"))
                .andExpect(jsonPath("$.detail").value("Workflow not found"));
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void invalidState_returns422WithProblemDetail() throws Exception {
        when(workflowDefinitionService.findByIdAndTenant(any(), any()))
                .thenThrow(new IllegalStateException("Workflow is in an invalid state"));

        mockMvc.perform(get("/api/v1/workflows/" + UUID.randomUUID())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.type").value("https://conductor.io/errors/invalid-state"));
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void missingRequiredField_returns400WithValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/workflows")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://conductor.io/errors/validation"));
    }
}
