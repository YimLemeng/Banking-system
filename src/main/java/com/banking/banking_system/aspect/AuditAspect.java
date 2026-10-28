package com.banking.banking_system.aspect;

import com.banking.banking_system.Dto.account.AccountResponse;
import com.banking.banking_system.Dto.account.TransactionResponse;
import com.banking.banking_system.Dto.account.TransferRequest;
import com.banking.banking_system.Dto.auth.LoginRequest;
import com.banking.banking_system.Dto.auth.RegisterRequest;
import com.banking.banking_system.Dto.customer.CustomerResponse;
import com.banking.banking_system.Service.AuditLogService;
import com.banking.banking_system.annotation.Audited;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {
    private final AuditLogService auditLogService;

    @Around("@annotation(audited)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Audited audited) throws Throwable {
        long startTime = System.currentTimeMillis();
        String username = resolveUsername(joinPoint);
        String ipAddress = getClientIpAddress();
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            String resourceId = resolveResourceId(joinPoint, result, audited);
            auditLogService.log(
                    username,
                    audited.action(),
                    audited.resource(),
                    resourceId,
                    audited.description(),
                    ipAddress,
                    "SUCCESS",
                    null,
                    executionTime
            );
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            String resourceId = resolveResourceId(joinPoint, null, audited);
            auditLogService.log(
                    username,
                    audited.action(),
                    audited.resource(),
                    resourceId,
                    audited.description(),
                    ipAddress,
                    "FAILED",
                    ex.getMessage(),
                    executionTime
            );
            throw ex;
        }
    }

    private String resolveResourceId(ProceedingJoinPoint joinPoint, Object result, Audited audited) {
        try {
            Object body = result;
            if (result instanceof ResponseEntity<?> responseEntity) {
                body = responseEntity.getBody();
            }

            String resource = audited.resource();
            if ("User".equalsIgnoreCase(resource)) {
                for (Object arg : joinPoint.getArgs()) {
                    if (arg instanceof LoginRequest loginRequest) {
                        return loginRequest.getIdentifier();
                    }
                    if (arg instanceof RegisterRequest registerRequest) {
                        return registerRequest.getUsername() != null ? registerRequest.getUsername() : registerRequest.getPhoneNumber();
                    }
                }
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
                    return auth.getName();
                }
            } else if ("Account".equalsIgnoreCase(resource)) {
                if (body instanceof AccountResponse accountResponse) {
                    return accountResponse.getAccountNumber();
                }
                if (body instanceof TransactionResponse transactionResponse) {
                    return transactionResponse.getAccountNumber();
                }
                for (Object arg : joinPoint.getArgs()) {
                    if (arg instanceof String str && str.matches("^\\d{6,16}$")) {
                        return str;
                    }
                    if (arg instanceof TransferRequest transferReq) {
                        return transferReq.getSourceAccountNumber();
                    }
                }
            } else if ("Customer".equalsIgnoreCase(resource)) {
                if (body instanceof CustomerResponse customerResponse) {
                    return "CUST-#" + customerResponse.getId();
                }
                for (Object arg : joinPoint.getArgs()) {
                    if (arg instanceof Long id) {
                        return "CUST-#" + id;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Unable to resolve resourceId: {}", e.getMessage());
        }
        return null;
    }

    private String resolveUsername(ProceedingJoinPoint joinPoint) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof LoginRequest loginRequest) {
                return loginRequest.getIdentifier();
            }
            if (arg instanceof RegisterRequest registerRequest) {
                return registerRequest.getPhoneNumber() != null ? registerRequest.getPhoneNumber() : registerRequest.getUsername();
            }
        }
        return "ANONYMOUS";
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String xfHeader = request.getHeader("X-Forwarded-For");
                if (xfHeader != null && !xfHeader.isBlank()) {
                    return xfHeader.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Unable to determine client IP: {}", e.getMessage());
        }
        return "UNKNOWN";
    }
}
