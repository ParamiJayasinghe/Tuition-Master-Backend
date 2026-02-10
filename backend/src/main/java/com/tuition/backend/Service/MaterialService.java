package com.tuition.backend.Service;

import com.tuition.backend.Entity.Material;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Entity.User;
import com.tuition.backend.Repository.MaterialRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.Repository.TeacherRepository;
import com.tuition.backend.Repository.userRepository;
import com.tuition.backend.dto.MaterialDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MaterialService {

    @Autowired
    private MaterialRepository materialRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private userRepository userRepository;

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

    public MaterialDTO createMaterial(MaterialDTO dto) {
        Teacher teacher = getCurrentTeacher();

        Material material = new Material();
        material.setLessonName(dto.getLessonName());
        material.setSubject(dto.getSubject());
        material.setGrade(dto.getGrade());
        material.setFileUrl(dto.getFileUrl());
        material.setCreatedBy(teacher);

        Material saved = materialRepository.save(material);
        return mapToDTO(saved);
    }

    public List<MaterialDTO> getMaterials(String grade, String subject, String lessonName) {
        User user = getCurrentUser();
        List<Material> materials;

        if ("TEACHER".equals(user.getRole())) {
            Teacher teacher = teacherRepository.findByUser(user).orElseThrow();
            materials = materialRepository.findByCreatedBy(teacher);
        } else if ("STUDENT".equals(user.getRole())) {
            Student student = studentRepository.findByUser(user).orElseThrow();
            Teacher teacher = student.getCreatedBy(); // Student sees materials from their registered teacher
            materials = materialRepository.findByCreatedBy(teacher);

            // Filter for student's grade and subjects
            String[] studentSubjects = student.getSubjects() != null ? student.getSubjects().split(",") : new String[0];
            List<String> subjectList = Arrays.stream(studentSubjects)
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .collect(Collectors.toList());

            materials = materials.stream()
                    .filter(m -> m.getGrade().equalsIgnoreCase(student.getGrade()))
                    .filter(m -> subjectList.contains(m.getSubject().toLowerCase()))
                    .collect(Collectors.toList());
        } else {
            materials = new ArrayList<>();
        }

        // Apply additional filters if provided
        return materials.stream()
                .filter(m -> grade == null || m.getGrade().equalsIgnoreCase(grade))
                .filter(m -> subject == null || m.getSubject().equalsIgnoreCase(subject))
                .filter(m -> lessonName == null || m.getLessonName().toLowerCase().contains(lessonName.toLowerCase()))
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    public void deleteMaterial(Long id) {
        Teacher teacher = getCurrentTeacher();
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Material not found"));

        if (!material.getCreatedBy().getId().equals(teacher.getId())) {
            throw new RuntimeException("Unauthorized");
        }
        materialRepository.delete(material);
    }

    private MaterialDTO mapToDTO(Material m) {
        MaterialDTO dto = new MaterialDTO();
        dto.setId(m.getId());
        dto.setLessonName(m.getLessonName());
        dto.setSubject(m.getSubject());
        dto.setGrade(m.getGrade());
        dto.setFileUrl(m.getFileUrl());
        dto.setCreatedByTeacherId(m.getCreatedBy().getId());
        dto.setTeacherName(m.getCreatedBy().getFullName());
        dto.setCreatedAt(m.getCreatedAt());
        return dto;
    }
}
