package com.fde.backlogassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fde.backlogassistant.model.BacklogRequest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PromptBuilderServiceTest {
    private final PromptBuilderService service = new PromptBuilderService(new ObjectMapper());
    @Test void shouldBuildUserPrompt() {
        BacklogRequest request = new BacklogRequest("Customer wants password reset functionality.", null);
        String prompt = service.buildUserPrompt(request);
        assertTrue(prompt.contains("password reset"));
    }
    @Test void shouldBuildSystemPrompt() { assertTrue(service.buildSystemPrompt().contains("Agile")); }
}
