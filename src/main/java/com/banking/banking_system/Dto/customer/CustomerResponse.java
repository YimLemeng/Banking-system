package com.banking.banking_system.Dto.customer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerResponse {
    private Long id;
    private  String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String nationalIdOrPassport;
    private String address;
    private Long userId;
    private Instant createdAt;
    private Instant updatedAt;
}
