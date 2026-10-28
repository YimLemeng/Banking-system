package com.banking.banking_system.Service;

import com.banking.banking_system.Dto.customer.CustomerRequest;
import com.banking.banking_system.Dto.customer.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {
    CustomerResponse createCustomer(CustomerRequest request,  String authenticatedUsername);
    CustomerResponse getCustomerById(Long id);
    CustomerResponse getCustomerByEmail(String email);
    CustomerResponse updateCustomer(Long id, CustomerRequest request);
    void deleteCustomer(Long id);
    Page<CustomerResponse> getAllCustomers(String search, Pageable pageable);
}
