package com.tuition.backend.dto;

import java.time.LocalDateTime;

/**
 * DTO for creating/updating Teacher.
 * For creation, provide userId (id of an existing User) and teacher-specific fields.
 */
public class TeacherDto {

    private Long userId;           // id of existing user to attach
    private String fullName;
    private String contactNumber;
    private String nicNumber;
    private String teacherId;     // unique teacher identifier (e.g., T001)
    private String subjects;      // comma-separated or json string for now
    private String gender;        // Male, Female, Other

    // Getters and setters

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }

    public String getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(String teacherId) {
        this.teacherId = teacherId;
    }

    public String getSubjects() {
        return subjects;
    }

    public void setSubjects(String subjects) {
        this.subjects = subjects;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
