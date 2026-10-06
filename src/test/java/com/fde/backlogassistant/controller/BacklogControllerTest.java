package com.fde.backlogassistant.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fde.backlogassistant.exception.RateLimitExceededException;
import com.fde.backlogassistant.model.BacklogResponse;
import com.fde.backlogassistant.service.BacklogAssistantService;
import com.fde.backlogassistant.service.PdfExtractionService;
import com.fde.backlogassistant.service.RateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BacklogController.class)
class BacklogControllerTest {
    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockitoBean BacklogAssistantService backlogAssistantService;
    @MockitoBean PdfExtractionService pdfExtractionService;
    @MockitoBean RateLimitService rateLimitService;

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
