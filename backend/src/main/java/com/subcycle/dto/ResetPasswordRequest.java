package com.subcycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotBlank(message = "Token 不能為空")
    private String token;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "密碼至少需要 6 個字元")
    private String newPassword;
}
