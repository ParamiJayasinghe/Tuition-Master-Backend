package com.tuition.backend.Controller;

import com.tuition.backend.Service.PerformanceService;
import com.tuition.backend.dto.StudentPerformanceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/performance")
public class PerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @GetMapping("/me")
    public ResponseEntity<StudentPerformanceDTO> getMyPerformance(java.security.Principal principal) {
        return ResponseEntity.ok(performanceService.getMyPerformance(principal.getName()));
    }

    @GetMapping("/student/{userId}")
    public ResponseEntity<StudentPerformanceDTO> getStudentPerformance(@PathVariable Long userId) {
        return ResponseEntity.ok(performanceService.getStudentPerformance(userId));
    }
}
