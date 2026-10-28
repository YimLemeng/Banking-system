package com.banking.banking_system.Dto.account;

import com.banking.banking_system.Entity.Enum.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionResponse {
    private Long id;
    private String transactionId;
    private TransactionType type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String accountNumber;
    private String targetAccountNumber;
    private String description;
    private LocalDateTime timestamp;
}
