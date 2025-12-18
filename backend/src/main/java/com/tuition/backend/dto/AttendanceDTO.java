package com.tuition.backend.dto;

import java.time.LocalDate;

public class AttendanceDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private String contactNumber;
    private LocalDate date;
    private String status;
    private String subject;
    private String grade;
    private String markedAbsenceReason;
    private Long markedById;

    // Default Constructor
    public AttendanceDTO() {
    }

    // Constructor for fetching students who might not have attendance yet
    public AttendanceDTO(Long studentId, String studentName, String contactNumber, String grade, String subject) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.contactNumber = contactNumber;
        this.grade = grade;
        this.subject = subject;
        this.status = "NONE"; // Default status
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getMarkedAbsenceReason() {
        return markedAbsenceReason;
    }

    public void setMarkedAbsenceReason(String markedAbsenceReason) {
        this.markedAbsenceReason = markedAbsenceReason;
    }

    public Long getMarkedById() {
        return markedById;
    }

    public void setMarkedById(Long markedById) {
        this.markedById = markedById;
    }
}
