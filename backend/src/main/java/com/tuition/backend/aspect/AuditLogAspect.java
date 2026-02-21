package com.tuition.backend.aspect;

import com.tuition.backend.Service.AuditLogService;
import com.tuition.backend.config.AuditLog;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;

@Aspect
@Component
public class AuditLogAspect {

    @Autowired
    private AuditLogService auditLogService;

    @AfterReturning(pointcut = "@annotation(com.tuition.backend.config.AuditLog)", returning = "result")
    public void logAction(JoinPoint joinPoint, Object result) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return;
            }

            String username = authentication.getName();
            String role = authentication.getAuthorities().toString();

            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            AuditLog auditLogAnnotation = method.getAnnotation(AuditLog.class);

            String action = auditLogAnnotation.action();
            String targetType = auditLogAnnotation.targetType();
            
            // Try to extract ID from parameters if it's an update/delete
            String targetId = extractTargetId(joinPoint, result);
            
            String details = String.format("Method: %s, Args: %s", 
                    method.getName(), 
                    Arrays.toString(joinPoint.getArgs()));

            auditLogService.log(action, username, role, targetType, targetId, details);
        } catch (Exception e) {
            // Logging should not break the business logic
            System.err.println("Audit Logging failed: " + e.getMessage());
        }
    }

    private String extractTargetId(JoinPoint joinPoint, Object result) {
        // This is a simple heuristic: if the first argument is a Long or String, it might be the ID
        // Or if the result has an getId method
        Object[] args = joinPoint.getArgs();
        if (args.length > 0) {
            if (args[0] instanceof Long || args[0] instanceof String) {
                return args[0].toString();
            }
        }
        
        if (result != null) {
            try {
                Method getIdMethod = result.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(result);
                if (id != null) return id.toString();
            } catch (Exception ignored) {}
        }
        
        return null;
    }
}
