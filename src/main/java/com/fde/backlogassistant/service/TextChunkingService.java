package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TextChunkingService {
    private final AppProperties.Pdf properties;
    public TextChunkingService(AppProperties appProperties) { this.properties = appProperties.getPdf(); }

    public List<String> chunk(String text) {
        if (text == null || text.isBlank()) return List.of();
        int chunkSize = properties.getChunkSize();
        int overlap = Math.min(properties.getChunkOverlap(), Math.max(0, chunkSize - 1));
        if (text.length() <= chunkSize) return List.of(text);

        List<String> chunks = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int hardEnd = Math.min(start + chunkSize, text.length());
            int end = hardEnd;
            if (hardEnd < text.length()) {
                int sentence = Math.max(text.lastIndexOf('.', hardEnd), text.lastIndexOf('\n', hardEnd));
                if (sentence > start + chunkSize / 2) end = sentence + 1;
            }
            chunks.add(text.substring(start, end).trim());
            if (end >= text.length()) break;
            start = Math.max(start + 1, end - overlap);
        }
        return chunks;
    }
}
