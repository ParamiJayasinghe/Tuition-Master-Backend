package com.tuition.backend.Controller;

import com.tuition.backend.Service.AssignmentService;
import com.tuition.backend.dto.AssignmentDTO;
import com.tuition.backend.dto.AssignmentSubmissionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
//@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    private AssignmentService assignmentService;

    // --- Teacher Endpoints ---

    @PostMapping
    public ResponseEntity<AssignmentDTO> createAssignment(@RequestBody AssignmentDTO dto) {
        return ResponseEntity.ok(assignmentService.createAssignment(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AssignmentDTO> updateAssignment(@PathVariable Long id, @RequestBody AssignmentDTO dto) {
        return ResponseEntity.ok(assignmentService.updateAssignment(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        assignmentService.deleteAssignment(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/submissions")
    public ResponseEntity<List<AssignmentSubmissionDTO>> getSubmissions(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getSubmissionsForAssignment(id));
    }

    // --- Student Endpoints ---

    @PostMapping("/{id}/submit")
    public ResponseEntity<AssignmentSubmissionDTO> submitAssignment(
            @PathVariable Long id,
            @RequestBody AssignmentSubmissionDTO dto) {
        return ResponseEntity.ok(assignmentService.submitAssignment(id, dto));
    }

    // --- Shared Endpoints ---

    @GetMapping
    public ResponseEntity<List<AssignmentDTO>> getAssignments() {
        return ResponseEntity.ok(assignmentService.getAssignments());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentDTO> getAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(assignmentService.getAssignmentById(id));
    }
}
