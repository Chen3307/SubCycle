package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryResponse {
    private Long id;
    private Long subscriptionId;
    private String subscriptionName;
    private BigDecimal amount;
    private LocalDate paymentDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
}
