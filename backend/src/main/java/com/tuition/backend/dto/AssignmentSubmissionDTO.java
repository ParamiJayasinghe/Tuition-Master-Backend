package com.tuition.backend.dto;

import java.time.LocalDateTime;

public class AssignmentSubmissionDTO {
    private Long id;
    private Long assignmentId;
    private String assignmentTitle;
    private Long studentId;
    private String studentName;
    private String answerText;
    private String fileUrl;
    private LocalDateTime submittedAt;
    private Boolean isLate;
    private Integer marks;
    private Boolean isMarked;

    // Constructors
    public AssignmentSubmissionDTO() {}

    public AssignmentSubmissionDTO(Long id, Long assignmentId, String assignmentTitle, Long studentId, String studentName, String answerText, String fileUrl, LocalDateTime submittedAt, Boolean isLate, Integer marks, Boolean isMarked) {
        this.id = id;
        this.assignmentId = assignmentId;
        this.assignmentTitle = assignmentTitle;
        this.studentId = studentId;
        this.studentName = studentName;
        this.answerText = answerText;
        this.fileUrl = fileUrl;
        this.submittedAt = submittedAt;
        this.isLate = isLate;
        this.marks = marks;
        this.isMarked = isMarked;
    }

    // Getters and Setters

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getAssignmentId() { return assignmentId; }
    public void setAssignmentId(Long assignmentId) { this.assignmentId = assignmentId; }

    public String getAssignmentTitle() { return assignmentTitle; }
    public void setAssignmentTitle(String assignmentTitle) { this.assignmentTitle = assignmentTitle; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public Boolean getIsLate() { return isLate; }
    public void setIsLate(Boolean isLate) { this.isLate = isLate; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public Boolean getIsMarked() { return isMarked; }
    public void setIsMarked(Boolean isMarked) { this.isMarked = isMarked; }
}
