package com.subcycle.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PaymentHistoryRequest {
    @NotNull(message = "訂閱ID不能為空")
    private Long subscriptionId;

    @NotNull(message = "金額不能為空")
    @DecimalMin(value = "0.0", inclusive = false, message = "金額必須大於 0")
    private BigDecimal amount;

    @NotNull(message = "付款日期不能為空")
    @PastOrPresent(message = "付款日期不能是未來日期")
    private LocalDate paymentDate;

    @NotBlank(message = "付款狀態不能為空")
    @Pattern(regexp = "^(completed|pending|failed|refunded)$",
             message = "付款狀態必須為 completed, pending, failed 或 refunded")
    private String status;

    @Size(max = 1000, message = "備註不能超過 1000 個字元")
    private String notes;
}
