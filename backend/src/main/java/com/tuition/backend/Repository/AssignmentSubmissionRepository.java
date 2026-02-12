package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Assignment;
import com.tuition.backend.Entity.AssignmentSubmission;
import com.tuition.backend.Entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
    List<AssignmentSubmission> findByAssignment(Assignment assignment);
    Optional<AssignmentSubmission> findByAssignmentAndStudent(Assignment assignment, Student student);
    List<AssignmentSubmission> findByStudent(Student student);
}
