package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private Long id;
    private Long subscriptionId;
    private String subscriptionName;
    private String type;
    private String title;
    private String message;
    private Boolean isRead;
    private LocalDateTime createdAt;
}
