package com.fde.backlogassistant.model;

import java.util.List;

public class GeneratedUserStory {

    private String title;
    private String description;
    private List<String> acceptanceCriteria;
    private Priority priority;
    private BacklogCategory category;
    private DuplicateAnalysis duplicateAnalysis;

    public GeneratedUserStory() {
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

    public List<String> getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public void setAcceptanceCriteria(
            List<String> acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public BacklogCategory getCategory() {
        return category;
    }

    public void setCategory(BacklogCategory category) {
        this.category = category;
    }

    public DuplicateAnalysis getDuplicateAnalysis() {
        return duplicateAnalysis;
    }

    public void setDuplicateAnalysis(
            DuplicateAnalysis duplicateAnalysis) {
        this.duplicateAnalysis = duplicateAnalysis;
    }
}