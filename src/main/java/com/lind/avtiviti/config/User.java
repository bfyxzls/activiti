package com.lind.avtiviti.config;

public class User {
    private String id;

    private String summary;

    private String content;

    private String title;

    public User(String id, String summary, String content, String title) {
        this.id = id;
        this.summary = summary;
        this.content = content;
        this.title = title;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public String getSummary() {
        return summary;
    }

    public String getContent() {
        return content;
    }

    public String getTitle() {
        return title;
    }
}
