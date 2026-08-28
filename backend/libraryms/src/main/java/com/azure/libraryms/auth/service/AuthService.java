package com.azure.libraryms.auth.service;

import java.time.LocalDateTime;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.azure.libraryms.auth.dto.request.LoginRequest;
import com.azure.libraryms.auth.dto.request.RegisterRequest;
import com.azure.libraryms.auth.dto.response.LoginResponse;
import com.azure.libraryms.auth.dto.response.RegisterResponse;
import com.azure.libraryms.auth.model.MembershipStatus;
import com.azure.libraryms.auth.model.Role;
import com.azure.libraryms.auth.model.User;
import com.azure.libraryms.auth.repository.UserRepository;
import com.azure.libraryms.auth.security.JwtService;
import com.azure.libraryms.common.exception.EntityAlreadyExistsException;
import com.azure.libraryms.common.exception.InvalidCredentialsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Register
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EntityAlreadyExistsException("User", "email", request.email());
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName())
                .role(Role.USER)
                .membershipStatus(MembershipStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        try {
            User savedUser = userRepository.save(user);
            return new RegisterResponse(savedUser.getId(), savedUser.getEmail());
        } catch (DataIntegrityViolationException e) {
            throw new EntityAlreadyExistsException("User", "email", request.email());
        }
    }

    // Login
    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        } catch (DisabledException | LockedException e) {
            throw new InvalidCredentialsException("Account is not available");
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user not found: " + request.email()));

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken);
    }

    // logout
    public void logout(String refreshToken) {
        // TODO: Implement logout functionality, such as invalidating the refresh token or removing it from the database/cache.
    }
}
