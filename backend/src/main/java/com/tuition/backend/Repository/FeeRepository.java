package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Fee;
import com.tuition.backend.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long>, JpaSpecificationExecutor<Fee> {
    List<Fee> findByStudent(Student student);
    List<Fee> findByMonthAndYear(Integer month, Integer year);
    List<Fee> findByGrade(String grade);
    List<Fee> findByStatus(String status);
    
    // Complex queries can be handled with Specifications or @Query if needed, 
    // but for now simple JpaRepository methods combining fields might suffice 
    // or we can filter in service for complex combinations if volume is low.
    // However, finding by Grade AND Month AND Year AND Status is a likely requirement.
    
    List<Fee> findByGradeAndMonthAndYear(String grade, Integer month, Integer year);
    
    List<Fee> findByGradeAndMonthAndYearAndStatus(String grade, Integer month, Integer year, String status);

    List<Fee> findByStudentAndMonthAndYear(Student student, Integer month, Integer year);
    
    List<Fee> findBySubject(String subject);

    boolean existsByStudentAndSubjectAndMonthAndYearAndStatus(Student student, String subject, Integer month, Integer year, String status);
}
