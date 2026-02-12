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

    @GetMapping("/student/{studentId}")
    public ResponseEntity<StudentPerformanceDTO> getStudentPerformance(@PathVariable Long studentId) {
        return ResponseEntity.ok(performanceService.getStudentPerformance(studentId));
    }
}
