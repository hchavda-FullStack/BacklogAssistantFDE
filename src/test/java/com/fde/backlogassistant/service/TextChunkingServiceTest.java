package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class TextChunkingServiceTest {
    @Test void shouldNotChunkSmallText() {
        AppProperties p = new AppProperties(); p.getPdf().setChunkSize(100);
        assertEquals(1, new TextChunkingService(p).chunk("small requirement").size());
    }
    @Test void shouldChunkLargeText() {
        AppProperties p = new AppProperties(); p.getPdf().setChunkSize(20); p.getPdf().setChunkOverlap(5);
        List<String> chunks = new TextChunkingService(p).chunk("First sentence. Second sentence. Third sentence. Fourth sentence.");
        assertTrue(chunks.size() > 1); assertTrue(chunks.stream().allMatch(c -> !c.isBlank()));
    }
}
