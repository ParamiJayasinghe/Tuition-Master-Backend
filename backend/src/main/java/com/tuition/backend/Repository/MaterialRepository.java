package com.tuition.backend.Repository;

import com.tuition.backend.Entity.Material;
import com.tuition.backend.Entity.Teacher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findByCreatedBy(Teacher teacher);
    List<Material> findByGrade(String grade);
    List<Material> findByGradeAndSubject(String grade, String subject);
    List<Material> findByGradeAndSubjectAndLessonNameContaining(String grade, String subject, String lessonName);
}
