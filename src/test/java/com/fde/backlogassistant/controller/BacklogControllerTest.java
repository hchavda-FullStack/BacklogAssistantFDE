package com.fde.backlogassistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fde.backlogassistant.exception.RateLimitExceededException;
import com.fde.backlogassistant.model.BacklogResponse;
import com.fde.backlogassistant.service.BacklogAssistantService;
import com.fde.backlogassistant.service.PdfExtractionService;
import com.fde.backlogassistant.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BacklogController.class)
class BacklogControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean BacklogAssistantService backlogAssistantService;
    @MockitoBean PdfExtractionService pdfExtractionService;
    @MockitoBean RateLimitService rateLimitService;

    @ParameterizedTest
    @ValueSource(strings = {"/api/does-not-exist", "/api/backlog/does-not-exist"})
    void shouldReturn404ForUnknownEndpoint(String path) throws Exception {
        mockMvc.perform(get(path))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("The requested resource was not found."))
                .andExpect(jsonPath("$.timestamp").isNotEmpty())
                .andExpect(jsonPath("$.validationErrors").value(nullValue()));
        verifyNoInteractions(backlogAssistantService, pdfExtractionService, rateLimitService);
    }

    @Test void shouldPreserve500ForUnexpectedException() throws Exception {
        when(backlogAssistantService.generateBacklog(any()))
                .thenThrow(new RuntimeException("Sensitive internal details"));
        mockMvc.perform(post("/api/backlog/generate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"requirementText\":\"Password reset\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal server error"))
                .andExpect(jsonPath("$.message").value("An unexpected error occurred."));
    }

    @Test void shouldGenerateBacklog() throws Exception {
        BacklogResponse response = new BacklogResponse(); response.setSummary("Generated");
        when(backlogAssistantService.generateBacklog(any())).thenReturn(response);
        mockMvc.perform(post("/api/backlog/generate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"requirementText\":\"Password reset\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.summary").value("Generated"));
    }
    @Test void shouldReturn400ForEmptyRequirement() throws Exception {
        mockMvc.perform(post("/api/backlog/generate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"requirementText\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
    @Test void shouldReturn429WhenRateLimitExceeded() throws Exception {
        doThrow(new RateLimitExceededException("limit")).when(rateLimitService).checkAllowed(anyString());
        mockMvc.perform(post("/api/backlog/generate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"requirementText\":\"Password reset\"}"))
                .andExpect(status().isTooManyRequests()).andExpect(jsonPath("$.status").value(429));
    }
}
