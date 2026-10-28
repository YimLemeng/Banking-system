package com.banking.banking_system.Service.impl;

import com.banking.banking_system.Dto.audit.AuditLogResponse;
import com.banking.banking_system.Entity.AuditLog;
import com.banking.banking_system.Mapper.AuditLogMapper;
import com.banking.banking_system.Repository.AuditLogRepository;
import com.banking.banking_system.Repository.Specification.AuditLogSpecification;
import com.banking.banking_system.Service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {
    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String username, String action, String resource, String resourceId,
                    String description, String ipAddress, String status, String errorMessage, Long executionTimeMs) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .username(username != null ? username : "ANONYMOUS")
                    .action(action)
                    .resource(resource)
                    .resourceId(resourceId)
                    .description(description)
                    .ipAddress(ipAddress)
                    .status(status)
                    .errorMessage(errorMessage)
                    .executionTimeMs(executionTimeMs)
                    .build();
            auditLogRepository.save(auditLog);
            log.debug("AuditLog saved: {} - {} by {}", action, status, username);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage(), e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAllAuditLogs(Pageable  pageable) {
        return auditLogRepository.findAll(pageable).map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> searchAuditLogs(String username, String action, String status,
                                                  Instant startDate, Instant endDate, Pageable pageable) {
        Specification<AuditLog> spec = AuditLogSpecification.filterAuditLogs(username, action, status, startDate, endDate);
        return auditLogRepository.findAll(spec, pageable).map(auditLogMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long id) {
        AuditLog auditLog = auditLogRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("AuditLog with id: " + id + " not found"));
        return auditLogMapper.toResponse(auditLog);
    }
}
