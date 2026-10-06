package com.fde.backlogassistant.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public class BacklogRequest {

    @NotBlank(message = "Requirement text is required.")
    @Size(
            max = 20_000,
            message = "Requirement text cannot exceed 20,000 characters."
    )
    private String requirementText;

    @Valid
    private List<ExistingBacklogItem> existingBacklog;

    public BacklogRequest() {
    }

    public BacklogRequest(
            String requirementText,
            List<ExistingBacklogItem> existingBacklog) {
        this.requirementText = requirementText;
        this.existingBacklog = existingBacklog;
    }

    public String getRequirementText() {
        return requirementText;
    }

    public void setRequirementText(String requirementText) {
        this.requirementText = requirementText;
    }

    public List<ExistingBacklogItem> getExistingBacklog() {
        return existingBacklog;
    }

    public void setExistingBacklog(
            List<ExistingBacklogItem> existingBacklog) {
        this.existingBacklog = existingBacklog;
    }
}