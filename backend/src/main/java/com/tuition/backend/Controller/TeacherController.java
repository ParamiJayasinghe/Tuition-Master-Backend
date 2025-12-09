//package com.tuition.backend.Controller;
//
//import com.tuition.backend.Entity.Teacher;
//import com.tuition.backend.Service.TeacherService;
//import com.tuition.backend.dto.TeacherDto;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/teachers")
//public class TeacherController {
//
//    @Autowired
//    private TeacherService teacherService;
//
//    // Only ADMIN can create
//    @PreAuthorize("hasRole('ADMIN')")
//    @PostMapping
//    public ResponseEntity<Teacher> createTeacher(@RequestBody TeacherDto dto) {
//        return ResponseEntity.ok(teacherService.createTeacher(dto));
//    }
//
//    // Anyone authenticated can view
//    @GetMapping
//    public ResponseEntity<List<Teacher>> getAllTeachers() {
//        return ResponseEntity.ok(teacherService.getAllTeachers());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Teacher> getTeacherById(@PathVariable Long id) {
//        return ResponseEntity.ok(teacherService.getTeacherById(id));
//    }
//
//    // Admin or teacher themselves
//    @PreAuthorize("hasAnyRole('ADMIN','TEACHER')")
//    @PutMapping("/{id}")
//    public ResponseEntity<Teacher> updateTeacher(@PathVariable Long id, @RequestBody TeacherDto dto) {
//        return ResponseEntity.ok(teacherService.updateTeacher(id, dto));
//    }
//
//    // Only ADMIN
//    @PreAuthorize("hasRole('ADMIN')")
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteTeacher(@PathVariable Long id) {
//        teacherService.deleteTeacher(id);
//        return ResponseEntity.noContent().build();
//    }
//}
