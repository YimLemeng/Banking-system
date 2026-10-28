package com.banking.banking_system.Dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    @NotBlank(message = "Identifier (phone, username, or email) is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;
}
