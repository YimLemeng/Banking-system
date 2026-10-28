package com.banking.banking_system.Service;

import com.banking.banking_system.Dto.account.TransactionResponse;
import com.banking.banking_system.Entity.Enum.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface TransactionService {
    TransactionResponse getTransactionById(String transactionId);
    Page<TransactionResponse> getAccountTransactions(Long accountId, Pageable pageable);
    Page<TransactionResponse> searchTransaction(
            Long accountId,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    );
}
