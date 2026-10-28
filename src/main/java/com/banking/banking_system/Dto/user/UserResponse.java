package com.banking.banking_system.Dto.user;

import com.banking.banking_system.Entity.Enum.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Role role;
    private String branch;
    private String status;
    private String terminalId;
    private Instant createdAt;
    private Instant updatedAt;
}
