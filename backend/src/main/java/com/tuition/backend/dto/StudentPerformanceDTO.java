package com.tuition.backend.dto;

import java.util.List;

public class StudentPerformanceDTO {
    private Long id;
    private String studentName;
    private String grade;
    private List<SubjectPerformanceDTO> subjectPerformances;

    public StudentPerformanceDTO() {}

    public StudentPerformanceDTO(Long id, String studentName, String grade, List<SubjectPerformanceDTO> subjectPerformances) {
        this.id = id;
        this.studentName = studentName;
        this.grade = grade;
        this.subjectPerformances = subjectPerformances;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public List<SubjectPerformanceDTO> getSubjectPerformances() { return subjectPerformances; }
    public void setSubjectPerformances(List<SubjectPerformanceDTO> subjectPerformances) { this.subjectPerformances = subjectPerformances; }
}
