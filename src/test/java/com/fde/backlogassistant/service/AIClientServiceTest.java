package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import com.fde.backlogassistant.exception.AiServiceException;
import com.fde.backlogassistant.model.BacklogResponse;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AIClientServiceTest {
    @Test void shouldRetryTransientFailureAndSucceed() {
        AppProperties p = props(); AtomicInteger calls = new AtomicInteger(); BacklogResponse expected = new BacklogResponse();
        AIClientService service = new AIClientService(mock(ChatClient.class), p, ms -> {}) {
            @Override protected BacklogResponse invokeModel(String s, String u) {
                if (calls.incrementAndGet() < 3) throw new RuntimeException("503 temporarily unavailable");
                return expected;
            }
        };
        assertSame(expected, service.generateBacklog("s", "u")); assertEquals(3, calls.get());
    }
    @Test void shouldStopAfterRetryExhaustion() {
        AppProperties p = props(); AtomicInteger calls = new AtomicInteger();
        AIClientService service = new AIClientService(mock(ChatClient.class), p, ms -> {}) {
            @Override protected BacklogResponse invokeModel(String s, String u) { calls.incrementAndGet(); throw new RuntimeException("429 rate limit"); }
        };
        assertThrows(AiServiceException.class, () -> service.generateBacklog("s", "u")); assertEquals(3, calls.get());
    }
    @Test void shouldNotRetryPermanentFailure() {
        AppProperties p = props(); AtomicInteger calls = new AtomicInteger();
        AIClientService service = new AIClientService(mock(ChatClient.class), p, ms -> {}) {
            @Override protected BacklogResponse invokeModel(String s, String u) { calls.incrementAndGet(); throw new IllegalArgumentException("bad request"); }

            @Override
            boolean isTransient(Throwable throwable) {
                return super.isTransient(throwable);
            }
        };
        assertThrows(AiServiceException.class, () -> service.generateBacklog("s", "u")); assertEquals(1, calls.get());
    }
    private AppProperties props() { AppProperties p = new AppProperties(); p.getAi().getRetry().setMaxAttempts(3); p.getAi().getRetry().setInitialDelayMs(1); return p; }
}
