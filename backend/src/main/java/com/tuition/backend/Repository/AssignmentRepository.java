package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Assignment;
import com.tuition.backend.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findByCreatedBy(Teacher teacher);
    List<Assignment> findByGradeAndSubjectContaining(String grade, String subject);
    List<Assignment> findByGrade(String grade);
}
