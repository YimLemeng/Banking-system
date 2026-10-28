package com.banking.banking_system.Dto.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerRequest {

    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Phone number must be between 8 and 15 digits")
    private String phoneNumber;

    @NotBlank(message = "National ID or passport is required")
    private String nationalIdOrPassport;

    private String address;
}
