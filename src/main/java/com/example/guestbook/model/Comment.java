package com.example.guestbook.model;

import java.time.OffsetDateTime;

public class Comment {
    private long id;
    private String author;
    private String text;
    private OffsetDateTime createdAt;

    public Comment() {}

    public Comment(long id, String author, String text, OffsetDateTime createdAt) {
        this.id = id;
        this.author = author;
        this.text = text;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public String getAuthor() { return author; }
    public String getText() { return text; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    public void setId(long id) { this.id = id; }
    public void setAuthor(String author) { this.author = author; }
    public void setText(String text) { this.text = text; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
