package com.tuition.backend.Service;

import com.tuition.backend.Entity.*;
import com.tuition.backend.Repository.*;
import com.tuition.backend.dto.AssignmentDTO;
import com.tuition.backend.dto.AssignmentSubmissionDTO;
import com.tuition.backend.config.AuditLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssignmentService {

    @Autowired
    private AssignmentRepository assignmentRepository;

    @Autowired
    private AssignmentSubmissionRepository submissionRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private com.tuition.backend.Repository.userRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Teacher getCurrentTeacher() {
        User user = getCurrentUser();
        return teacherRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found"));
    }

    private Student getCurrentStudent() {
        User user = getCurrentUser();
        return studentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Student profile not found"));
    }

    // --- Teacher Operations ---

    @Transactional
    @AuditLog(action = "CREATE_ASSIGNMENT", targetType = "Assignment")
    public AssignmentDTO createAssignment(AssignmentDTO dto) {
        Teacher teacher = getCurrentTeacher();

        Assignment assignment = new Assignment();
        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setFileUrl(dto.getFileUrl());
        assignment.setDueDate(dto.getDueDate());
        assignment.setGrade(dto.getGrade());
        assignment.setSubject(dto.getSubject()); // Single subject per assignment usually
        assignment.setCreatedBy(teacher);
        
        // Set isActive based on due date
        assignment.setIsActive(!dto.getDueDate().isBefore(java.time.LocalDate.now()));

        Assignment saved = assignmentRepository.save(assignment);
        return mapToDTO(saved);
    }

    @Transactional
    @AuditLog(action = "UPDATE_ASSIGNMENT", targetType = "Assignment")
    public AssignmentDTO updateAssignment(Long id, AssignmentDTO dto) {
        Teacher teacher = getCurrentTeacher();
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized: specific assignment not created by this teacher");
        }

        assignment.setTitle(dto.getTitle());
        assignment.setDescription(dto.getDescription());
        assignment.setFileUrl(dto.getFileUrl());
        assignment.setDueDate(dto.getDueDate());
        assignment.setGrade(dto.getGrade());
        assignment.setSubject(dto.getSubject());
        
        // Update isActive based on new due date
        assignment.setIsActive(!dto.getDueDate().isBefore(java.time.LocalDate.now()));

        Assignment saved = assignmentRepository.save(assignment);
        return mapToDTO(saved);
    }

    @Transactional
    @AuditLog(action = "DELETE_ASSIGNMENT", targetType = "Assignment")
    public void deleteAssignment(Long id) {
        Teacher teacher = getCurrentTeacher();
        Assignment assignment = assignmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        assignmentRepository.delete(assignment);
    }

    public List<AssignmentSubmissionDTO> getSubmissionsForAssignment(Long assignmentId) {
        Teacher teacher = getCurrentTeacher();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        if (!assignment.getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized");
        }

        List<AssignmentSubmission> submissions = submissionRepository.findByAssignment(assignment);
        return submissions.stream().map(this::mapToSubmissionDTO).collect(Collectors.toList());
    }

    // --- Student Operations ---

    public AssignmentSubmissionDTO submitAssignment(Long assignmentId, AssignmentSubmissionDTO dto) {
        Student student = getCurrentStudent();
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Assignment not found"));

        // Check if already submitted? Can update or create new? Let's assume update if exists
        Optional<AssignmentSubmission> existing = submissionRepository.findByAssignmentAndStudent(assignment, student);

        AssignmentSubmission submission = existing.orElse(new AssignmentSubmission());
        submission.setAssignment(assignment);
        submission.setStudent(student);
        submission.setAnswerText(dto.getAnswerText());
        submission.setFileUrl(dto.getFileUrl());

        AssignmentSubmission saved = submissionRepository.save(submission);
        return mapToSubmissionDTO(saved);
    }

    @Transactional
    @AuditLog(action = "MARK_SUBMISSION", targetType = "Submission")
    public AssignmentSubmissionDTO markSubmission(Long submissionId, Integer marks) {
        Teacher teacher = getCurrentTeacher();
        AssignmentSubmission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new RuntimeException("Submission not found"));

        if (!submission.getAssignment().getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized: submission for an assignment not created by this teacher");
        }

        submission.setMarks(marks);
        submission.setIsMarked(true);

        AssignmentSubmission saved = submissionRepository.save(submission);
        return mapToSubmissionDTO(saved);
    }

    // --- Shared / Context Aware ---

    public List<AssignmentDTO> getAssignments() {
        User user = getCurrentUser();
        if ("TEACHER".equals(user.getRole())) {
            Teacher teacher = teacherRepository.findByUser(user).orElseThrow();
            return assignmentRepository.findByCreatedBy(teacher).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        } else if ("STUDENT".equals(user.getRole())) {
            Student student = studentRepository.findByUser(user).orElseThrow();
            Teacher teacher = student.getCreatedBy(); 
            // Student only sees assignments from their registered teacher
            // Filter by grade and subjects
            
            List<Assignment> teacherAssignments = assignmentRepository.findByCreatedBy(teacher);
            
            String[] studentSubjects = student.getSubjects() != null ? student.getSubjects().split(",") : new String[0];
            List<String> subjectList = Arrays.stream(studentSubjects)
                                             .map(String::trim)
                                             .map(String::toLowerCase)
                                             .collect(Collectors.toList());

            return teacherAssignments.stream()
                    .filter(a -> a.getGrade().equalsIgnoreCase(student.getGrade()))
                    .filter(a -> subjectList.contains(a.getSubject().toLowerCase()))
                    .map(a -> {
                        var submission = submissionRepository.findByAssignmentAndStudent(a, student);
                        boolean submitted = submission.isPresent();
                        String submissionUrl = submitted ? submission.get().getFileUrl() : null;
                        Integer marks = submitted ? submission.get().getMarks() : null;
                        boolean marked = submitted && Boolean.TRUE.equals(submission.get().getIsMarked());
                        return mapToDTO(a, submitted, submissionUrl, marks, marked);
                    })
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
    
    public AssignmentDTO getAssignmentById(Long id) {
        // Simple get for now, could add permission checks
        Assignment assignment = assignmentRepository.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
        return mapToDTO(assignment);
    }


    // --- Mappers ---

    public void updateAssignmentsStatus() {
        java.time.LocalDate today = java.time.LocalDate.now();
        List<Assignment> expiredAssignments = assignmentRepository.findByDueDateBeforeAndIsActiveTrue(today);
        
        for (Assignment assignment : expiredAssignments) {
            assignment.setIsActive(false);
            assignmentRepository.save(assignment);
        }
    }

    // --- Mappers ---

    private AssignmentDTO mapToDTO(Assignment a) {
        return mapToDTO(a, false, null, null, false);
    }

    private AssignmentDTO mapToDTO(Assignment a, boolean isSubmitted, String submissionFileUrl, Integer marks, boolean isMarked) {
        // Calculate dynamic isActive status logic for the DTO
        boolean currentActiveStatus = !a.getDueDate().isBefore(java.time.LocalDate.now());
        
        return new AssignmentDTO(
                a.getId(),
                a.getTitle(),
                a.getDescription(),
                a.getFileUrl(),
                a.getDueDate(),
                a.getGrade(),
                a.getSubject(),
                a.getCreatedBy().getFullName(),
                a.getCreatedBy().getId(),
                a.getCreatedAt(),
                currentActiveStatus, // Return calculated status
                isSubmitted,
                submissionFileUrl,
                marks,
                isMarked
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
