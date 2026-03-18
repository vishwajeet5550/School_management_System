package com.JwtBased.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private String token;
    private String refreshToken;
    private String username;
    private String email;
    private String role;
    private String fullName;
    private String message;
}