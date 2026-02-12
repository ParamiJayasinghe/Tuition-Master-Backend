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
    private com.tuition.backend.Service.FileStorageService fileStorageService;

    @Autowired
    private AssignmentService assignmentService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String fileName = fileStorageService.storeFile(file);
        
        // Return the downloadable URL
        String fileDownloadUri = org.springframework.web.servlet.support.ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(fileName)
                .toUriString();

        return ResponseEntity.ok(fileDownloadUri);
    }

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

    @PutMapping("/submissions/{submissionId}/mark")
    public ResponseEntity<AssignmentSubmissionDTO> markSubmission(
            @PathVariable Long submissionId,
            @RequestParam Integer marks) {
        return ResponseEntity.ok(assignmentService.markSubmission(submissionId, marks));
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
