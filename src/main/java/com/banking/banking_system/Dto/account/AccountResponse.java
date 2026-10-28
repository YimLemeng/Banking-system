package com.banking.banking_system.Dto.account;

import com.banking.banking_system.Entity.Enum.AccountStatus;
import com.banking.banking_system.Entity.Enum.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccountResponse {
    private Long id;
    private String accountNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private AccountStatus status;
    private Long customerId;
    private String customerName;
    private Instant createdAt;
    private Instant updatedAt;
}
