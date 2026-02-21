package com.tuition.backend.Repository;

import com.tuition.backend.Entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByActorUsername(String actorUsername);
    List<AuditLog> findByAction(String action);
}
