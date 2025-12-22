package com.tuition.backend.Repository;

import com.tuition.backend.Entity.TuitionClass;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TuitionClassRepository extends JpaRepository<TuitionClass, Long> {
}
