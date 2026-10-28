package com.banking.banking_system.Service.impl;

import com.banking.banking_system.Dto.customer.CustomerRequest;
import com.banking.banking_system.Dto.customer.CustomerResponse;
import com.banking.banking_system.Entity.Customer;
import com.banking.banking_system.Exception.CustomerNotFoundException;
import com.banking.banking_system.Exception.EmailAlreadyExistsException;
import com.banking.banking_system.Mapper.CustomerMapper;
import com.banking.banking_system.Repository.CustomerRepository;
import com.banking.banking_system.Repository.Specification.CustomerSpecification;
import com.banking.banking_system.Repository.UserRepository;
import com.banking.banking_system.Service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerRequest request, String authenticatedUsername){
        log.info("Creating customer with email: {}", request.getEmail());
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Customer email already registered: " + request.getEmail());
        }
        Customer customer = customerMapper.toEntity(request);
        if (authenticatedUsername != null) {
            userRepository.findByIdentifier(authenticatedUsername).ifPresent(customer::setUser);
        }
        Customer savedCustomer = customerRepository.save(customer);
        log.info("Successfully created customer id: {}", savedCustomer.getId());
        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Long id){
        log.debug("Fetching customer by id: {}", id);
        Customer customer = customerRepository.findById(id).orElseThrow(() ->
                new CustomerNotFoundException("Customer not found with id: " + id));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByEmail(String email){
        log.debug("Fetching customer by email: {}", email);
        Customer customer = customerRepository.findByEmail(email).orElseThrow(() ->
                new CustomerNotFoundException("Customer not found with email: " + email));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Long id, CustomerRequest request){
        log.info("Updating customer with id: {}", id);
        Customer customer = customerRepository.findById(id).orElseThrow(() ->
                new CustomerNotFoundException("Customer not found with id: " + id));
        if (!customer.getEmail().equalsIgnoreCase(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already taken: " + request.getEmail());
        }
        customerMapper.updateEntityFromRequest(customer, request);
        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Successfully updated customer id: {}", updatedCustomer.getId());
        return customerMapper.toResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id){
        log.info("Deleting customer with id: {}", id);
        if (!customerRepository.existsById(id)) {
            throw new CustomerNotFoundException("Customer not found with id: " + id);
        }
        customerRepository.deleteById(id);
        log.info("Successfully deleted customer id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(String search, Pageable pageable){
        log.debug("Fetching all customers with search: {} and pageable: {}", search,  pageable);
        Specification<Customer> spec = CustomerSpecification.filterCustomer(search);
        return customerRepository.findAll(spec, pageable).map(customerMapper::toResponse);
    }
}
