package com.banking.banking_system.Controller;

import com.banking.banking_system.annotation.Audited;
import com.banking.banking_system.Dto.customer.CustomerRequest;
import com.banking.banking_system.Dto.customer.CustomerResponse;
import com.banking.banking_system.Security.UserPrincipal;
import com.banking.banking_system.Service.CustomerService;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customer Management", description = "Endpoints for Customer CRUD, pagination, sorting and dynamic search")
@SecurityRequirement(name = "BearerAuth")
public class CustomerController {
    private final CustomerService customerService;

    @PostMapping
    @Audited(action = "CREATE_CUSTOMER", resource = "Customer", description = "Create customer profile")
    @Operation(summary = "Create customer profile", description = "Creates a new customer profile linked to authenticated user.")
    public ResponseEntity<CustomerResponse> addCustomer(@Valid @RequestBody CustomerRequest request,
                                                @AuthenticationPrincipal UserPrincipal currentUser) {
        String username = currentUser != null ? currentUser.getUsername() : null;
        CustomerResponse response = customerService.createCustomer(request, username);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/by-email")
    @Operation(summary = "Get customer by email")
    public ResponseEntity<CustomerResponse> getCustomerByEmail(@RequestParam String email) {
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @PutMapping("/{id}")
    @Audited(action = "UPDATE_CUSTOMER", resource = "Customer", description = "Update customer profile")
    @Operation(summary = "Update customer profile")
    public ResponseEntity<CustomerResponse> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.updateCustomer(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Audited(action = "DELETE_CUSTOMER", resource = "Customer", description = "Delete customer profile")
    @Operation(summary = "Delete customer profile (Admin only)")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get all customers with pagination, sorting, and dynamic keyword search")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return  ResponseEntity.ok(customerService.getAllCustomers(search, pageable));
    }
}
