package com.banking.banking_system.Service.impl;

import com.banking.banking_system.Dto.account.*;
import com.banking.banking_system.Entity.Account;
import com.banking.banking_system.Entity.BankTransaction;
import com.banking.banking_system.Entity.Customer;
import com.banking.banking_system.Entity.Enum.AccountStatus;
import com.banking.banking_system.Entity.Enum.TransactionType;
import com.banking.banking_system.Exception.AccountNotFoundException;
import com.banking.banking_system.Exception.CustomerNotFoundException;
import com.banking.banking_system.Exception.InsufficientBalanceException;
import com.banking.banking_system.Mapper.AccountMapper;
import com.banking.banking_system.Mapper.TransactionMapper;
import com.banking.banking_system.Repository.AccountRepository;
import com.banking.banking_system.Repository.CustomerRepository;
import com.banking.banking_system.Repository.TransactionRepository;
import com.banking.banking_system.Service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        log.info("Creating account for customer id: {}, type: {}", request.getCustomerId(), request.getAccountType());
        Customer customer = customerRepository.findById(request.getCustomerId()).orElseThrow(() ->
                new CustomerNotFoundException("Customer not found with id: " + request.getCustomerId()));

        String accountNumber = generateUniqueAccountNumber();
        BigDecimal initialBalance = (request.getInitialDeposit() != null && request.getInitialDeposit().compareTo(BigDecimal.ZERO) > 0)
                ? request.getInitialDeposit() : BigDecimal.ZERO;

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(initialBalance)
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();
        Account savedAccount = accountRepository.save(account);

        if (initialBalance.compareTo(BigDecimal.ZERO) > 0) {
            BankTransaction initialDepositTx = BankTransaction.builder()
                    .transactionId(UUID.randomUUID().toString())
                    .type(TransactionType.DEPOSIT)
                    .amount(initialBalance)
                    .balanceAfter(initialBalance)
                    .account(savedAccount)
                    .description("Initial account opening deposit")
                    .build();
            transactionRepository.save(initialDepositTx);
        }
        log.info("Successfully created account {} with initial balance {}", accountNumber, initialBalance);
        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountById(Long id) {
        log.debug("Fetching account by id {}", id);
        Account account = accountRepository.findById(id).orElseThrow(() ->
                new AccountNotFoundException("Account not found with id: " + id));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse getAccountByNumber(String accountNumber) {
        log.debug("Fetching account by number {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() ->
                new AccountNotFoundException("Account not found with number: " + accountNumber));
        return accountMapper.toResponse(account);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAccountByCustomerId(Long customerId, Pageable pageable){
        log.debug("Fetching account by customer id {}", customerId);
        if (!customerRepository.existsById(customerId)) {
            throw new CustomerNotFoundException("Customer not found with id: " + customerId);
        }
        return accountRepository.findByCustomerId(customerId, pageable).map(accountMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AccountResponse> getAllAccounts(Pageable pageable) {
        log.debug("Fetching all accounts for pageable {}", pageable);
        return accountRepository.findAll(pageable).map(accountMapper::toResponse);
    }

    @Override
    @Transactional
    public AccountResponse deposit(String accountNumber, DepositRequest request) {
        log.info("Depositing amount {} to account {}", request.getAmount(), accountNumber);
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be strictly greater than zero");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() ->
                new AccountNotFoundException("Account not found with number: " + accountNumber));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot deposit into an inactive or frozen account: " + account.getStatus());
        }

        BigDecimal newBalance = account.getBalance().add(request.getAmount());
        account.setBalance(newBalance);
        Account savedAccount = accountRepository.save(account);

        BankTransaction transaction = BankTransaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .account(savedAccount)
                .description(request.getDescription() != null ? request.getDescription() : "Deposited amount")
                .build();
        transactionRepository.save(transaction);
        log.info("Successfully deposited {} to {}. New balance: {}", request.getAmount(), accountNumber, newBalance);
        return accountMapper.toResponse(savedAccount);
    }

    public AccountResponse withdraw(String accountNumber, WithdrawRequest request) {
        log.info("Withdrawing amount {} from account {}", request.getAmount(), accountNumber);
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be strictly greater than zero");
        }
        Account account = accountRepository.findByAccountNumber(accountNumber).orElseThrow(() ->
                new AccountNotFoundException("Account not found with number: " + accountNumber));
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot withdraw from an inactive or frozen account: " + account.getStatus());
        }
        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + account.getBalance() + ", Requested: " + request.getAmount());
        }

        BigDecimal newBalance = account.getBalance().subtract(request.getAmount());
        account.setBalance(newBalance);
        Account savedAccount = accountRepository.save(account);

        BankTransaction transaction = BankTransaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(TransactionType.WITHDRAW)
                .amount(request.getAmount())
                .balanceAfter(newBalance)
                .account(savedAccount)
                .description(request.getDescription() != null ? request.getDescription() : "Withdrawal amount")
                .build();
        transactionRepository.save(transaction);
        log.info("Successfully withdraw {} from {}. New balance: {}", request.getAmount(), accountNumber, newBalance);
        return accountMapper.toResponse(savedAccount);
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Transferring {} from {} to {}", request.getAmount(), request.getSourceAccountNumber(), request.getTargetAccountNumber());
        if (request.getSourceAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new IllegalArgumentException("Source and target account numbers cannot be identical");
        }
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be strictly greater than zero");
        }

        Account sourceAccount = accountRepository.findByAccountNumber(request.getSourceAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Source account not found with number: " + request.getSourceAccountNumber()));

        Account targetAccount = accountRepository.findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Target account not found with number: " + request.getTargetAccountNumber()));

        if (sourceAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Source account is not active" + sourceAccount.getStatus());
        }
        if (targetAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Target account is not active" + targetAccount.getStatus());
        }
        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " + sourceAccount.getBalance() + ", Requested: " + request.getAmount());
        }

        BigDecimal newSourceBalance = sourceAccount.getBalance().subtract(request.getAmount());
        sourceAccount.setBalance(newSourceBalance);
        accountRepository.save(sourceAccount);

        BigDecimal newTargetBalance = targetAccount.getBalance().add(request.getAmount());
        targetAccount.setBalance(newTargetBalance);
        accountRepository.save(targetAccount);

        String txId = UUID.randomUUID().toString();
        BankTransaction transaction = BankTransaction.builder()
                .transactionId(txId)
                .type(TransactionType.TRANSFER)
                .amount(request.getAmount())
                .balanceAfter(newSourceBalance)
                .account(sourceAccount)
                .targetAccount(targetAccount)
                .description(request.getDescription() != null ? request.getDescription() : "Transfer to " + targetAccount.getAccountNumber())
                .build();
        BankTransaction savedTx = transactionRepository.save(transaction);
        log.info("Transfer {} completed successfully. Source new balance: {}, Target new balance: {}",
                txId, newSourceBalance, newTargetBalance, newTargetBalance);
        return transactionMapper.toResponse(savedTx);
    }

    private String generateUniqueAccountNumber() {
        String accountNumber;
        do {
            long number = 1000000000L + (long) (RANDOM.nextDouble() * 9000000000L);
            accountNumber = String.valueOf(number);
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }
}
