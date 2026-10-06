package com.fde.backlogassistant.model;

import java.util.List;

public class BacklogResponse {

    private String summary;
    private List<GeneratedUserStory> userStories;

    public BacklogResponse() {
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<GeneratedUserStory> getUserStories() {
        return userStories;
    }

    public void setUserStories(
            List<GeneratedUserStory> userStories) {
        this.userStories = userStories;
    }
}