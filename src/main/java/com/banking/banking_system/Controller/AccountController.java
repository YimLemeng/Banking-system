package com.banking.banking_system.Controller;

import com.banking.banking_system.Dto.account.*;
import com.banking.banking_system.Service.AccountService;
import com.banking.banking_system.annotation.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Operations", description = "Endpoints for bank accounts, deposits, withdrawals, and transfers")
@SecurityRequirement(name = "BearerAuth")
public class AccountController {
    private final AccountService accountService;

    @PostMapping
    @Audited(action = "CREATE_ACCOUNT", resource = "Account", description = "Create new bank account")
    @Operation(summary = "Create a new bank account", description = "Creates a new savings or checking account for a customer.")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request){
        AccountResponse response = accountService.createAccount(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponse> getAccountById(@PathVariable Long id){
        return ResponseEntity.ok(accountService.getAccountById(id));
    }

    @GetMapping("/number/{accountNumber}")
    @Operation(summary = "Get account by account number")
    public ResponseEntity<AccountResponse> getAccountByNumber(@PathVariable String accountNumber){
        return ResponseEntity.ok(accountService.getAccountByNumber(accountNumber));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get all accounts belonging to a customer")
    public ResponseEntity<Page<AccountResponse>> getAccountByCustomerId(
            @PathVariable Long customerId,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(accountService.getAccountByCustomerId(customerId, pageable));
    }

    @GetMapping
    @Operation(summary = "Get all bank accounts with pagination and sorting")
    public ResponseEntity<Page<AccountResponse>> getAllAccounts(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "id",  direction = Sort.Direction.DESC) Pageable pageable){
        return ResponseEntity.ok(accountService.getAllAccounts(pageable));
    }

    @PostMapping("/{accountNumber}/deposit")
    @Audited(action = "DEPOSIT", resource = "Account", description = "Deposit funds into account")
    @Operation(summary = "Deposit money into account")
    public ResponseEntity<AccountResponse> deposit(@PathVariable String accountNumber, @Valid @RequestBody DepositRequest request){
        return ResponseEntity.ok(accountService.deposit(accountNumber, request));
    }

    @PostMapping("/{accountNumber}/withdraw")
    @Audited(action = "WITHDRAW", resource = "Account", description = "Withdraw funds from account")
    @Operation(summary = "Withdraw money from account")
    public ResponseEntity<AccountResponse> withdraw(@PathVariable String accountNumber, @Valid @RequestBody WithdrawRequest request){
        return ResponseEntity.ok(accountService.withdraw(accountNumber, request));
    }

    @PostMapping("/transfer")
    @Audited(action = "TRANSFER", resource = "Account", description = "Transfer funds between accounts")
    @Operation(summary = "Transfer money between accounts", description = "Executes an atomic money transfer between source and destination accounts.")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request){
        return ResponseEntity.ok(accountService.transfer(request));
    }
}
