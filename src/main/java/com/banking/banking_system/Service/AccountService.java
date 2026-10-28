package com.banking.banking_system.Service;

import com.banking.banking_system.Dto.account.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AccountService {
    AccountResponse createAccount(CreateAccountRequest request);
    AccountResponse getAccountById(Long id);
    AccountResponse getAccountByNumber(String accountNumber);
    Page<AccountResponse> getAccountByCustomerId(Long customerId, Pageable pageable);
    Page<AccountResponse> getAllAccounts(Pageable pageable);
    AccountResponse deposit(String accountNumber, DepositRequest request);
    AccountResponse withdraw(String accountNumber, WithdrawRequest request);
    TransactionResponse transfer(TransferRequest request);
}
