package com.subcycle.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新 Token 請求 DTO
 */
@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh Token 不能為空")
    private String refreshToken;
}
