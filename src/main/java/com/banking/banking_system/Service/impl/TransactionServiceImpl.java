package com.banking.banking_system.Service.impl;

import com.banking.banking_system.Dto.account.TransactionResponse;
import com.banking.banking_system.Entity.BankTransaction;
import com.banking.banking_system.Entity.Enum.TransactionType;
import com.banking.banking_system.Exception.AccountNotFoundException;
import com.banking.banking_system.Mapper.TransactionMapper;
import com.banking.banking_system.Repository.AccountRepository;
import com.banking.banking_system.Repository.Specification.TransactionSpecification;
import com.banking.banking_system.Repository.TransactionRepository;
import com.banking.banking_system.Service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final TransactionMapper transactionMapper;

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(String transactionId) {
        log.debug("Fetching transaction by id {}", transactionId);
        BankTransaction transaction = transactionRepository.findByTransactionId(transactionId).orElseThrow(() ->
                new IllegalArgumentException("Transaction not found with transaction ID: " + transactionId));
        return transactionMapper.toResponse(transaction);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAccountTransactions(Long accountId, Pageable pageable) {
        log.debug("Fetching transactions by account id {}", accountId);
        if (!accountRepository.existsById(accountId)) {
            throw new AccountNotFoundException("Account not found with account ID: " + accountId);
        }
        return transactionRepository.findByAccountIdOrTargetAccountId(accountId, accountId, pageable).map(transactionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TransactionResponse> searchTransaction(
            Long accountId,
            TransactionType type,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable
    ) {
        log.debug("Searching transactions with accountId: {}, type: {}, start: {}, end: {}",  accountId, type, startDate, endDate);
        Specification<BankTransaction> spec = TransactionSpecification.filterTransactions(accountId, type, startDate, endDate, minAmount, maxAmount);
        return transactionRepository.findAll(spec, pageable).map(transactionMapper::toResponse);
    }
}
