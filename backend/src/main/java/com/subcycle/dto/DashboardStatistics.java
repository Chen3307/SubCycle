package com.subcycle.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 儀表板統計數據 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatistics {

    // 基本統計
    private BigDecimal monthlyAverage;          // 總月均支出
    private BigDecimal next30DaysTotal;         // 下30天應付總額
    private Integer totalSubscriptions;          // 訂閱項目總數

    // 本月統計
    private BigDecimal currentMonthPaid;        // 本月已支出
    private BigDecimal currentMonthTotal;       // 本月預計總支出
    private BigDecimal currentMonthRemaining;   // 本月剩餘
    private Double currentMonthProgress;        // 本月進度百分比

    // 環比數據
    private BigDecimal lastMonthTotal;          // 上月總支出
    private BigDecimal monthOverMonthChange;    // 環比變化金額
    private Double monthOverMonthChangePercent; // 環比變化百分比

    // Top 5 訂閱
    private List<TopSubscription> topSubscriptions;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSubscription {
        private Long id;
        private String name;
        private BigDecimal amount;
        private String cycle;
        private BigDecimal monthlyAmount; // 本月花費
    }
}
