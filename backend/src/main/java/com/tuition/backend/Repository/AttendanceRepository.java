package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Attendance;
import com.tuition.backend.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByDateAndSubject(LocalDate date, String subject);
    Optional<Attendance> findByStudentAndDateAndSubject(Student student, LocalDate date, String subject);
    List<Attendance> findByStudent(Student student);
}
