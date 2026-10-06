package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import com.fde.backlogassistant.exception.AiServiceException;
import com.fde.backlogassistant.model.BacklogResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ai.retry.NonTransientAiException;
import org.springframework.ai.retry.TransientAiException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Service
public class AIClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AIClientService.class);
    private final ChatClient chatClient;
    private final AppProperties.Retry retry;
    private final Sleeper sleeper;

    @Autowired
    public AIClientService(ChatClient chatClient, AppProperties appProperties) {
        this(chatClient, appProperties, Thread::sleep);
    }

    AIClientService(ChatClient chatClient, AppProperties appProperties, Sleeper sleeper) {
        this.chatClient = chatClient;
        this.retry = appProperties.getAi().getRetry();
        this.sleeper = sleeper;
    }

    public BacklogResponse generateBacklog(String systemPrompt, String userPrompt) {
        long delay = retry.getInitialDelayMs();
        Exception lastFailure = null;
        for (int attempt = 1; attempt <= retry.getMaxAttempts(); attempt++) {
            try {
                LOGGER.info("Sending backlog-generation request to AI model. Attempt {}/{}.", attempt, retry.getMaxAttempts());
                BacklogResponse response = invokeModel(systemPrompt, userPrompt);
                LOGGER.info("AI model returned a structured response.");
                return response;
            } catch (Exception exception) {
                lastFailure = exception;
                boolean retryable = isTransient(exception);
                if (!retryable || attempt == retry.getMaxAttempts()) {
                    LOGGER.error(
                            "AI model request failed. Retryable: {}, attempts: {}, exception: {}, message: {}",
                            retryable,
                            attempt,
                            exception.getClass().getSimpleName(),
                            exception.getMessage()
                    );
                    throw new AiServiceException("Unable to generate backlog items using the AI service.", exception);
                }
                LOGGER.warn("Transient AI failure on attempt {}. Retrying after {} ms.", attempt, delay);
                try {
                    sleeper.sleep(delay);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new AiServiceException("AI retry was interrupted.", interrupted);
                }
                delay = Math.max(delay + 1, Math.round(delay * retry.getMultiplier()));
            }
        }
        throw new AiServiceException("Unable to generate backlog items using the AI service.", lastFailure);
    }

    protected BacklogResponse invokeModel(String systemPrompt, String userPrompt) {
        return chatClient.prompt().system(systemPrompt).user(userPrompt).call().entity(BacklogResponse.class);
    }

    boolean isTransient(Throwable throwable) {
        Throwable current = throwable;

        while (current != null) {

            // Permanent AI errors: do not retry
            if (current instanceof NonTransientAiException) {
                return false;
            }

            // Spring AI classified it as retryable
            if (current instanceof TransientAiException) {
                return true;
            }

            // Temporary network failures
            if (current instanceof SocketTimeoutException ||
                    current instanceof ConnectException) {
                return true;
            }

            String message = current.getMessage() == null
                    ? ""
                    : current.getMessage().toLowerCase();

            // Permanent failures
            if (message.contains("401") ||
                    message.contains("403") ||
                    message.contains("invalid_api_key") ||
                    message.contains("incorrect api key") ||
                    message.contains("model_not_found")) {
                return false;
            }

            // Retryable failures
            if (message.contains("429") ||
                    message.contains("rate limit") ||
                    message.contains("too many requests") ||
                    message.contains("502") ||
                    message.contains("503") ||
                    message.contains("504") ||
                    message.contains("timeout") ||
                    message.contains("temporarily unavailable")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
