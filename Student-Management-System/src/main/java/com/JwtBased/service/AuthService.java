package com.JwtBased.service;

import com.JwtBased.dto.ApiResponse;
import com.JwtBased.dto.LoginRequest;
import com.JwtBased.dto.LoginResponse;
import com.JwtBased.entity.User;
import com.JwtBased.repository.UserRepo;
import com.JwtBased.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepo userRepository;

    // ✅ Return type changed to ApiResponse<LoginResponse>
    public ApiResponse<LoginResponse> login(LoginRequest request) {

        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Get user details
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate tokens
        String token = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(authentication);

        // Build response
        LoginResponse loginResponse = LoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .fullName(user.getFullName())
                .message("Login successful")
                .build();

        return ApiResponse.success("Login successful", loginResponse);
    }
}