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
    private Boolean isActive;
    private Boolean isSubmitted;
    private String submissionFileUrl;

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
        this.isActive = true; // Default or should be passed? Passed is better if mapping from entity
    }

    public AssignmentDTO(Long id, String title, String description, String fileUrl, LocalDate dueDate, String grade, String subject, String createdByName, Long createdById, LocalDateTime createdAt, Boolean isActive) {
        this.createdAt = createdAt;
        this.isActive = isActive;
        this.isSubmitted = false; // Default
    }

    public AssignmentDTO(Long id, String title, String description, String fileUrl, LocalDate dueDate, String grade, String subject, String createdByName, Long createdById, LocalDateTime createdAt, Boolean isActive, Boolean isSubmitted, String submissionFileUrl) {
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
        this.isActive = isActive;
        this.isSubmitted = isSubmitted;
        this.submissionFileUrl = submissionFileUrl;
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

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Boolean getIsSubmitted() { return isSubmitted; }
    public void setIsSubmitted(Boolean isSubmitted) { this.isSubmitted = isSubmitted; }

    public String getSubmissionFileUrl() { return submissionFileUrl; }
    public void setSubmissionFileUrl(String submissionFileUrl) { this.submissionFileUrl = submissionFileUrl; }
}
