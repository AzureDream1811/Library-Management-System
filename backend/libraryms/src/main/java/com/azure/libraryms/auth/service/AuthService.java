package com.azure.libraryms.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import com.azure.libraryms.auth.dto.request.LoginRequest;
import com.azure.libraryms.auth.dto.response.LoginResponse;
import com.azure.libraryms.auth.model.User;
import com.azure.libraryms.auth.repository.UserRepository;
import com.azure.libraryms.auth.security.JwtService;
import com.azure.libraryms.common.exception.InvalidCredentialsException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;



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
}
