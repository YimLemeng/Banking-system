package com.banking.banking_system.Dto.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private String username;
    private String action;
    private String resource;
    private String resourceId;
    private String description;
    private String ipAddress;
    private String status;
    private String errorMessage;
    private Long executionTimeMs;
    private Instant createdAt;
}
