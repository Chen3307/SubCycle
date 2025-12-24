package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionTemplateResponse {
    private String name;
    private String categoryName;
    private String icon;
    private String color;
    private String description;
    private String billingCycle; // 建议的计费周期
}
