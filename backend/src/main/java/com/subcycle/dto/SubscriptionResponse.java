package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private Long id;
    private String name;
    private BigDecimal price;  // 改為 price 以匹配 Entity
    private String billingCycle;  // 改為 billingCycle 以匹配 Entity
    private LocalDate nextPaymentDate;
    private LocalDate startDate;
    private LocalDate endDate;
    private CategoryResponse category;  // 改為 CategoryResponse 對象
    private String status;
    private String currency;
    private Boolean autoRenew;
    private Boolean reminderSent;
    private Boolean notificationEnabled;
    private Boolean includeHistoricalPayments;
}
