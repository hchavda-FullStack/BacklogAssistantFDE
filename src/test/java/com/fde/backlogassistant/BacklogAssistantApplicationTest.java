package com.fde.backlogassistant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
class BacklogAssistantApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context and dependency injection start successfully.
    }
}
