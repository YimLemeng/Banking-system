package com.banking.banking_system.Service.impl;

import com.banking.banking_system.Dto.auth.AuthResponse;
import com.banking.banking_system.Dto.auth.ChangePasswordRequest;
import com.banking.banking_system.Dto.auth.LoginRequest;
import com.banking.banking_system.Dto.auth.RegisterRequest;
import com.banking.banking_system.Entity.Enum.Role;
import com.banking.banking_system.Entity.User;
import com.banking.banking_system.Exception.EmailAlreadyExistsException;
import com.banking.banking_system.Repository.UserRepository;
import com.banking.banking_system.Security.UserPrincipal;
import com.banking.banking_system.Security.jwtService;
import com.banking.banking_system.Service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final jwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request){
        log.info("Registering user with phone: {}", request.getPhoneNumber());
        if (request.getConfirmPassword() != null && !request.getConfirmPassword().equals(request.getPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("Phone number already registered: " + request.getPhoneNumber());
        }

        String username = (request.getUsername() != null && !request.getUsername().isBlank()) ?
                request.getUsername() : request.getPhoneNumber();

        if (request.getUsername() != null && !request.getUsername().isBlank() && userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException("Email already registered: " + request.getEmail());
            }
        }

        Role role = request.getRole() != null ? request.getRole() : Role.ROLE_USER;
        User user = User.builder()
                .phoneNumber(request.getPhoneNumber())
                .username(username)
                .email(request.getEmail() != null && !request.getEmail().isBlank() ? request.getEmail() : null)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .build();
        User savedUser = userRepository.save(user);
        log.info("Successfully registered user with id: {}", savedUser.getId());

        UserPrincipal userPrincipal = UserPrincipal.create(savedUser);
        String jwtToken = jwtService.generateToken(userPrincipal);

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .phoneNumber(savedUser.getPhoneNumber())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request){
        log.info("Authenticating user with identifier: {}", request.getIdentifier());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getIdentifier(),
                        request.getPassword()
                ));

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        String jwtToken = jwtService.generateToken(userPrincipal);
        log.info("User {} successfully authenticated", userPrincipal.getUsername());

        return AuthResponse.builder()
                .token(jwtToken)
                .tokenType("Bearer")
                .phoneNumber(userPrincipal.getPhoneNumber())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .role(userPrincipal.getRole().name())
                .build();
    }

    @Override
    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new IllegalArgumentException("User must be authenticated to change password");
        }

        String identifier = authentication.getName();
        User user = userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new IllegalArgumentException("User not found with identifier: " + identifier));

        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New password and confirm new password do not match");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password does not match");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("New password cannot be the same as the current password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password updated successfully for user: {}", identifier);
    }
}
