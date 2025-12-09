package com.tuition.backend.Service;

import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.Repository.TeacherRepository;
import com.tuition.backend.Repository.userRepository;
import com.tuition.backend.dto.StudentDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private userRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    /**
     * Create student: Only TEACHER can create
     */
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public Student createStudent(StudentDto dto) {

        if (dto.getStudentId() == null || dto.getStudentId().trim().isEmpty()) {
            throw new RuntimeException("studentId is required");
        }
        if (studentRepository.existsByStudentId(dto.getStudentId())) {
            throw new RuntimeException("studentId already exists");
        }
        if (dto.getUserId() == null) {
            throw new RuntimeException("userId (existing User) is required to create student");
        }

        User linkedUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("Linked user not found with id: " + dto.getUserId()));

        linkedUser.setRole("STUDENT");
        linkedUser.setIsActive(true);
        userRepository.save(linkedUser);

        // Get current authenticated TEACHER
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Use currentUser directly as createdBy
        Student s = new Student();
        s.setUser(linkedUser);
        s.setFullName(dto.getFullName());
        s.setContactNumber(dto.getContactNumber());
        s.setStudentId(dto.getStudentId());
        s.setSubjects(dto.getSubjects());
        s.setGender(dto.getGender());
        s.setDateOfBirth(dto.getDateOfBirth());
        s.setAddress(dto.getAddress());
        s.setCreatedBy(currentUser);

        return studentRepository.save(s);
    }

    /**
     * Get all students: any authenticated user
     */
    @PreAuthorize("hasRole('TEACHER')")
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Get a student by ID: any authenticated user
     */
    @PreAuthorize("isAuthenticated()")
    public Student getStudentById(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    /**
     * Update student: Only TEACHER (creator) or ADMIN
     */
    @PreAuthorize("hasRole('TEACHER')")
    @Transactional
    public Student updateStudent(Long id, StudentDto dto) {

        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isCreatorTeacher = existing.getCreatedBy() != null &&
                existing.getCreatedBy().getId().equals(currentUser.getId());

        if (!isAdmin && !isCreatorTeacher) {
            throw new RuntimeException("Not authorized to update this student");
        }

        if (dto.getFullName() != null) existing.setFullName(dto.getFullName());
        if (dto.getContactNumber() != null) existing.setContactNumber(dto.getContactNumber());
        if (dto.getSubjects() != null) existing.setSubjects(dto.getSubjects());
        if (dto.getGender() != null) existing.setGender(dto.getGender());
        if (dto.getDateOfBirth() != null) existing.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getAddress() != null) existing.setAddress(dto.getAddress());

        if (dto.getStudentId() != null && !dto.getStudentId().equals(existing.getStudentId())) {
            if (studentRepository.existsByStudentId(dto.getStudentId())) {
                throw new RuntimeException("studentId already exists");
            }
            existing.setStudentId(dto.getStudentId());
        }

        if (dto.getUserId() != null && !isAdmin) {
            throw new RuntimeException("Only ADMIN can change the linked user for a student");
        }

        if (dto.getUserId() != null && isAdmin) {
            User newLinkedUser = userRepository.findById(dto.getUserId())
                    .orElseThrow(() ->
                            new RuntimeException("Linked user not found with id: " + dto.getUserId()));
            newLinkedUser.setRole("STUDENT");
            userRepository.save(newLinkedUser);
            existing.setUser(newLinkedUser);
        }

        return studentRepository.save(existing);
    }

    /**
     * Delete student: Only ADMIN
     */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteStudent(Long id) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Student not found with id: " + id));
        studentRepository.deleteById(id);
    }
}
