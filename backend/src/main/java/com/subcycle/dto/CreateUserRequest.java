package com.subcycle.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 管理者建立用戶請求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "名稱不能為空")
    @Size(max = 50, message = "姓名不能超過 50 個字元")
    private String name;

    @NotBlank(message = "Email 不能為空")
    @Email(message = "Email 格式不正確")
    private String email;

    @NotBlank(message = "密碼不能為空")
    @Size(min = 6, message = "密碼至少需要 6 個字元")
    private String password;

    @NotBlank(message = "角色不能為空")
    private String role;

    private Boolean isActive;
}
