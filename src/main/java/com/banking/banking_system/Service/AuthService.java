package com.banking.banking_system.Service;

import com.banking.banking_system.Dto.auth.AuthResponse;
import com.banking.banking_system.Dto.auth.ChangePasswordRequest;
import com.banking.banking_system.Dto.auth.LoginRequest;
import com.banking.banking_system.Dto.auth.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void changePassword(ChangePasswordRequest request);
}
