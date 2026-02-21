package com.tuition.backend.Service;

import com.tuition.backend.Entity.AuditLog;
import com.tuition.backend.Repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tuition.backend.dto.AuditLogDTO;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String actorUsername, String actorRole, String targetType, String targetId, String details) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setActorUsername(actorUsername);
        auditLog.setActorRole(actorRole);
        auditLog.setTargetType(targetType);
        auditLog.setTargetId(targetId);
        auditLog.setDetails(details);
        auditLogRepository.save(auditLog);
    }

    public List<AuditLogDTO> getAllLogs() {
        System.out.println("AuditLogService: Fetching all logs from repository...");
        List<AuditLog> logs = auditLogRepository.findAll();
        System.out.println("AuditLogService: Found " + logs.size() + " logs.");
        
        return logs.stream()
                .map(this::mapToDTO)
                .sorted((a, b) -> {
                    if (a.getTimestamp() == null && b.getTimestamp() == null) return 0;
                    if (a.getTimestamp() == null) return 1;
                    if (b.getTimestamp() == null) return -1;
                    return b.getTimestamp().compareTo(a.getTimestamp());
                })
                .collect(Collectors.toList());
    }

    private AuditLogDTO mapToDTO(AuditLog log) {
        String formattedDate = null;
        if (log.getCreatedAt() != null) {
            formattedDate = log.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        
        return new AuditLogDTO(
                log.getId(),
                log.getAction(),
                log.getActorUsername(),
                log.getActorRole(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetails(),
                formattedDate
        );
    }
}
