package com.fde.backlogassistant.model;

public class DuplicateAnalysis {

    private DuplicateRisk duplicateRisk;
    private String relatedBacklogId;
    private String explanation;

    public DuplicateAnalysis() {
    }

    public DuplicateRisk getDuplicateRisk() {
        return duplicateRisk;
    }

    public void setDuplicateRisk(DuplicateRisk duplicateRisk) {
        this.duplicateRisk = duplicateRisk;
    }

    public String getRelatedBacklogId() {
        return relatedBacklogId;
    }

    public void setRelatedBacklogId(String relatedBacklogId) {
        this.relatedBacklogId = relatedBacklogId;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}