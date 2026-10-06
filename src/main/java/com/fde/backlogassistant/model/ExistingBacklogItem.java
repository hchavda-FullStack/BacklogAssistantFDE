package com.fde.backlogassistant.model;

import jakarta.validation.constraints.NotBlank;

public class ExistingBacklogItem {

    @NotBlank(message = "Existing backlog item ID is required.")
    private String id;

    @NotBlank(message = "Existing backlog item title is required.")
    private String title;

    private String description;
    private String status;

    public ExistingBacklogItem() {
    }

    public ExistingBacklogItem(
            String id,
            String title,
            String description,
            String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}