package com.banking.banking_system.Mapper;

import com.banking.banking_system.Dto.customer.CustomerRequest;
import com.banking.banking_system.Dto.customer.CustomerResponse;
import com.banking.banking_system.Entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {
    public Customer toEntity(CustomerRequest request) {
        if (request == null) return null;
        return Customer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .nationalIdOrPassport(request.getNationalIdOrPassport())
                .address(request.getAddress())
                .build();
    }

    public CustomerResponse toResponse(Customer customer) {
        if (customer == null) return null;
        return CustomerResponse.builder()
                .id(customer.getId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .nationalIdOrPassport(customer.getNationalIdOrPassport())
                .address(customer.getAddress())
                .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public void updateEntityFromRequest(Customer customer, CustomerRequest request) {
        if (request == null || customer == null) return;
        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setNationalIdOrPassport(request.getNationalIdOrPassport());
        customer.setAddress(request.getAddress());
    }
}
