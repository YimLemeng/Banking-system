package com.banking.banking_system.Controller;

import com.banking.banking_system.Dto.account.TransactionResponse;
import com.banking.banking_system.Entity.Enum.TransactionType;
import com.banking.banking_system.Service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction History", description = "Endpoints for viewing and querying transaction history and audit logs")
@SecurityRequirement(name = "BearerAuth")
public class TransactionController {
    private final TransactionService transactionService;

    @GetMapping
    @Operation(summary = "Get all transactions with pagination")
    public ResponseEntity<Page<TransactionResponse>> getAllTransactions(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(transactionService.searchTransaction(null, null, null, null, null, null, pageable));
    }

    @GetMapping("/{transactionId}")
    @Operation(summary = "Get transaction details by transaction ID")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransactionById(transactionId));
    }

    @GetMapping("/account/{accountId}")
    @Operation(summary = "Get transaction history for a specific account")
    public ResponseEntity<Page<TransactionResponse>> getAccountTransactions(
            @PathVariable Long accountId,
            @ParameterObject @PageableDefault (
                    page = 0,
                    size = 10,
                    sort = "timestamp",
                    direction = Sort.Direction.DESC)
            Pageable pageable) {
        return ResponseEntity.ok(transactionService.getAccountTransactions(accountId, pageable));

    }

    @GetMapping("/search")
    @Operation(summary = "Search transaction history with filters", description = "Filter by accountId, type (DEPOSIT, WITHDRAW, TRANSFER), date range, and amount range.")
    public ResponseEntity<Page<TransactionResponse>> searchTransactions(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(transactionService.searchTransaction(accountId, type, startDate, endDate, minAmount, maxAmount, pageable));
    }
}
