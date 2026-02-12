package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Question;
import com.tuition.backend.Entity.Student;
import com.tuition.backend.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByStudent(Student student);
    List<Question> findByTeacher(Teacher teacher);
    List<Question> findByTeacherAndStatus(Teacher teacher, String status);
}
