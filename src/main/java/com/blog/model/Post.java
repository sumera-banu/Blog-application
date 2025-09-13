package com.blog.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "posts")
public class Post extends AbstractEntity {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Content is required")
    @Lob
    @Column(nullable = false, columnDefinition = "LONGTEXT")
    private String content;
    
    @Column(columnDefinition = "TEXT")
    private String summary;
    
    @Column(length = 500)
    private String tags;
    
    @Column(name = "is_published")
    private Boolean isPublished = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;
    
    // Constructors
    public Post() {}
    
    public Post(String title, String content, User author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }
    
    // Business methods
    public String getShortContent(int length) {
        if (content == null) return "";
        return content.length() > length ? 
               content.substring(0, length) + "..." : content;
    }
    
    public String[] getTagArray() {
        if (tags == null || tags.trim().isEmpty()) {
            return new String[0];
        }
        return tags.split(",");
    }
    
    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    
    public Boolean getIsPublished() { return isPublished; }
    public void setIsPublished(Boolean isPublished) { this.isPublished = isPublished; }
    
    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }
}
