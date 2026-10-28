package com.banking.banking_system.Controller;

import com.banking.banking_system.Dto.auth.AuthResponse;
import com.banking.banking_system.Dto.auth.ChangePasswordRequest;
import com.banking.banking_system.Dto.auth.LoginRequest;
import com.banking.banking_system.Dto.auth.RegisterRequest;
import com.banking.banking_system.Service.AuthService;
import com.banking.banking_system.annotation.Audited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration and JWT authentication")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @Audited(action = "REGISTER", resource = "User", description = "User registration")
    @Operation(summary = "Register a new user", description = "Registers a new user account with BCrypt password hashing and returns JWT token.")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Audited(action = "LOGIN", resource = "User", description = "User authentication")
    @Operation(summary = "Login user", description = "Authenticates user credentials and returns a signed JWT Bearer token.")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    @Audited(action = "CHANGE_PASSWORD", resource = "User", description = "User password change")
    @Operation(summary = "Change password", description = "Updates password for the currently authenticated user")
    @SecurityRequirement(name = "BearerAuth")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok(Map.of("message", "Password changed successfully"));
    }
}

