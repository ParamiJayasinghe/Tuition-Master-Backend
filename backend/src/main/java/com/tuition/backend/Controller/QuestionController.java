package com.tuition.backend.Controller;

import com.tuition.backend.Service.QuestionService;
import com.tuition.backend.dto.QuestionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;

    @PostMapping("/ask")
    public ResponseEntity<QuestionDTO> askQuestion(
            Principal principal,
            @RequestParam Long teacherId,
            @RequestParam String text,
            @RequestParam(required = false) String fileUrl) {
        return ResponseEntity.ok(questionService.askQuestion(principal.getName(), teacherId, text, fileUrl));
    }

    @GetMapping("/teachers")
    public ResponseEntity<List<QuestionDTO>> getAvailableTeachers() {
        return ResponseEntity.ok(questionService.getAvailableTeachers());
    }

    @PostMapping("/{questionId}/answer")
    public ResponseEntity<QuestionDTO> answerQuestion(
            @PathVariable Long questionId,
            @RequestParam String text,
            @RequestParam(required = false) String fileUrl) {
        return ResponseEntity.ok(questionService.answerQuestion(questionId, text, fileUrl));
    }

    @GetMapping("/student")
    public ResponseEntity<List<QuestionDTO>> getStudentQuestions(Principal principal) {
        return ResponseEntity.ok(questionService.getStudentQuestions(principal.getName()));
    }

    @GetMapping("/teacher")
    public ResponseEntity<List<QuestionDTO>> getTeacherQuestions(
            Principal principal,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(questionService.getTeacherQuestions(principal.getName(), status));
    }
}
