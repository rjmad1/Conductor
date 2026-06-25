package com.conductor.identity.api;

import com.conductor.identity.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({IdentityController.class, IdentityExceptionHandler.class})
class IdentityExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private UserService userService;
    @MockBean private org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void invalidEmail_returns400WithValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/identity/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"not-an-email\",\"password\":\"pass\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://conductor.io/errors/validation"))
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void emptyBody_returns400WithValidationErrors() throws Exception {
        mockMvc.perform(post("/api/v1/identity/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.type").value("https://conductor.io/errors/validation"));
    }

    @Test
    @WithMockUser(roles = "TENANT_ADMIN")
    void serviceThrowsIllegalState_returns409WithProblemDetail() throws Exception {
        when(userService.createUser(any(), any()))
                .thenThrow(new IllegalStateException("User already exists"));

        mockMvc.perform(post("/api/v1/identity/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"password\":\"secret\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.type").value("https://conductor.io/errors/identity-conflict"));
    }
}
