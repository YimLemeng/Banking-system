package com.banking.banking_system.Mapper;

import com.banking.banking_system.Dto.account.AccountResponse;
import com.banking.banking_system.Entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponse toResponse(Account account) {
        if (account == null) return null;
        String customerName = null;
        Long customerId = null;
        if (account.getCustomer() != null) {
            customerId = account.getCustomer().getId();
            customerName = account.getCustomer().getFirstName() + " " + account.getCustomer().getLastName();
        }
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .status(account.getStatus())
                .customerId(customerId)
                .customerName(customerName)
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
