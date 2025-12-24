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
    private LocalDate nextPaymentDate;

    @NotNull(message = "訂閱起始日不能為空")
    private LocalDate startDate;

    private Long categoryId;

    @Pattern(regexp = "^(active|paused|cancelled|expired|completed)?$", message = "狀態必須為 active, paused, cancelled, expired 或 completed")
    private String status;

    @Size(max = 3, message = "幣別代碼不能超過 3 個字元")
    private String currency = "TWD";

    private LocalDate endDate;

    private Boolean autoRenew = true;

    private Boolean reminderSent = false;

    private Boolean notificationEnabled = true;

    private Boolean includeHistoricalPayments = false;

}
