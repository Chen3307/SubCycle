package com.subcycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者重置用戶密碼請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetUserPasswordRequest {
    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "密碼長度至少為 6 個字元")
    private String newPassword;
}
