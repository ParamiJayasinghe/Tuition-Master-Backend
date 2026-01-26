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

        // =========================
        // BOOTSTRAP: First ADMIN
        // =========================
        boolean adminExists = userRepository.existsByRole("ADMIN");

        if (!adminExists && "ADMIN".equalsIgnoreCase(dto.getRole())) {

            User admin = new User();
            admin.setUsername(dto.getUsername());
            admin.setPassword(passwordEncoder.encode(dto.getPassword()));
            admin.setEmail(dto.getEmail());
            admin.setRole("ADMIN");
            admin.setIsActive(true);

            return userRepository.save(admin);
        }

        // =========================
        // NORMAL AUTHENTICATED FLOW
        // =========================
        String requesterRole = authService.getCurrentUserRole();

        // ROLE ENFORCEMENT
        if ("TEACHER".equalsIgnoreCase(dto.getRole()) && !"ADMIN".equals(requesterRole)) {
            throw new RuntimeException("Only ADMIN can create teachers");
        }

        if ("STUDENT".equalsIgnoreCase(dto.getRole()) && !"TEACHER".equals(requesterRole)) {
            throw new RuntimeException("Only TEACHER can create students");
        }

        // Create base user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // =========================
        // Create TEACHER
        // =========================
        if ("TEACHER".equalsIgnoreCase(dto.getRole())) {

            Teacher t = new Teacher();
            t.setSubjects(dto.getTeacherDetails().getSubjects());
            t.setContactNumber(dto.getTeacherDetails().getContactNumber());
            t.setFullName(dto.getTeacherDetails().getFullName());
            t.setNicNumber(dto.getTeacherDetails().getNicNumber());
            t.setTeacherId(dto.getTeacherDetails().getTeacherId());
            t.setGender(dto.getTeacherDetails().getGender());
            t.setUser(savedUser);

            // Set createdBy (ADMIN)
            String currentUsername = authService.getCurrentUsername();
            User creator = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));

            t.setCreatedBy(creator);

            teacherRepository.save(t);
        }

        // =========================
        // Create STUDENT
        // =========================
        if ("STUDENT".equalsIgnoreCase(dto.getRole())) {

            Student s = new Student();
            s.setContactNumber(dto.getStudentDetails().getContactNumber());
            s.setFullName(dto.getStudentDetails().getFullName());
            s.setStudentId(dto.getStudentDetails().getStudentId());
            s.setSubjects(dto.getStudentDetails().getSubjects());
            s.setGrade(dto.getStudentDetails().getGrade());
            s.setDateOfBirth(dto.getStudentDetails().getDateOfBirth());
            s.setAddress(dto.getStudentDetails().getAddress());
            s.setGender(dto.getStudentDetails().getGender());
            s.setUser(savedUser);

            String currentUsername = authService.getCurrentUsername();
            User creator = userRepository.findByUsername(currentUsername)
                    .orElseThrow(() -> new RuntimeException("Current user not found"));

            // Link teacher only if creator is TEACHER
            if ("TEACHER".equalsIgnoreCase(creator.getRole())) {
                Teacher teacher = teacherRepository.findByUser(creator)
                        .orElseThrow(() -> new RuntimeException("Teacher profile not found for current user"));
                s.setCreatedBy(teacher);
            }

            studentRepository.save(s);
        }

        return savedUser;
    }

    // Get all users
    public List<User> getAllUsers() {
        String requesterRole = authService.getCurrentUserRole();
        String currentUsername = authService.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        if ("ADMIN".equalsIgnoreCase(requesterRole)) {
            // Admin sees only Teachers
            return userRepository.findAll().stream()
                    .filter(u -> "TEACHER".equalsIgnoreCase(u.getRole()))
                    .toList();
        } else if ("TEACHER".equalsIgnoreCase(requesterRole)) {
            // Find the Teacher entity for this user
            Optional<Teacher> teacherOpt = teacherRepository.findByUser(currentUser);
            if (teacherOpt.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            List<Student> students = studentRepository.findAllByCreatedBy(teacherOpt.get());
            return students.stream().map(Student::getUser).toList();
        } else {
            // Students or others see nothing (or maybe their own profile, but requirement implies teacher/admin focus)
            return java.util.Collections.emptyList();
        }
    }

    // Get user by id
    public User getUserById(Long id) {
        // Optional: Add permission check here too if needed, but user emphasized list/update/delete
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    // Update user
    @Transactional
    public User updateUser(Long id, UserDto userDTO) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        String requesterRole = authService.getCurrentUserRole();
        String currentUsername = authService.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Permission Check
        if ("TEACHER".equalsIgnoreCase(existing.getRole())) {
            if (!"ADMIN".equalsIgnoreCase(requesterRole)) {
                throw new RuntimeException("Only ADMIN can update teachers");
            }
        } else if ("STUDENT".equalsIgnoreCase(existing.getRole())) {
            // Admin cannot update students anymore
            if ("ADMIN".equalsIgnoreCase(requesterRole)) {
                throw new RuntimeException("ADMIN cannot update students");
            }
            
            boolean isCreator = false;
            // Check if creator
            Optional<Student> studentOpt = studentRepository.findByUser(existing);
            if (studentOpt.isPresent()) {
                Student s = studentOpt.get();
                // Check if createdBy (Teacher) is linked to currentUser
                if (s.getCreatedBy() != null && s.getCreatedBy().getUser().getId().equals(currentUser.getId())) {
                    isCreator = true;
                }
            }
            
            if (!isCreator) {
                throw new RuntimeException("Not authorized to update this student");
            }
        }

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

        // Update password if provided
        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existing.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        // Update role if provided (Only ADMIN should probably do this, but keeping flexible as per existing)
        if (userDTO.getRole() != null) {
             // Maybe restrict role change? For now leaving as is but ensuring case
            existing.setRole(userDTO.getRole().toUpperCase());
        }
        
        User savedUser = userRepository.save(existing);

        // Update details if provided
        if ("TEACHER".equalsIgnoreCase(existing.getRole()) && userDTO.getTeacherDetails() != null) {
            Optional<Teacher> teacherOpt = teacherRepository.findByUser(existing);
            if (teacherOpt.isPresent()) {
                Teacher t = teacherOpt.get();
                if(userDTO.getTeacherDetails().getFullName() != null) t.setFullName(userDTO.getTeacherDetails().getFullName());
                if(userDTO.getTeacherDetails().getContactNumber() != null) t.setContactNumber(userDTO.getTeacherDetails().getContactNumber());
                if(userDTO.getTeacherDetails().getSubjects() != null) t.setSubjects(userDTO.getTeacherDetails().getSubjects());
                if(userDTO.getTeacherDetails().getNicNumber() != null) t.setNicNumber(userDTO.getTeacherDetails().getNicNumber());
                if(userDTO.getTeacherDetails().getTeacherId() != null) t.setTeacherId(userDTO.getTeacherDetails().getTeacherId());
                if(userDTO.getTeacherDetails().getGender() != null) t.setGender(userDTO.getTeacherDetails().getGender());
                teacherRepository.save(t);
            }
        } else if ("STUDENT".equalsIgnoreCase(existing.getRole()) && userDTO.getStudentDetails() != null) {
            Optional<Student> studentOpt = studentRepository.findByUser(existing);
            if (studentOpt.isPresent()) {
                Student s = studentOpt.get();
                if(userDTO.getStudentDetails().getFullName() != null) s.setFullName(userDTO.getStudentDetails().getFullName());
                if(userDTO.getStudentDetails().getContactNumber() != null) s.setContactNumber(userDTO.getStudentDetails().getContactNumber());
                if(userDTO.getStudentDetails().getSubjects() != null) s.setSubjects(userDTO.getStudentDetails().getSubjects());
                if(userDTO.getStudentDetails().getStudentId() != null) s.setStudentId(userDTO.getStudentDetails().getStudentId());
                if(userDTO.getStudentDetails().getGender() != null) s.setGender(userDTO.getStudentDetails().getGender());
                if(userDTO.getStudentDetails().getDateOfBirth() != null) s.setDateOfBirth(userDTO.getStudentDetails().getDateOfBirth());
                if(userDTO.getStudentDetails().getAddress() != null) s.setAddress(userDTO.getStudentDetails().getAddress());
                studentRepository.save(s);
            }
        }

        return savedUser;
    }

    // Delete user (hard delete)
    @Transactional
    public void deleteUser(Long id) {
        User existing = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        String requesterRole = authService.getCurrentUserRole();
        String currentUsername = authService.getCurrentUsername();
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("Current user not found"));

        // Permission Check
        if ("TEACHER".equalsIgnoreCase(existing.getRole())) {
            if (!"ADMIN".equalsIgnoreCase(requesterRole)) {
                throw new RuntimeException("Only ADMIN can delete teachers");
            }
        } else if ("STUDENT".equalsIgnoreCase(existing.getRole())) {
            // Admin cannot delete students anymore
            if ("ADMIN".equalsIgnoreCase(requesterRole)) {
                 throw new RuntimeException("ADMIN cannot delete students");
            }

            boolean isCreator = false;
            // Check if creator
            Optional<Student> studentOpt = studentRepository.findByUser(existing);
            if (studentOpt.isPresent()) {
                Student s = studentOpt.get();
                if (s.getCreatedBy() != null && s.getCreatedBy().getUser().getId().equals(currentUser.getId())) {
                    isCreator = true;
                }
            }
            
            if (!isCreator) {
                throw new RuntimeException("Not authorized to delete this student");
            }
        }

        userRepository.deleteById(id);
    }
}

