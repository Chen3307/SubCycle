package com.subcycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NotificationRequest {
    private Long subscriptionId;

    @NotBlank(message = "通知類型不能為空")
    @Pattern(regexp = "^(payment_due|payment_completed|subscription_expiring|general)$",
             message = "通知類型必須為 payment_due, payment_completed, subscription_expiring 或 general")
    private String type;

    @NotBlank(message = "標題不能為空")
    @Size(max = 100, message = "標題不能超過 100 個字元")
    private String title;

    @Size(max = 500, message = "訊息不能超過 500 個字元")
    private String message;
}
