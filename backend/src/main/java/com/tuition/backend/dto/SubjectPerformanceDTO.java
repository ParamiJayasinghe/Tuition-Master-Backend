package com.tuition.backend.dto;

import java.util.List;

public class SubjectPerformanceDTO {
    private String subjectName;
    private Double averageMarks;
    private List<AssignmentSubmissionDTO> submissions;

    public SubjectPerformanceDTO() {}

    public SubjectPerformanceDTO(String subjectName, Double averageMarks, List<AssignmentSubmissionDTO> submissions) {
        this.subjectName = subjectName;
        this.averageMarks = averageMarks;
        this.submissions = submissions;
    }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public Double getAverageMarks() { return averageMarks; }
    public void setAverageMarks(Double averageMarks) { this.averageMarks = averageMarks; }

    public List<AssignmentSubmissionDTO> getSubmissions() { return submissions; }
    public void setSubmissions(List<AssignmentSubmissionDTO> submissions) { this.submissions = submissions; }
}
