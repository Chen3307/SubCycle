package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String name;
    private String avatar;
    private String currency;
    private Integer notificationDays;
    private String role;
    private LocalDateTime createdAt;
}
