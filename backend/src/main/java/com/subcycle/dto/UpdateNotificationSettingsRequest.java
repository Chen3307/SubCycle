package com.subcycle.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateNotificationSettingsRequest {
    @NotNull(message = "通知開關不能為空")
    private Boolean enabled;
}
