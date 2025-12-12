package com.subcycle.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class SubscriptionRequest {
    @NotBlank(message = "訂閱名稱不能為空")
    @Size(max = 100, message = "訂閱名稱不能超過 100 個字元")
    private String name;

    @NotNull(message = "金額不能為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "金額必須大於 0")
    private BigDecimal amount;

    @NotBlank(message = "計費週期不能為空")
    @Pattern(regexp = "^(daily|weekly|monthly|quarterly|yearly)$", message = "計費週期必須為 daily, weekly, monthly, quarterly 或 yearly")
    private String cycle;

    @NotNull(message = "下次付款日期不能為空")
    @FutureOrPresent(message = "下次付款日期不能是過去日期")
    private LocalDate nextPaymentDate;

    private Long categoryId;

    @Pattern(regexp = "^(active|paused|cancelled)?$", message = "狀態必須為 active, paused 或 cancelled")
    private String status;

    @Size(max = 500, message = "描述不能超過 500 個字元")
    private String description;
}
