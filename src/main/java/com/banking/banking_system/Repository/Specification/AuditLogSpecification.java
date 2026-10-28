package com.banking.banking_system.Repository.Specification;

import com.banking.banking_system.Entity.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class AuditLogSpecification {
    public static Specification<AuditLog> filterAuditLogs(
            String username,
            String action,
            String status,
            Instant startDate,
            Instant endDate) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (username != null && !username.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("username")), "%" + username.toLowerCase() + "%"));
            }

            if (action != null && !action.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("action")), action.toUpperCase()));
            }

            if (status != null && !status.isBlank()) {
                predicates.add(cb.equal(
                        cb.upper(root.get("status")),
                        status.toUpperCase()));
            }

            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), startDate));
            }

            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), endDate));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
