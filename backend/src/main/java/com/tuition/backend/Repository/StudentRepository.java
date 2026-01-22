package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    boolean existsByStudentId(String studentId);

    List<Student> findAllByCreatedBy(com.tuition.backend.Entity.Teacher teacher);

    java.util.Optional<Student> findByUser(User user);
}


