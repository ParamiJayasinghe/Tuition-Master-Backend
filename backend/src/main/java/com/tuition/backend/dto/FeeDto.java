package com.tuition.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FeeDto {
    private Long id;
    private Long studentId;
    private String studentName;
    private String subject; // Now directly from Fee
    private String grade;   // Snapshot of student grade
    private BigDecimal amount;
    private Integer month;
    private Integer year;
    private LocalDate dueDate;
    private String status;
    private LocalDate paidOn;
    private String paymentMethod;
    private String notes;

    public FeeDto() {
    }

    public FeeDto(Long id, Long studentId, String studentName, String subject, String grade, BigDecimal amount, Integer month, Integer year, LocalDate dueDate, String status, LocalDate paidOn, String paymentMethod, String notes) {
        this.id = id;
        this.studentId = studentId;
        this.studentName = studentName;
        this.subject = subject;
        this.grade = grade;
        this.amount = amount;
        this.month = month;
        this.year = year;
        this.dueDate = dueDate;
        this.status = status;
        this.paidOn = paidOn;
        this.paymentMethod = paymentMethod;
        this.notes = notes;
    }

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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getPaidOn() {
        return paidOn;
    }

    public void setPaidOn(LocalDate paidOn) {
        this.paidOn = paidOn;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
