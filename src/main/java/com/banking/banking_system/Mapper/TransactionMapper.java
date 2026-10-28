package com.banking.banking_system.Mapper;

import com.banking.banking_system.Dto.account.TransactionResponse;
import com.banking.banking_system.Entity.BankTransaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {
    public TransactionResponse toResponse(BankTransaction transaction) {
        if (transaction == null) return null;
        return TransactionResponse.builder()
                .id(transaction.getId())
                .transactionId(transaction.getTransactionId())
                .type(transaction.getType())
                .amount(transaction.getAmount())
                .balanceAfter(transaction.getBalanceAfter())
                .accountNumber(transaction.getAccount() != null ? transaction.getAccount().getAccountNumber() : null)
                .targetAccountNumber(transaction.getTargetAccount() != null ? transaction.getTargetAccount().getAccountNumber() : null)
                .description(transaction.getDescription())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
