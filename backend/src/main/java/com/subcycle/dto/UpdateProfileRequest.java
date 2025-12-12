package com.subcycle.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 100, message = "姓名不能超過 100 個字元")
    private String name;

    @Size(max = 500, message = "頭像URL不能超過 500 個字元")
    private String avatar;

    @Pattern(regexp = "^[A-Z]{3}$", message = "幣別必須為3個大寫字母（例如：TWD, USD）")
    private String currency;

    private Integer notificationDays;
}
