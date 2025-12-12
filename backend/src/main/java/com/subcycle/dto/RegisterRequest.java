package com.subcycle.dto;

import lombok.Data;

/**
 * 註冊請求 DTO
 */
@Data
public class RegisterRequest {
    private String email;
    private String password;
    private String name;
}
