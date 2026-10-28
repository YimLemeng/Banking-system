package com.banking.banking_system.Controller;

import com.banking.banking_system.Dto.user.UserResponse;
import com.banking.banking_system.Entity.User;
import com.banking.banking_system.Repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "System Users", description = "Endpoints for viewing and auditing system staff and users")
@SecurityRequirement(name = "BearerAuth")
public class UserController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all system users from database")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> list = userRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get system user by ID")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(u -> ResponseEntity.ok(mapToResponse(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    private UserResponse mapToResponse(User u) {
        String roleStr = u.getRole() != null ? u.getRole().name() : "ROLE_USER";
        String fullName;
        String terminalId;
        String branch = "Head Office (Phnom Penh)";

        if (roleStr.contains("ADMIN") || "admin".equalsIgnoreCase(u.getUsername())) {
            fullName = "System Administrator";
            terminalId = "TB-0492";
        } else if ("teller".equalsIgnoreCase(u.getUsername())) {
            fullName = "Teller Officer";
            terminalId = "TB-0104";
        } else {
            fullName = u.getUsername() != null
                    ? u.getUsername().substring(0, 1).toUpperCase() + u.getUsername().substring(1)
                    : "Bank Staff";
            terminalId = "TB-" + String.format("%04d", (u.getId() != null ? u.getId() * 117 % 10000 : 100));
        }

        return UserResponse.builder()
                .id(u.getId())
                .username(u.getUsername())
                .fullName(fullName)
                .email(u.getEmail())
                .phoneNumber(u.getPhoneNumber())
                .role(u.getRole())
                .branch(branch)
                .status("ACTIVE")
                .terminalId(terminalId)
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt())
                .build();
    }
}
