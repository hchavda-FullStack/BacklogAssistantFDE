package com.fde.backlogassistant.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fde.backlogassistant.config.AppProperties;
import com.fde.backlogassistant.exception.AiServiceException;
import com.fde.backlogassistant.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class BacklogAssistantServiceTest {

    @Test
    void shouldRejectMalformedAiResponse() {
        AIClientService ai = mock(AIClientService.class);
        when(ai.generateBacklog(anyString(), anyString())).thenReturn(new BacklogResponse());
        BacklogAssistantService service = service(ai, 100);
        assertThrows(AiServiceException.class,
                () -> service.generateBacklog(new BacklogRequest("Password reset", null)));
    }

    @Test
    void shouldProcessAndConsolidateLargeDocumentChunks() {
        AIClientService ai = mock(AIClientService.class);
        when(ai.generateBacklog(anyString(), anyString()))
                .thenReturn(validResponse("Story A"), validResponse("Story B"), validResponse("Story A"));
        BacklogAssistantService service = service(ai, 25);
        BacklogResponse result = service.generateBacklogFromDocument(
                "First requirement sentence. Second requirement sentence. Third requirement sentence. Fourth requirement sentence.");
        assertTrue(result.getUserStories().size() >= 2);
        verify(ai, atLeast(2)).generateBacklog(anyString(), anyString());
    }

    private BacklogAssistantService service(AIClientService ai, int chunkSize) {
        AppProperties p = new AppProperties();
        p.getPdf().setChunkSize(chunkSize); p.getPdf().setChunkOverlap(5);
        return new BacklogAssistantService(new PromptBuilderService(new ObjectMapper()), ai, new TextChunkingService(p));
    }

    private BacklogResponse validResponse(String title) {
        GeneratedUserStory story = new GeneratedUserStory();
        story.setTitle(title); story.setDescription("As a user, I want a capability, so that I get value.");
        story.setAcceptanceCriteria(List.of("Given X, When Y, Then Z"));
        story.setPriority(Priority.MEDIUM); story.setCategory(BacklogCategory.FEATURE);
        BacklogResponse response = new BacklogResponse(); response.setSummary("Summary"); response.setUserStories(List.of(story));
        return response;
    }
}
