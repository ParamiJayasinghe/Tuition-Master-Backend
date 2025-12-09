//package com.tuition.backend.Service;
//
//import com.tuition.backend.Entity.Teacher;
//import com.tuition.backend.Entity.User;
//import com.tuition.backend.Repository.TeacherRepository;
//import com.tuition.backend.Repository.userRepository;
//import com.tuition.backend.dto.TeacherDto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//public class TeacherService {
//
//    @Autowired
//    private TeacherRepository teacherRepository;
//
//    @Autowired
//    private userRepository userRepository;
//
//    // ⭐ CREATE — Only ADMIN
//    @PreAuthorize("hasRole('ADMIN')")
//    @Transactional
//    public Teacher createTeacher(TeacherDto dto) {
//
//        if (dto.getTeacherId() == null || dto.getTeacherId().trim().isEmpty()) {
//            throw new RuntimeException("teacherId is required");
//        }
//        if (teacherRepository.existsByTeacherId(dto.getTeacherId())) {
//            throw new RuntimeException("teacherId already exists");
//        }
//
//        if (dto.getUserId() == null) {
//            throw new RuntimeException("userId is required");
//        }
//
//        User linkedUser = userRepository.findById(dto.getUserId())
//                .orElseThrow(() -> new RuntimeException("Linked user not found with id: " + dto.getUserId()));
//
//        linkedUser.setRole("TEACHER");
//        linkedUser.setIsActive(true);
//        userRepository.save(linkedUser);
//
//        Teacher teacher = new Teacher();
//        teacher.setUser(linkedUser);
//        teacher.setFullName(dto.getFullName());
//        teacher.setContactNumber(dto.getContactNumber());
//        teacher.setNicNumber(dto.getNicNumber());
//        teacher.setTeacherId(dto.getTeacherId());
//        teacher.setSubjects(dto.getSubjects());
//        teacher.setGender(dto.getGender());
//
//        return teacherRepository.save(teacher);
//    }
//
//    // ⭐ READ — Everyone authenticated can see
//    public List<Teacher> getAllTeachers() {
//        return teacherRepository.findAll();
//    }
//
//    public Teacher getTeacherById(Long id) {
//        return teacherRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
//    }
//
//    // ⭐ UPDATE — ADMIN or the teacher themself
//    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
//    @Transactional
//    public Teacher updateTeacher(Long id, TeacherDto dto) {
//
//        Teacher existing = teacherRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
//
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        User currentUser = userRepository.findByUsername(auth.getName())
//                .orElseThrow(() -> new RuntimeException("Current user not found"));
//
//        boolean isAdmin = currentUser.getRole().equalsIgnoreCase("ADMIN");
//        boolean isSelf = existing.getUser() != null &&
//                existing.getUser().getId().equals(currentUser.getId());
//
//        if (!isAdmin && !isSelf) {
//            throw new RuntimeException("Not authorized to update this teacher");
//        }
//
//        if (dto.getFullName() != null) existing.setFullName(dto.getFullName());
//        if (dto.getContactNumber() != null) existing.setContactNumber(dto.getContactNumber());
//        if (dto.getNicNumber() != null) existing.setNicNumber(dto.getNicNumber());
//        if (dto.getSubjects() != null) existing.setSubjects(dto.getSubjects());
//        if (dto.getGender() != null) existing.setGender(dto.getGender());
//
//        if (dto.getTeacherId() != null && !dto.getTeacherId().equals(existing.getTeacherId())) {
//            if (teacherRepository.existsByTeacherId(dto.getTeacherId())) {
//                throw new RuntimeException("teacherId already exists");
//            }
//            existing.setTeacherId(dto.getTeacherId());
//        }
//
//        if (dto.getUserId() != null &&
//                !dto.getUserId().equals(existing.getUser() != null ? existing.getUser().getId() : null)) {
//
//            if (!isAdmin) {
//                throw new RuntimeException("Only ADMIN can change linked user");
//            }
//
//            User newLinkedUser = userRepository.findById(dto.getUserId())
//                    .orElseThrow(() ->
//                            new RuntimeException("Linked user not found with id: " + dto.getUserId())
//                    );
//
//            newLinkedUser.setRole("TEACHER");
//            userRepository.save(newLinkedUser);
//            existing.setUser(newLinkedUser);
//        }
//
//        return teacherRepository.save(existing);
//    }
//
//    // ⭐ DELETE — Only ADMIN
//    @PreAuthorize("hasRole('ADMIN')")
//    @Transactional
//    public void deleteTeacher(Long id) {
//
//        Teacher existing = teacherRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Teacher not found with id: " + id));
//
//        User linkedUser = existing.getUser();
//        if (linkedUser != null) {
//            linkedUser.setRole("STUDENT");
//            userRepository.save(linkedUser);
//        }
//
//        teacherRepository.deleteById(id);
//    }
//}
