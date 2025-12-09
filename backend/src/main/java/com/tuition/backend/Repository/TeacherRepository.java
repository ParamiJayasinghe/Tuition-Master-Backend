package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Teacher;
import com.tuition.backend.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    Optional<Teacher> findByTeacherId(String teacherId);
    boolean existsByTeacherId(String teacherId);
    Optional<Teacher> findByUser(User user);
}
