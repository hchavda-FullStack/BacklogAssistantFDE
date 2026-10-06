package com.fde.backlogassistant.controller;

import com.fde.backlogassistant.model.BacklogRequest;
import com.fde.backlogassistant.model.BacklogResponse;
import com.fde.backlogassistant.service.BacklogAssistantService;
import com.fde.backlogassistant.service.PdfExtractionService;
import com.fde.backlogassistant.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/backlog")
public class BacklogController {
    private final BacklogAssistantService backlogAssistantService;
    private final PdfExtractionService pdfExtractionService;
    private final RateLimitService rateLimitService;

    public BacklogController(BacklogAssistantService backlogAssistantService, PdfExtractionService pdfExtractionService,
                             RateLimitService rateLimitService) {
        this.backlogAssistantService = backlogAssistantService;
        this.pdfExtractionService = pdfExtractionService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/generate")
    public ResponseEntity<BacklogResponse> generateBacklog(@Valid @RequestBody BacklogRequest request,
                                                            HttpServletRequest httpRequest) {
        rateLimitService.checkAllowed(httpRequest.getRemoteAddr());
        return ResponseEntity.ok(backlogAssistantService.generateBacklog(request));
    }

    @PostMapping(value = "/generate/pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BacklogResponse> generateFromPdf(@RequestPart("file") MultipartFile file,
                                                            HttpServletRequest httpRequest) {
        rateLimitService.checkAllowed(httpRequest.getRemoteAddr());
        String requirementText = pdfExtractionService.extractText(file);
        return ResponseEntity.ok(backlogAssistantService.generateBacklogFromDocument(requirementText));
    }
}
