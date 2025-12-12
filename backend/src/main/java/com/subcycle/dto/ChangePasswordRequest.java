package com.subcycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank(message = "目前密碼不能為空")
    private String currentPassword;

    @NotBlank(message = "新密碼不能為空")
    @Size(min = 6, message = "新密碼至少需要 6 個字元")
    private String newPassword;
}
