package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 認證回應 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String refreshToken;
    private Long userId;
    private String email;
    private String name;
    private String role;
    private String message;

    public AuthResponse(String token, String refreshToken, Long userId, String email, String name, String role) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public AuthResponse(String token, Long userId, String email, String name, String role) {
        this(token, null, userId, email, name, role);
    }
}
