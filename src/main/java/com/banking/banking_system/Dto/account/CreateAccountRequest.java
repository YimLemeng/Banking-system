package com.banking.banking_system.Dto.account;

import com.banking.banking_system.Entity.Enum.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateAccountRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Account type is required (e.g. SAVINGS, CHECKING")
    private AccountType accountType;

    private BigDecimal initialDeposit;
}
