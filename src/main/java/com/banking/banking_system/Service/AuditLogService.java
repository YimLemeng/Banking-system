package com.banking.banking_system.Service;

import com.banking.banking_system.Dto.audit.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AuditLogService {
    void log(String username, String action, String resource, String resourceId, String description, String ipAddress,
             String status, String errorMessage, Long executionTimeMs);
    Page<AuditLogResponse> getAllAuditLogs(Pageable pageable);
    Page<AuditLogResponse> searchAuditLogs(String username, String action, String status, Instant startTime,
                                           Instant endTime, Pageable pageable);
    AuditLogResponse getAuditLogById(Long id);
}
