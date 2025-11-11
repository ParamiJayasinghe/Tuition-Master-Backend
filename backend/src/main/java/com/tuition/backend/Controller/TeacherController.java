package com.tuition.backend.Controller;
import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Service.TeacherService;
import com.tuition.backend.dto.TeacherDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    @Autowired
    private TeacherService teacherService;

    // Create a teacher (only ADMIN allowed; service enforces)
    @PostMapping
    public ResponseEntity<Teacher> createTeacher(@RequestBody TeacherDto dto) {
        Teacher created = teacherService.createTeacher(dto);
        return ResponseEntity.ok(created);
    }

    // Get all teachers
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers() {
        List<Teacher> list = teacherService.getAllTeachers();
        return ResponseEntity.ok(list);
    }

    // Get teacher by id
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
        Teacher t = teacherService.getTeacherById(id);
        return ResponseEntity.ok(t);
    }

    // Update teacher (ADMIN or the teacher themselves)
    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @RequestBody TeacherDto dto) {
        Teacher updated = teacherService.updateTeacher(id, dto);
        return ResponseEntity.ok(updated);
    }

    // Delete teacher (only ADMIN)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
        teacherService.deleteTeacher(id);
        return ResponseEntity.noContent().build();
    }
}
