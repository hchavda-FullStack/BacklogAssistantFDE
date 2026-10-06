package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import com.fde.backlogassistant.exception.DocumentTooLargeException;
import com.fde.backlogassistant.exception.PdfProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.io.InputStream;
import static org.junit.jupiter.api.Assertions.*;

class PdfExtractionServiceTest {
    private final AppProperties props = new AppProperties();
    private final PdfExtractionService service = new PdfExtractionService(props);

    @Test void shouldExtractTextFromPdf() throws Exception {
        try (InputStream in = getClass().getClassLoader().getResourceAsStream("meeting-notes.pdf")) {
            assertNotNull(in);
            String text = service.extractText(new MockMultipartFile("file", "meeting-notes.pdf", "application/pdf", in));
            assertTrue(text.toLowerCase().contains("password"));
        }
    }
    @Test void shouldRejectEmptyPdf() {
        assertThrows(PdfProcessingException.class, () -> service.extractText(new MockMultipartFile("file", "empty.pdf", "application/pdf", new byte[0])));
    }
    @Test void shouldRejectNonPdf() {
        assertThrows(PdfProcessingException.class, () -> service.extractText(new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes())));
    }
    @Test void shouldRejectOversizedUploadBeforeParsing() {
        props.getPdf().setMaxFileSizeBytes(3);
        assertThrows(DocumentTooLargeException.class, () -> service.extractText(new MockMultipartFile("file", "big.pdf", "application/pdf", new byte[4])));
    }
}
