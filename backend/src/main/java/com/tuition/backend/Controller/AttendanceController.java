package com.tuition.backend.Controller;

import com.tuition.backend.Service.AttendanceService;
import com.tuition.backend.dto.AttendanceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
//@CrossOrigin(origins = "http://localhost:3000") // Assuming standard valid React port, or global config handles it.
public class AttendanceController {

    @Autowired
    private AttendanceService attendanceService;

    @GetMapping
    public ResponseEntity<List<AttendanceDTO>> getAttendanceSheet(
            @RequestParam(required = false) String grade,
            @RequestParam(required = false) String subject,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<AttendanceDTO> sheet = attendanceService.getAttendanceSheet(grade, subject, date);
        return ResponseEntity.ok(sheet);
    }

    @PostMapping
    public ResponseEntity<List<AttendanceDTO>> markAttendance(
            @RequestBody List<AttendanceDTO> attendanceDtos,
            @RequestParam(required = false) String teacherEmail // Optional if we want to track who marked it via param
    ) {
        List<AttendanceDTO> saved = attendanceService.markAttendance(attendanceDtos, teacherEmail);
        return ResponseEntity.ok(saved);
    }
}
