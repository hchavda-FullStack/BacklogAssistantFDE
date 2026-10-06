package com.fde.backlogassistant.service;

import com.fde.backlogassistant.config.AppProperties;
import com.fde.backlogassistant.exception.DocumentTooLargeException;
import com.fde.backlogassistant.exception.PdfProcessingException;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfExtractionService {
    private final AppProperties.Pdf properties;
    public PdfExtractionService(AppProperties appProperties) { this.properties = appProperties.getPdf(); }

    public String extractText(MultipartFile file) {
        validatePdf(file);
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            if (document.getNumberOfPages() > properties.getMaxPages()) {
                throw new DocumentTooLargeException("PDF exceeds the maximum allowed page count of " + properties.getMaxPages() + ".");
            }
            String text = new PDFTextStripper().getText(document);
            if (text == null || text.isBlank()) {
                throw new PdfProcessingException("PDF does not contain extractable text. Scanned/image-only PDFs require OCR, which is not supported.");
            }
            if (text.length() > properties.getMaxExtractedCharacters()) {
                throw new DocumentTooLargeException("Extracted PDF text exceeds the maximum allowed size of " + properties.getMaxExtractedCharacters() + " characters.");
            }
            return text;
        } catch (DocumentTooLargeException | PdfProcessingException e) {
            throw e;
        } catch (IOException e) {
            throw new PdfProcessingException("Unable to read PDF document.", e);
        }
    }

    private void validatePdf(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new PdfProcessingException("PDF file cannot be empty.");
        if (file.getSize() > properties.getMaxFileSizeBytes()) {
            throw new DocumentTooLargeException("PDF exceeds the maximum upload size of " + properties.getMaxFileSizeBytes() + " bytes.");
        }
        if (!"application/pdf".equalsIgnoreCase(file.getContentType())) {
            throw new PdfProcessingException("Only PDF files are supported.");
        }
    }
}
