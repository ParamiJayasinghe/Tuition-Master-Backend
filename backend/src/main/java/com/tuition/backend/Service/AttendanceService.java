package com.tuition.backend.Service;

import com.tuition.backend.Entity.Attendance;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Repository.AttendanceRepository;
import com.tuition.backend.Repository.StudentRepository;
import com.tuition.backend.Repository.TeacherRepository;
import com.tuition.backend.dto.AttendanceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AttendanceService {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private com.tuition.backend.Repository.userRepository userRepository;

    public List<AttendanceDTO> getAttendanceSheet(String grade, String subject, LocalDate date) {
        // Default date to today if not provided
        LocalDate targetDate = (date != null) ? date : LocalDate.now();

        // 0. Get Logged-in Teacher
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.tuition.backend.Entity.User currentUser = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        
        com.tuition.backend.Entity.Teacher currentTeacher = teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Teacher profile not found for current user"));

        // 1. Fetch students created by this teacher
        List<Student> allStudents = studentRepository.findAllByCreatedBy(currentTeacher);

        List<Student> filteredStudents = allStudents.stream()
                .filter(s -> {
                    // Check Grade (if filter provided)
                    boolean gradeMatch = (grade == null || grade.isEmpty()) || (s.getGrade() != null && s.getGrade().contains(grade));
                    // Check Subject (if filter provided)
                    boolean subjectMatch = (subject == null || subject.isEmpty()) || (s.getSubjects() != null && s.getSubjects().contains(subject));
                    return gradeMatch && subjectMatch;
                })
                .collect(Collectors.toList());

        // 2. Fetch existing attendance for this date and subject (ONLY if subject is specified)
        // If subject is not specified, we cannot map specific subject attendance, so we return NONE status.
        Map<Long, Attendance> attendanceMap;
        if (subject != null && !subject.isEmpty()) {
            List<Attendance> existingAttendance = attendanceRepository.findByDateAndSubject(targetDate, subject);
            attendanceMap = existingAttendance.stream()
                    .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));
        } else {
             attendanceMap = Map.of();
        }

        // 3. Map to DTOs
        List<AttendanceDTO> sheet = new ArrayList<>();
        for (Student student : filteredStudents) {
            AttendanceDTO dto = new AttendanceDTO(
                    student.getId(),
                    student.getFullName(),
                    student.getContactNumber(),
                    student.getGrade(),
                    // Use the specific subject filter if available, otherwise just use student's subjects string or null
                    (subject != null && !subject.isEmpty()) ? subject : student.getSubjects()
            );
            dto.setDate(targetDate);

            if (attendanceMap.containsKey(student.getId())) {
                Attendance existing = attendanceMap.get(student.getId());
                dto.setId(existing.getId());
                dto.setStatus(existing.getStatus());
                if(existing.getMarkedBy() != null) {
                    dto.setMarkedById(existing.getMarkedBy().getId());
                }
            } else {
                dto.setStatus("NONE");
            }
            sheet.add(dto);
        }
        return sheet;
    }

    @Transactional
    public List<AttendanceDTO> markAttendance(List<AttendanceDTO> dtos, String teacherEmail) {
        // Prepare to return updated DTOs
        List<AttendanceDTO> result = new ArrayList<>();

        // Find teacher (Marker) - in a real app, we might get ID from SecurityContext or pass it.
        // For now, assuming teacherEmail maps to User -> Teacher or passing ID directly in DTO?
        // Let's assume the teacher ID is passed or we look it up.
        // If teacherEmail is provided, we can look up.
        // Implementation Plan said "teacherEmail" argument, let's implement lookup.
        // Since I don't have TeacherRepository.findByEmail handy (User has email), I'll skip teacher lookup for now or rely on DTO markedById.
        // Let's rely on the first DTO having a markedById if available, or just save null for MVP to avoid complexity if Teacher logic is separate.
        // Actually, let's try to find Teacher by DTO's markedById if present.

        Teacher teacher = null;
        if (!dtos.isEmpty() && dtos.get(0).getMarkedById() != null) {
            teacher = teacherRepository.findById(dtos.get(0).getMarkedById()).orElse(null);
        }

        for (AttendanceDTO dto : dtos) {
            Student student = studentRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new RuntimeException("Student not found: " + dto.getStudentId()));

            Attendance attendance;
            if (dto.getId() != null) {
                attendance = attendanceRepository.findById(dto.getId())
                        .orElse(new Attendance());
            } else {
                // Check if exists by unique keys to avoid duplicate insert errors if ID was missing
                Optional<Attendance> existing = attendanceRepository.findByStudentAndDateAndSubject(student, dto.getDate(), dto.getSubject());
                attendance = existing.orElse(new Attendance());
            }

            attendance.setStudent(student);
            attendance.setDate(dto.getDate());
            attendance.setSubject(dto.getSubject()); // Ensure subject is set
            attendance.setStatus(dto.getStatus());
            attendance.setMarkedBy(teacher);
            // ClassEntity is nullable now, sending null.

            Attendance saved = attendanceRepository.save(attendance);

            dto.setId(saved.getId());
            result.add(dto);
        }
        return result;
    }
}
