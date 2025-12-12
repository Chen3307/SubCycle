package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private String name;
    private BigDecimal amount;
    private String cycle;
    private LocalDate nextPaymentDate;
    private Long categoryId;
    private String status;
    private String description;
}
