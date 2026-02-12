package com.tuition.backend.dto;

import java.time.LocalDateTime;

public class QuestionDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long teacherId;
    private String teacherName;
    private String questionText;
    private String questionFileUrl;
    private String answerText;
    private String answerFileUrl;
    private String status;
    private LocalDateTime askedAt;
    private LocalDateTime answeredAt;

    public QuestionDTO() {}

    public QuestionDTO(Long id, Long studentId, String studentName, Long teacherId, String teacherName, 
                       String questionText, String questionFileUrl, String answerText, String answerFileUrl, 
                       String status, LocalDateTime askedAt, LocalDateTime answeredAt) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.teacherId = teacherId;
        this.teacherName = teacherName;
        this.questionText = questionText;
        this.questionFileUrl = questionFileUrl;
        this.answerText = answerText;
        this.answerFileUrl = answerFileUrl;
        this.status = status;
        this.askedAt = askedAt;
        this.answeredAt = answeredAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }

    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getQuestionFileUrl() { return questionFileUrl; }
    public void setQuestionFileUrl(String questionFileUrl) { this.questionFileUrl = questionFileUrl; }

    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }

    public String getAnswerFileUrl() { return answerFileUrl; }
    public void setAnswerFileUrl(String answerFileUrl) { this.answerFileUrl = answerFileUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getAskedAt() { return askedAt; }
    public void setAskedAt(LocalDateTime askedAt) { this.askedAt = askedAt; }

    public LocalDateTime getAnsweredAt() { return answeredAt; }
    public void setAnsweredAt(LocalDateTime answeredAt) { this.answeredAt = answeredAt; }
}
