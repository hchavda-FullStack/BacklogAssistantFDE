package com.fde.backlogassistant.service;

import com.fde.backlogassistant.exception.AiServiceException;
import com.fde.backlogassistant.model.BacklogRequest;
import com.fde.backlogassistant.model.BacklogResponse;
import com.fde.backlogassistant.model.GeneratedUserStory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BacklogAssistantService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BacklogAssistantService.class);
    private final PromptBuilderService promptBuilderService;
    private final AIClientService aiClientService;
    private final TextChunkingService textChunkingService;

    public BacklogAssistantService(PromptBuilderService promptBuilderService, AIClientService aiClientService,
                                   TextChunkingService textChunkingService) {
        this.promptBuilderService = promptBuilderService;
        this.aiClientService = aiClientService;
        this.textChunkingService = textChunkingService;
    }

    public BacklogResponse generateBacklog(BacklogRequest request) {
        return generateSingle(request);
    }

    public BacklogResponse generateBacklogFromDocument(String requirementText) {
        List<String> chunks = textChunkingService.chunk(requirementText);
        if (chunks.isEmpty()) throw new IllegalArgumentException("Extracted requirement text cannot be empty.");
        if (chunks.size() == 1) return generateSingle(new BacklogRequest(chunks.get(0), null));

        LOGGER.info("Large document split into {} chunks for bounded AI processing.", chunks.size());
        List<BacklogResponse> responses = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            LOGGER.info("Processing document chunk {}/{}.", i + 1, chunks.size());
            responses.add(generateSingle(new BacklogRequest(chunks.get(i), null)));
        }
        return consolidate(responses);
    }

    private BacklogResponse generateSingle(BacklogRequest request) {
        int count = request.getExistingBacklog() == null ? 0 : request.getExistingBacklog().size();
        LOGGER.info("Starting backlog generation. Existing backlog count: {}", count);
        BacklogResponse response = aiClientService.generateBacklog(
                promptBuilderService.buildSystemPrompt(), promptBuilderService.buildUserPrompt(request));
        validateAiResponse(response);
        LOGGER.info("Backlog generation completed. Generated story count: {}", response.getUserStories().size());
        return response;
    }

    private BacklogResponse consolidate(List<BacklogResponse> responses) {
        BacklogResponse consolidated = new BacklogResponse();
        consolidated.setSummary(responses.stream().map(BacklogResponse::getSummary).filter(s -> s != null && !s.isBlank())
                .distinct().reduce((a, b) -> a + " " + b).orElse("Backlog generated from document chunks."));
        Map<String, GeneratedUserStory> unique = new LinkedHashMap<>();
        for (BacklogResponse response : responses) {
            for (GeneratedUserStory story : response.getUserStories()) {
                String key = story.getTitle() == null ? String.valueOf(unique.size()) : story.getTitle().trim().toLowerCase();
                unique.putIfAbsent(key, story);
            }
        }
        consolidated.setUserStories(new ArrayList<>(unique.values()));
        validateAiResponse(consolidated);
        return consolidated;
    }

    private void validateAiResponse(BacklogResponse response) {
        if (response == null) throw new AiServiceException("The AI service returned an empty response.");
        if (response.getSummary() == null || response.getSummary().isBlank()) throw new AiServiceException("The AI response does not contain a summary.");
        if (response.getUserStories() == null || response.getUserStories().isEmpty()) throw new AiServiceException("The AI response does not contain any user stories.");
        for (int i = 0; i < response.getUserStories().size(); i++) validateUserStory(response.getUserStories().get(i), i);
    }

    private void validateUserStory(GeneratedUserStory story, int index) {
        int n = index + 1;
        if (story == null) throw new AiServiceException("Generated user story " + n + " is empty.");
        if (story.getTitle() == null || story.getTitle().isBlank()) throw new AiServiceException("Generated user story " + n + " does not contain a title.");
        if (story.getDescription() == null || story.getDescription().isBlank()) throw new AiServiceException("Generated user story " + n + " does not contain a description.");
        if (story.getAcceptanceCriteria() == null || story.getAcceptanceCriteria().isEmpty()) throw new AiServiceException("Generated user story " + n + " does not contain acceptance criteria.");
        if (story.getPriority() == null) throw new AiServiceException("Generated user story " + n + " does not contain a priority.");
        if (story.getCategory() == null) throw new AiServiceException("Generated user story " + n + " does not contain a category.");
    }
}
