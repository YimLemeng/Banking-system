package com.banking.banking_system.Mapper;

import com.banking.banking_system.Dto.audit.AuditLogResponse;
import com.banking.banking_system.Entity.AuditLog;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {
    public AuditLogResponse toResponse(AuditLog auditLog) {
        if  (auditLog == null) return null;
        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .username(auditLog.getUsername())
                .action(auditLog.getAction())
                .resource(auditLog.getResource())
                .resourceId(auditLog.getResourceId())
                .description(auditLog.getDescription())
                .ipAddress(auditLog.getIpAddress())
                .status(auditLog.getStatus())
                .errorMessage(auditLog.getErrorMessage())
                .executionTimeMs(auditLog.getExecutionTimeMs())
                .createdAt(auditLog.getCreatedAt())
                .build();
    }
}
