package com.banking.banking_system.Repository;

import com.banking.banking_system.Entity.BankTransaction;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<BankTransaction, Long>, JpaSpecificationExecutor<BankTransaction> {
    Optional<BankTransaction> findByTransactionId(String transactionId);
    Page<BankTransaction> findByAccountIdOrTargetAccountId(Long accountId, Long targetAccountId, Pageable pageable);
}
