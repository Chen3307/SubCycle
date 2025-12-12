package com.subcycle.dto;

import lombok.Data;

/**
 * 登入請求 DTO
 */
@Data
public class LoginRequest {
    private String email;
    private String password;
}
