package com.tuition.backend.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AssignmentDTO {
    private Long id;
    private String title;
    private String description;
    private String fileUrl;
    private LocalDate dueDate;
    private String grade;
    private String subject;
    private String createdByName;
    private Long createdById;
    private LocalDateTime createdAt;

    // Constructors
    public AssignmentDTO() {}

    public AssignmentDTO(Long id, String title, String description, String fileUrl, LocalDate dueDate, String grade, String subject, String createdByName, Long createdById, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.fileUrl = fileUrl;
        this.dueDate = dueDate;
        this.grade = grade;
        this.subject = subject;
        this.createdByName = createdByName;
        this.createdById = createdById;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getCreatedByName() { return createdByName; }
    public void setCreatedByName(String createdByName) { this.createdByName = createdByName; }

    public Long getCreatedById() { return createdById; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
