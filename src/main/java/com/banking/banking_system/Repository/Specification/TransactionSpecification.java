package com.banking.banking_system.Repository.Specification;

import com.banking.banking_system.Entity.BankTransaction;
import com.banking.banking_system.Entity.Enum.TransactionType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionSpecification {
    public static Specification<BankTransaction> filterTransactions(
            Long accountId,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAccount,
            BigDecimal maxAccount){
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (accountId != null) {
                Predicate sourceMatch = cb.equal(root.get("account").get("id"), accountId);
                Predicate targetMatch = cb.equal(root.get("targetAccount").get("id"), accountId);
                predicates.add(cb.or(sourceMatch, targetMatch));
            }
            if (type != null) {
                predicates.add(cb.equal(root.get("type"), type));
            }
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), endDate));
            }
            if (minAccount != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("amount"), minAccount));
            }
            if (maxAccount != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("amount"), maxAccount));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
