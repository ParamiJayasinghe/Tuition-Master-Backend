package com.tuition.backend.Service;

import com.tuition.backend.Entity.AssignmentSubmission;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.AssignmentSubmissionRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.Repository.userRepository;
import com.tuition.backend.dto.AssignmentSubmissionDTO;
import com.tuition.backend.dto.StudentPerformanceDTO;
import com.tuition.backend.dto.SubjectPerformanceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PerformanceService {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    public StudentPerformanceDTO getStudentPerformance(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Student details not found for user: " + userId));

        List<AssignmentSubmission> submissions = submissionRepository.findByStudent(student);

        // Group submissions by assignment subject
        Map<String, List<AssignmentSubmission>> groupedBySubject = submissions.stream()
                .collect(Collectors.groupingBy(s -> s.getAssignment().getSubject()));

        List<SubjectPerformanceDTO> subjectPerformances = new ArrayList<>();

        groupedBySubject.forEach((subject, subList) -> {
            List<AssignmentSubmissionDTO> dtoList = subList.stream()
                    .map(this::mapToSubmissionDTO)
                    .collect(Collectors.toList());

            // Calculate average marks for marked submissions
            double average = subList.stream()
                    .filter(s -> Boolean.TRUE.equals(s.getIsMarked()) && s.getMarks() != null)
                    .mapToInt(AssignmentSubmission::getMarks)
                    .average()
                    .orElse(0.0);

            subjectPerformances.add(new SubjectPerformanceDTO(subject, average, dtoList));
        });

        // Add subjects the student is enrolled in but has no submissions for?
        // Let's stick to subjects they have at least one submission for for now as per requirement 
        // "If one student has enrolled to few subjects their performance for all subjects should be shown"
        // If they have 0 submissions for a subject, it might be worth showing it with 0 avg.
        String studentSubjects = student.getSubjects();
        if (studentSubjects != null && !studentSubjects.isEmpty()) {
            for (String sub : studentSubjects.split(",")) {
                String trimmedSub = sub.trim();
                boolean exists = subjectPerformances.stream()
                        .anyMatch(p -> p.getSubjectName().equalsIgnoreCase(trimmedSub));
                if (!exists && !trimmedSub.isEmpty()) {
                    subjectPerformances.add(new SubjectPerformanceDTO(trimmedSub, 0.0, new ArrayList<>()));
                }
            }
        }

        return new StudentPerformanceDTO(
                student.getId(),
                student.getFullName(),
                student.getGrade(),
                subjectPerformances
        );
    }

    private AssignmentSubmissionDTO mapToSubmissionDTO(AssignmentSubmission s) {
        boolean isLate = s.getSubmittedAt().toLocalDate().isAfter(s.getAssignment().getDueDate());
        return new AssignmentSubmissionDTO(
                s.getId(),
                s.getAssignment().getId(),
                s.getAssignment().getTitle(),
                s.getStudent().getId(),
                s.getStudent().getFullName(),
                s.getAnswerText(),
                s.getFileUrl(),
                s.getSubmittedAt(),
                isLate,
                s.getMarks(),
                s.getIsMarked()
        );
    }
}
