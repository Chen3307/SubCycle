package com.subcycle.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "類別名稱不能為空")
    @Size(max = 50, message = "類別名稱不能超過 50 個字元")
    private String name;

    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "顏色必須為有效的十六進位色碼（例如：#FF0000）")
    @Size(max = 20, message = "顏色不能超過 20 個字元")
    private String color;

    @Size(max = 50, message = "圖示不能超過 50 個字元")
    private String icon;

    private Integer sortOrder;
}
