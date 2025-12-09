package com.tuition.backend.Service;

import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.userRepository;
import com.tuition.backend.Repository.TeacherRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.Service.AuthService;
import com.tuition.backend.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private userRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Transactional

    public User createUser(UserDto dto) {

        String requesterRole = authService.getCurrentUserRole();

        // ROLE ENFORCEMENT
        if (dto.getRole().equalsIgnoreCase("TEACHER") && !requesterRole.equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can create teachers");
        }

        if (dto.getRole().equalsIgnoreCase("STUDENT") &&
                !(requesterRole.equals("ADMIN") || requesterRole.equals("TEACHER"))) {
            throw new RuntimeException("Only ADMIN or TEACHER can create students");
        }

        // Create base user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Create teacher
        if (dto.getRole().equals("TEACHER")) {
            Teacher t = new Teacher();
            t.setSubjects(dto.getTeacherDetails().getSubjects());
            t.setContactNumber(dto.getTeacherDetails().getContactNumber());
            t.setFullName(dto.getTeacherDetails().getFullName());
            t.setNicNumber(dto.getTeacherDetails().getNicNumber());
            t.setTeacherId(dto.getTeacherDetails().getTeacherId());
            t.setGender(dto.getTeacherDetails().getGender());
            t.setUser(savedUser);
            teacherRepository.save(t);
        }

        // Create student
        if (dto.getRole().equals("STUDENT")) {
            Student s = new Student();
            s.setContactNumber(dto.getStudentDetails().getContactNumber());
            s.setFullName(dto.getStudentDetails().getFullName());
            s.setStudentId(dto.getStudentDetails().getStudentId());
            s.setSubjects(dto.getStudentDetails().getSubjects());
            s.setDateOfBirth(dto.getStudentDetails().getDateOfBirth());
            s.setAddress(dto.getStudentDetails().getAddress());
            s.setGender(dto.getStudentDetails().getGender());
            String currentUsername = authService.getCurrentUsername();
            User creator = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));
            s.setCreatedBy(creator);

            s.setUser(savedUser);
            studentRepository.save(s);
        }

        return savedUser;
    }
    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by id
    public User getUserById(Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Update user
    public User updateUser(Long id, UserDto userDTO) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        // If username changed, ensure uniqueness
        String newUsername = userDTO.getUsername();
        if (newUsername != null && !newUsername.equals(existing.getUsername())) {
            if (userRepository.existsByUsername(newUsername)) {
                throw new RuntimeException("Username already exists");
            }
            existing.setUsername(newUsername);
        }

        // If email changed, ensure uniqueness
        String newEmail = userDTO.getEmail();
        if (newEmail != null && !newEmail.equals(existing.getEmail())) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new RuntimeException("Email already exists");
            }
            existing.setEmail(newEmail);
        }

        // Update password if provided (remember to encode later)
        if (userDTO.getPassword() != null) {
            existing.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Update role if provided
        if (userDTO.getRole() != null) {
            existing.setRole(userDTO.getRole().toUpperCase());
        }

        // Optionally update isActive via DTO (if you add it). For now we leave it as-is.

        return userRepository.save(existing);
    }

    // Delete user (hard delete)
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}

