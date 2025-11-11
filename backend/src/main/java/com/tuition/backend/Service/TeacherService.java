package com.tuition.backend.Service;

import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.TeacherRepository;
import com.tuition.backend.Repository.userRepository;
import com.tuition.backend.dto.TeacherDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeacherService {

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private userRepository userRepository;

    // Create teacher: only ADMIN (current authenticated user) can perform this.
    @Transactional
    public Teacher createTeacher(TeacherDto dto) {
        // Ensure unique teacherId
        if (dto.getTeacherId() == null || dto.getTeacherId().trim().isEmpty()) {
            throw new RuntimeException("teacherId is required");
        }
        if (teacherRepository.existsByTeacherId(dto.getTeacherId())) {
            throw new RuntimeException("teacherId already exists");
        }

        // Get currently authenticated user and check authorities (ROLE_ADMIN)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthenticated request");
        }

        User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new RuntimeException("Only ADMIN users can create teachers");
        }

        // Validate linked user exists
        if (dto.getUserId() == null) {
            throw new RuntimeException("userId (existing User) is required to create teacher");
        }
        User linkedUser = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("Linked user not found with id: " + dto.getUserId()));

        // Update linked user's role to TEACHER (and keep active)
        linkedUser.setRole("TEACHER");
        linkedUser.setIsActive(true);
        userRepository.save(linkedUser);

        // Create and persist teacher entity
        Teacher teacher = new Teacher();
        teacher.setUser(linkedUser);
        teacher.setFullName(dto.getFullName());
        teacher.setContactNumber(dto.getContactNumber());
        teacher.setNicNumber(dto.getNicNumber());
        teacher.setTeacherId(dto.getTeacherId());
        teacher.setSubjects(dto.getSubjects());
        teacher.setGender(dto.getGender());

        return teacherRepository.save(teacher);
    }

    // Get all teachers
    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    // Get teacher by id
    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
    }

    // Update teacher (admin or the teacher themself could be allowed; here we allow ADMIN or the linked teacher user)
    @Transactional
    public Teacher updateTeacher(Long id, TeacherDto dto) {
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));

        // Authorization: only ADMIN or the teacher's own user can update
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthenticated request");
        }
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        boolean isAdmin = "ADMIN".equalsIgnoreCase(currentUser.getRole());
        boolean isSelf = existing.getUser() != null && existing.getUser().getId().equals(currentUser.getId());
        if (!isAdmin && !isSelf) {
            throw new RuntimeException("Not authorized to update this teacher");
        }

        // Update fields if provided
        if (dto.getFullName() != null) existing.setFullName(dto.getFullName());
        if (dto.getContactNumber() != null) existing.setContactNumber(dto.getContactNumber());
        if (dto.getNicNumber() != null) existing.setNicNumber(dto.getNicNumber());
        if (dto.getSubjects() != null) existing.setSubjects(dto.getSubjects());
        if (dto.getGender() != null) existing.setGender(dto.getGender());

        // If teacherId change requested, ensure uniqueness
        if (dto.getTeacherId() != null && !dto.getTeacherId().equals(existing.getTeacherId())) {
            if (teacherRepository.existsByTeacherId(dto.getTeacherId())) {
                throw new RuntimeException("teacherId already exists");
            }
            existing.setTeacherId(dto.getTeacherId());
        }

        // Optionally allow linking to a different user (only ADMIN)
        if (dto.getUserId() != null && !dto.getUserId().equals(existing.getUser() != null ? existing.getUser().getId() : null)) {
            if (!isAdmin) {
                throw new RuntimeException("Only ADMIN can change the linked user for a teacher");
            }
            User newLinkedUser = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Linked user not found with id: " + dto.getUserId()));
            // set role of new linked user to TEACHER
            newLinkedUser.setRole("TEACHER");
            userRepository.save(newLinkedUser);
            existing.setUser(newLinkedUser);
        }

        return teacherRepository.save(existing);
    }

    // Delete teacher (hard delete). Only ADMIN can delete
    @Transactional
    public void deleteTeacher(Long id) {
        Teacher existing = teacherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthenticated request");
        }
        String currentUsername = auth.getName();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
            throw new RuntimeException("Only ADMIN can delete teachers");
        }

        // Optionally, reset linked user's role (e.g., to STUDENT or keep as is). Here we set linked user role back to STUDENT.
        User linkedUser = existing.getUser();
        if (linkedUser != null) {
            linkedUser.setRole("STUDENT"); // or some other default; choose based on your requirements
            userRepository.save(linkedUser);
        }

        teacherRepository.deleteById(id);
    }
}
